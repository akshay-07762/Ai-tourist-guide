package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.crypto.DigitalIdManager
import com.example.data.engine.AiSafetyAdvisory
import com.example.data.engine.BleMeshRelayEvent
import com.example.data.engine.MeshPeerNode
import com.example.data.engine.RiskAnalysisResult
import com.example.data.model.BleMeshPacketEntity
import com.example.data.model.EmergencyType
import com.example.data.model.GeoFenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.RouteEntity
import com.example.data.model.TouristEntity
import com.example.data.repository.TouristSafetyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val title: String, val icon: String) {
    RADAR("Safety Radar", "radar"),
    MAP("Offline Map", "map"),
    ITINERARY("Itinerary", "route"),
    WEATHER("Weather Radar", "thunderstorm"),
    INCIDENTS("SOS Incidents", "warning"),
    BLE_MESH("BLE Mesh", "bluetooth"),
    DIGITAL_ID("Digital ID", "badge"),
    CONTROL_CENTRE("Control Centre", "admin_panel_settings")
}

class SafetyViewModel(application: Application) : AndroidViewModel(application) {

    val repository = TouristSafetyRepository(application, viewModelScope)

    // Screen navigation state
    var currentScreen: StateFlow<AppScreen> = kotlinx.coroutines.flow.MutableStateFlow(AppScreen.RADAR)
        private set

    fun navigateTo(screen: AppScreen) {
        (currentScreen as kotlinx.coroutines.flow.MutableStateFlow).value = screen
    }

    // Observed Flows
    val tourist: StateFlow<TouristEntity?> = repository.currentTourist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val geoFences: StateFlow<List<GeoFenceEntity>> = repository.allGeoFences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incidents: StateFlow<List<IncidentEntity>> = repository.allIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val meshPackets: StateFlow<List<BleMeshPacketEntity>> = repository.allMeshPackets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routes: StateFlow<List<RouteEntity>> = repository.allRoutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weatherForecasts: StateFlow<List<com.example.data.model.WeatherForecastEntity>> = repository.allWeatherForecasts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itineraries: StateFlow<List<com.example.data.model.ItineraryEntity>> = repository.allItineraries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeItinerary: StateFlow<com.example.data.model.ItineraryEntity?> = repository.activeItinerary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentLocation: StateFlow<LocationLogEntity> = repository.currentLocation
    val batteryLevel: StateFlow<Int> = repository.batteryLevel
    val isSimulatedInactivity: StateFlow<Boolean> = repository.isSimulatedInactivity
    val isSimulatedOffRoute: StateFlow<Boolean> = repository.isSimulatedOffRoute
    val activeSosAlert: StateFlow<IncidentEntity?> = repository.activeSosAlert
    val aiAdvisory: StateFlow<AiSafetyAdvisory?> = repository.aiAdvisory
    val isGeneratingAiAdvisory: StateFlow<Boolean> = repository.isGeneratingAiAdvisory

    val riskAnalysis: StateFlow<RiskAnalysisResult> = repository.riskAnalysis
    val meshEvents: StateFlow<List<BleMeshRelayEvent>> = repository.bleMeshEngine.meshEvents
    val activePeers: StateFlow<List<MeshPeerNode>> = repository.bleMeshEngine.activePeers

    val isSimulatedOffline: StateFlow<Boolean> = repository.syncEngine.isSimulatedOffline
    val isSyncing: StateFlow<Boolean> = repository.syncEngine.isSyncing
    val lastSyncTimestamp: StateFlow<Long?> = repository.syncEngine.lastSyncTimestamp
    val syncMessage: StateFlow<String> = repository.syncEngine.syncMessage
    val pendingSyncCount: StateFlow<Int> = repository.pendingSyncCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleOfflineMode() {
        repository.syncEngine.toggleOfflineSimulation()
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            repository.syncEngine.triggerSync()
        }
    }

    fun downloadWeatherForecast(regionId: String) {
        viewModelScope.launch {
            repository.downloadWeatherForRegion(regionId)
        }
    }

    fun refreshWeatherTelemetry() {
        viewModelScope.launch {
            repository.refreshAllWeatherForecasts()
        }
    }

    fun createItinerary(
        title: String,
        description: String,
        startName: String,
        startLat: Double,
        startLng: Double,
        endName: String,
        endLat: Double,
        endLng: Double,
        waypoints: List<com.example.data.model.WaypointItem>,
        transportMode: com.example.data.model.TransportMode,
        durationMinutes: Int,
        difficulty: String,
        setAsActive: Boolean = true
    ) {
        viewModelScope.launch {
            val id = "ITIN-" + java.util.UUID.randomUUID().toString().take(8).uppercase()
            val waypointsJson = com.example.data.engine.ItineraryEngine.serializeWaypoints(waypoints)
            val entity = com.example.data.model.ItineraryEntity(
                itineraryId = id,
                touristId = tourist.value?.touristId ?: "T-8492",
                title = title,
                description = description,
                startPointName = startName,
                startLat = startLat,
                startLng = startLng,
                endPointName = endName,
                endLat = endLat,
                endLng = endLng,
                waypointsJson = waypointsJson,
                modeOfTransport = transportMode,
                expectedDurationMinutes = durationMinutes,
                plannedDepartureTime = System.currentTimeMillis(),
                plannedArrivalTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L),
                difficultyLevel = difficulty,
                isActive = setAsActive
            )
            repository.saveItinerary(entity)
            if (setAsActive) {
                repository.setActiveItinerary(id)
            }
        }
    }

    fun selectActiveItinerary(itineraryId: String) {
        viewModelScope.launch {
            repository.setActiveItinerary(itineraryId)
        }
    }

    fun deactivateItinerary() {
        viewModelScope.launch {
            repository.deactivateActiveItinerary()
        }
    }

    fun deleteItinerary(itineraryId: String) {
        viewModelScope.launch {
            repository.deleteItinerary(itineraryId)
        }
    }


    fun triggerSos(type: EmergencyType, message: String = "EMERGENCY: Tourist needs immediate assistance.") {
        viewModelScope.launch {
            repository.triggerEmergencySos(type, message)
        }
    }

    fun cancelSos() {
        repository.cancelActiveSos()
    }

    fun requestAiSafetyCheck() {
        repository.requestAiAdvisory()
    }

    fun simulateMoveTo(lat: Double, lng: Double, alt: Double = 2240.0) {
        repository.simulateMoveToCoordinate(lat, lng, alt)
    }

    fun toggleInactivity() {
        repository.toggleInactivitySimulation()
    }

    fun toggleOffRoute() {
        repository.toggleOffRouteSimulation()
    }

    fun setBattery(percent: Int) {
        repository.setBatteryLevel(percent)
    }

    fun simulateMeshRelayHop(packet: BleMeshPacketEntity, peer: MeshPeerNode) {
        viewModelScope.launch {
            repository.bleMeshEngine.simulateStoreAndForwardHop(packet, peer)
        }
    }

    fun addSimulatedPeer(name: String, role: String, hasNet: Boolean) {
        repository.bleMeshEngine.addSimulatedPeer(name, role, hasNet)
    }

    fun updateTouristProfile(name: String, phone: String, blood: String, hotel: String) {
        viewModelScope.launch {
            val current = tourist.value ?: TouristEntity()
            val newHash = DigitalIdManager.generateSha256Hash("$name|$phone|$blood|$hotel|${System.currentTimeMillis()}")
            val updated = current.copy(
                fullName = name,
                emergencyContactPhone = phone,
                bloodGroup = blood,
                currentHotel = hotel,
                blockchainTxHash = "0x" + newHash,
                credentialSignature = "SIG_ED25519_" + newHash.take(24)
            )
            repository.updateTouristProfile(updated)
        }
    }

    fun acknowledgeIncident(incidentId: String, note: String = "Acknowledged by Nilgiris Forest Patrol") {
        viewModelScope.launch {
            repository.acknowledgeIncident(incidentId, note)
        }
    }

    fun dispatchRescueTeam(incidentId: String, note: String = "Mountain Rescue Unit Alpha Dispatched with Medical Kit") {
        viewModelScope.launch {
            repository.dispatchRescueTeam(incidentId, note)
        }
    }
}
