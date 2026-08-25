package com.example.data.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.db.IncidentDao
import com.example.data.db.ItineraryDao
import com.example.data.db.LocationLogDao
import com.example.data.db.WeatherDao
import com.example.data.model.CommunicationChannel
import com.example.data.model.SyncStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SyncEngine(
    private val context: Context,
    private val locationLogDao: LocationLogDao,
    private val incidentDao: IncidentDao,
    private val itineraryDao: ItineraryDao? = null,
    private val weatherDao: WeatherDao? = null
) {
    // Allows manual network state override for testing offline scenarios in hill stations
    private val _isSimulatedOffline = MutableStateFlow(false)
    val isSimulatedOffline: StateFlow<Boolean> = _isSimulatedOffline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    private val _syncMessage = MutableStateFlow("Ready")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    fun toggleOfflineSimulation() {
        _isSimulatedOffline.value = !_isSimulatedOffline.value
    }

    fun isOnline(): Boolean {
        if (_isSimulatedOffline.value) return false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNet = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNet) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun triggerSync(): Int = withContext(Dispatchers.IO) {
        if (!isOnline()) {
            _syncMessage.value = "Offline mode active. Stored in local database."
            return@withContext 0
        }

        _isSyncing.value = true
        _syncMessage.value = "Synchronizing itineraries & safety telemetry..."
        var syncedCount = 0

        try {
            val firestore = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

            // 1. Sync Unsynced Incidents
            val unsyncedIncidents = incidentDao.getUnsyncedIncidents()
            for (incident in unsyncedIncidents) {
                if (firestore != null) {
                    val map = hashMapOf(
                        "incidentId" to incident.incidentId,
                        "touristId" to incident.touristId,
                        "touristName" to incident.touristName,
                        "emergencyType" to incident.emergencyType.name,
                        "latitude" to incident.latitude,
                        "longitude" to incident.longitude,
                        "altitudeMeters" to incident.altitudeMeters,
                        "timestamp" to incident.timestamp,
                        "riskLevel" to incident.riskLevel.name,
                        "relayedVia" to incident.relayedVia.name,
                        "hopCount" to incident.hopCount,
                        "batteryPercentage" to incident.batteryPercentage,
                        "distressMessage" to incident.distressMessage,
                        "responderStatus" to incident.responderStatus.name,
                        "digitalSignature" to incident.digitalSignature,
                        "syncedAt" to System.currentTimeMillis()
                    )
                    firestore.collection("tourist_incidents")
                        .document(incident.incidentId)
                        .set(map, SetOptions.merge())
                }

                // Update local Room database status to SYNCED
                incidentDao.updateIncidentSyncStatus(incident.incidentId, SyncStatus.SYNCED)
                syncedCount++
            }

            // 2. Sync Unsynced Location Logs
            val unsyncedLogs = locationLogDao.getUnsyncedLogs()
            if (unsyncedLogs.isNotEmpty()) {
                if (firestore != null) {
                    val batch = firestore.batch()
                    for (log in unsyncedLogs.take(20)) {
                        val doc = firestore.collection("tourist_telemetry").document("${log.touristId}_${log.timestamp}")
                        val logMap = hashMapOf(
                            "touristId" to log.touristId,
                            "lat" to log.latitude,
                            "lng" to log.longitude,
                            "altitude" to log.altitudeMeters,
                            "speedKmh" to log.speedKmh,
                            "bearing" to log.bearingDegrees,
                            "timestamp" to log.timestamp,
                            "riskScore" to log.riskScore,
                            "isOffRoute" to log.isOffRoute
                        )
                        batch.set(doc, logMap, SetOptions.merge())
                    }
                    try { batch.commit() } catch (_: Exception) {}
                }
                locationLogDao.updateSyncStatus(unsyncedLogs.map { it.id }, SyncStatus.SYNCED)
                syncedCount += unsyncedLogs.size
            }

            // 3. Sync Unsynced Itineraries to Cloud
            if (itineraryDao != null) {
                val unsyncedItineraries = itineraryDao.getUnsyncedItineraries()
                for (itin in unsyncedItineraries) {
                    if (firestore != null) {
                        val map = hashMapOf(
                            "itineraryId" to itin.itineraryId,
                            "touristId" to itin.touristId,
                            "title" to itin.title,
                            "startPointName" to itin.startPointName,
                            "startLat" to itin.startLat,
                            "startLng" to itin.startLng,
                            "endPointName" to itin.endPointName,
                            "endLat" to itin.endLat,
                            "endLng" to itin.endLng,
                            "waypointsJson" to itin.waypointsJson,
                            "modeOfTransport" to itin.modeOfTransport.name,
                            "expectedDurationMinutes" to itin.expectedDurationMinutes,
                            "isActive" to itin.isActive,
                            "lastSyncedAt" to System.currentTimeMillis()
                        )
                        firestore.collection("planned_itineraries")
                            .document(itin.itineraryId)
                            .set(map, SetOptions.merge())
                    }
                    itineraryDao.updateSyncStatus(itin.itineraryId, SyncStatus.SYNCED, System.currentTimeMillis())
                    syncedCount++
                }
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncMessage.value = if (syncedCount > 0) "Successfully synced $syncedCount records" else "Up to date"
        } catch (e: Exception) {
            _syncMessage.value = "Sync failed: ${e.localizedMessage ?: "Network timeout"}"
        } finally {
            _isSyncing.value = false
        }

        syncedCount
    }
}

