package com.example.data.repository

import android.content.Context
import com.example.data.crypto.DigitalIdManager
import com.example.data.db.SafeTourDatabase
import com.example.data.engine.AiSafetyAdvisory
import com.example.data.engine.BleMeshEngine
import com.example.data.engine.BleMeshRelayEvent
import com.example.data.engine.GeminiSafetyAdvisor
import com.example.data.engine.ItineraryEngine
import com.example.data.engine.MeshPeerNode
import com.example.data.engine.RiskAnalysisResult
import com.example.data.engine.SafetyRiskEngine
import com.example.data.engine.SyncEngine
import com.example.data.engine.WeatherAlertEngine
import com.example.data.model.BleMeshPacketEntity
import com.example.data.model.CommunicationChannel
import com.example.data.model.EmergencyType
import com.example.data.model.GeoFenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.ItineraryEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.RiskLevel
import com.example.data.model.RouteEntity
import com.example.data.model.SyncStatus
import com.example.data.model.TouristEntity
import com.example.data.model.WeatherForecastEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TouristSafetyRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val database = SafeTourDatabase.getInstance(context)
    private val touristDao = database.touristDao()
    private val locationDao = database.locationLogDao()
    private val geoFenceDao = database.geoFenceDao()
    private val incidentDao = database.incidentDao()
    private val bleMeshDao = database.bleMeshDao()
    private val routeDao = database.routeDao()
    private val weatherDao = database.weatherDao()
    private val itineraryDao = database.itineraryDao()

    val syncEngine = SyncEngine(context, locationDao, incidentDao, itineraryDao, weatherDao)
    val bleMeshEngine = BleMeshEngine(bleMeshDao, incidentDao)

    // Data Flows from Room
    val currentTourist: Flow<TouristEntity?> = touristDao.getTouristFlow()
    val allGeoFences: Flow<List<GeoFenceEntity>> = geoFenceDao.getAllGeoFencesFlow()
    val allIncidents: Flow<List<IncidentEntity>> = incidentDao.getAllIncidentsFlow()
    val allMeshPackets: Flow<List<BleMeshPacketEntity>> = bleMeshDao.getAllMeshPacketsFlow()
    val allRoutes: Flow<List<RouteEntity>> = routeDao.getAllRoutesFlow()
    val recentLocationLogs: Flow<List<LocationLogEntity>> = locationDao.getRecentLocationLogs()
    val pendingSyncCount: Flow<Int> = incidentDao.getPendingSyncCount()
    val allWeatherForecasts: Flow<List<WeatherForecastEntity>> = weatherDao.getAllWeatherForecastsFlow()
    val allItineraries: Flow<List<ItineraryEntity>> = itineraryDao.getAllItinerariesFlow()
    val activeItinerary: Flow<ItineraryEntity?> = itineraryDao.getActiveItineraryFlow()

    // Live Telemetry State
    private val _currentLocation = MutableStateFlow(
        LocationLogEntity(
            touristId = "T-8492",
            latitude = 11.4102,
            longitude = 76.6950,
            altitudeMeters = 2240.0,
            speedKmh = 3.6f,
            bearingDegrees = 42.0f,
            accuracyMeters = 4.0f,
            riskScore = 12
        )
    )
    val currentLocation: StateFlow<LocationLogEntity> = _currentLocation.asStateFlow()

    private val _batteryLevel = MutableStateFlow(84)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isSimulatedInactivity = MutableStateFlow(false)
    val isSimulatedInactivity: StateFlow<Boolean> = _isSimulatedInactivity.asStateFlow()

    private val _isSimulatedOffRoute = MutableStateFlow(false)
    val isSimulatedOffRoute: StateFlow<Boolean> = _isSimulatedOffRoute.asStateFlow()

    private val _activeSosAlert = MutableStateFlow<IncidentEntity?>(null)
    val activeSosAlert: StateFlow<IncidentEntity?> = _activeSosAlert.asStateFlow()

    private val _aiAdvisory = MutableStateFlow<AiSafetyAdvisory?>(null)
    val aiAdvisory: StateFlow<AiSafetyAdvisory?> = _aiAdvisory.asStateFlow()

    private val _isGeneratingAiAdvisory = MutableStateFlow(false)
    val isGeneratingAiAdvisory: StateFlow<Boolean> = _isGeneratingAiAdvisory.asStateFlow()

    // Combined Live Risk Analysis Flow
    val riskAnalysis: StateFlow<RiskAnalysisResult> = combine(
        combine(_currentLocation, allGeoFences, _batteryLevel) { loc, fences, battery ->
            Triple(loc, fences, battery)
        },
        combine(_isSimulatedInactivity, _isSimulatedOffRoute) { inact, offRt ->
            Pair(inact, offRt)
        },
        combine(allWeatherForecasts, activeItinerary) { forecasts, itinerary ->
            Pair(forecasts, itinerary)
        }
    ) { (loc, fences, battery), (inact, offRt), (forecasts, itinerary) ->
        SafetyRiskEngine.evaluateRisk(
            currentLocation = loc,
            previousLogs = emptyList(),
            geoFences = fences,
            batteryPercent = battery,
            isSimulatedInactivity = inact,
            isSimulatedDeviatedRoute = offRt,
            weatherForecasts = forecasts,
            activeItinerary = itinerary
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = SafetyRiskEngine.evaluateRisk(
            currentLocation = _currentLocation.value,
            previousLogs = emptyList(),
            geoFences = emptyList()
        )
    )

    init {
        // Periodic background simulation loop for realistic GPS walk telemetry
        scope.launch(Dispatchers.Default) {
            var step = 0
            while (true) {
                delay(4000)
                if (_activeSosAlert.value == null) {
                    val current = _currentLocation.value
                    // Small simulated GPS jitter or movement
                    val latOffset = (Math.sin(step.toDouble() * 0.2) * 0.00015)
                    val lngOffset = (Math.cos(step.toDouble() * 0.2) * 0.00015)
                    val newLoc = current.copy(
                        id = 0,
                        latitude = current.latitude + latOffset,
                        longitude = current.longitude + lngOffset,
                        altitudeMeters = current.altitudeMeters + (if (step % 2 == 0) 1.2 else -0.8),
                        timestamp = System.currentTimeMillis(),
                        speedKmh = if (_isSimulatedInactivity.value) 0.0f else (3.2f + (Math.random() * 1.5).toFloat())
                    )
                    _currentLocation.value = newLoc
                    locationDao.insertLocationLog(newLoc)
                    step++
                }
            }
        }
    }

    /**
     * Move tourist to a specific location on the map (for simulation / demoing geo-fence breaches).
     */
    fun simulateMoveToCoordinate(lat: Double, lng: Double, altitude: Double = 2300.0, speed: Float = 4.0f) {
        val updated = _currentLocation.value.copy(
            id = 0,
            latitude = lat,
            longitude = lng,
            altitudeMeters = altitude,
            speedKmh = speed,
            timestamp = System.currentTimeMillis()
        )
        _currentLocation.value = updated
        scope.launch(Dispatchers.IO) {
            locationDao.insertLocationLog(updated)
        }
    }

    fun toggleInactivitySimulation() {
        _isSimulatedInactivity.value = !_isSimulatedInactivity.value
    }

    fun toggleOffRouteSimulation() {
        _isSimulatedOffRoute.value = !_isSimulatedOffRoute.value
    }

    fun setBatteryLevel(percent: Int) {
        _batteryLevel.value = percent.coerceIn(5, 100)
    }

    suspend fun downloadWeatherForRegion(regionId: String) {
        weatherDao.updateDownloadStatus(regionId, true, System.currentTimeMillis())
    }

    suspend fun refreshAllWeatherForecasts() {
        val forecasts = weatherDao.getAllWeatherForecasts()
        forecasts.forEach {
            weatherDao.updateDownloadStatus(it.regionId, true, System.currentTimeMillis())
        }
    }

    suspend fun saveItinerary(itinerary: ItineraryEntity) {
        itineraryDao.insertItinerary(itinerary)
        if (syncEngine.isOnline()) {
            syncEngine.triggerSync()
        }
    }

    suspend fun setActiveItinerary(itineraryId: String) {
        itineraryDao.deactivateAllItineraries()
        itineraryDao.activateItinerary(itineraryId)
    }

    suspend fun deactivateActiveItinerary() {
        itineraryDao.deactivateAllItineraries()
    }

    suspend fun deleteItinerary(itineraryId: String) {
        itineraryDao.deleteItinerary(itineraryId)
    }

    /**
     * Trigger Emergency SOS Alarm across Multi-Channel Failover System.
     */
    suspend fun triggerEmergencySos(
        type: EmergencyType,
        customMessage: String = "EMERGENCY: Tourist needs immediate assistance in Nilgiris sector."
    ): IncidentEntity {
        val loc = _currentLocation.value
        val tourist = touristDao.getTouristById("T-8492") ?: TouristEntity()
        val analysis = riskAnalysis.value

        val incidentId = "INC-${(1000..9999).random()}"
        val isNetAvailable = syncEngine.isOnline()

        val channel = if (isNetAvailable) {
            CommunicationChannel.INTERNET_DIRECT
        } else {
            CommunicationChannel.BLE_STORE_FORWARD
        }

        val syncStatus = if (isNetAvailable) SyncStatus.SYNCED else SyncStatus.PENDING

        val signature = "SIG_ED25519_" + DigitalIdManager.generateSha256Hash(
            "$incidentId|${tourist.touristId}|${loc.latitude}|${loc.longitude}|${System.currentTimeMillis()}"
        ).take(32)

        val incident = IncidentEntity(
            incidentId = incidentId,
            touristId = tourist.touristId,
            touristName = tourist.fullName,
            emergencyType = type,
            latitude = loc.latitude,
            longitude = loc.longitude,
            altitudeMeters = loc.altitudeMeters,
            timestamp = System.currentTimeMillis(),
            riskLevel = RiskLevel.CRITICAL,
            syncStatus = syncStatus,
            relayedVia = channel,
            hopCount = if (isNetAvailable) 0 else 1,
            batteryPercentage = _batteryLevel.value,
            distressMessage = customMessage,
            digitalSignature = signature
        )

        incidentDao.insertIncident(incident)
        _activeSosAlert.value = incident

        // Always create BLE packet for store-and-forward mesh resilience
        bleMeshEngine.broadcastSosPacket(incident)

        // Attempt cloud sync if online
        if (isNetAvailable) {
            syncEngine.triggerSync()
        }

        // Request AI guidance
        requestAiAdvisory(incident)

        return incident
    }

    fun cancelActiveSos() {
        _activeSosAlert.value = null
    }

    fun requestAiAdvisory(incident: IncidentEntity? = null) {
        scope.launch(Dispatchers.IO) {
            _isGeneratingAiAdvisory.value = true
            val analysis = riskAnalysis.value
            val advisory = GeminiSafetyAdvisor.generateSituationalAdvisory(
                riskLevel = incident?.riskLevel ?: analysis.riskLevel,
                currentZone = analysis.activeWarningZone,
                riskFactors = analysis.riskFactors,
                altitudeMeters = _currentLocation.value.altitudeMeters,
                batteryPercent = _batteryLevel.value,
                isOfflineMode = !syncEngine.isOnline(),
                weatherHeadline = analysis.weatherAssessment?.weatherAlertHeadline,
                routeDeviationMeters = analysis.routeDeviation?.deviationDistanceMeters
            )
            _aiAdvisory.value = advisory
            _isGeneratingAiAdvisory.value = false
        }
    }

    suspend fun updateTouristProfile(tourist: TouristEntity) {
        touristDao.insertTourist(tourist)
    }

    suspend fun acknowledgeIncident(incidentId: String, note: String) {
        val incident = incidentDao.getIncidentById(incidentId) ?: return
        val updated = incident.copy(
            responderStatus = com.example.data.model.ResponderStatus.ACKNOWLEDGED,
            responderNotes = note
        )
        incidentDao.updateIncident(updated)
    }

    suspend fun dispatchRescueTeam(incidentId: String, teamNote: String) {
        val incident = incidentDao.getIncidentById(incidentId) ?: return
        val updated = incident.copy(
            responderStatus = com.example.data.model.ResponderStatus.RESCUE_DISPATCHED,
            responderNotes = teamNote
        )
        incidentDao.updateIncident(updated)
    }
}
