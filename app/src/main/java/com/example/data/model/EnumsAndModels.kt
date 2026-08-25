package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RiskLevel(val displayName: String, val scoreWeight: Int) {
    SAFE("SAFE", 0),
    LOW("LOW RISK", 25),
    MEDIUM("MEDIUM RISK", 50),
    HIGH("HIGH RISK", 75),
    CRITICAL("CRITICAL DANGER", 95)
}

enum class SyncStatus {
    PENDING,
    UPLOADING,
    SYNCED,
    FAILED_RETRY
}

enum class EmergencyType(val label: String, val iconName: String) {
    SOS_MANUAL("Manual SOS Alarm", "alarm"),
    MEDICAL("Medical Emergency", "medical_services"),
    LOST_STRANDED("Lost / Off Route", "explore_off"),
    LANDSLIDE_DISASTER("Landslide / Natural Hazard", "landslide"),
    WILD_ANIMAL("Wild Animal Threat", "pets"),
    GEOFENCE_BREACH("High-Risk Zone Breach", "warning"),
    INACTIVITY_TRIGGER("No Movement Detected", "timer_off")
}

enum class CommunicationChannel(val label: String) {
    INTERNET_DIRECT("Direct Cloud / 4G"),
    BLE_STORE_FORWARD("BLE Mesh Store-and-Forward"),
    SMS_EMERGENCY("Cellular / Emergency SMS"),
    SATELLITE_BUFFER("Satellite Emergency Queue")
}

enum class ResponderStatus {
    OPEN,
    ACKNOWLEDGED,
    RESCUE_DISPATCHED,
    MEDICAL_EN_ROUTE,
    RESOLVED
}

@Entity(tableName = "tourists")
data class TouristEntity(
    @PrimaryKey val touristId: String = "T-8492",
    val credentialId: String = "DID:SAFE:IN-88294",
    val fullName: String = "Alex Chen",
    val nationality: String = "International / Visitor",
    val emergencyContactName: String = "Sarah Chen (Spouse)",
    val emergencyContactPhone: String = "+91 98765 43210",
    val bloodGroup: String = "O+ Positive",
    val currentHotel: String = "Hillside Heritage Lodge, Ooty",
    val isVerified: Boolean = true,
    val blockchainTxHash: String = "0x7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
    val credentialSignature: String = "SIG_ED25519_a83f9e2b4c10d7a982...",
    val registeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "location_logs")
data class LocationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val touristId: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val speedKmh: Float,
    val bearingDegrees: Float,
    val accuracyMeters: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val currentZoneId: String? = null,
    val isOffRoute: Boolean = false,
    val riskScore: Int = 10
)

@Entity(tableName = "geofences")
data class GeoFenceEntity(
    @PrimaryKey val zoneId: String,
    val name: String,
    val description: String,
    val category: String, // LANDSLIDE, WILDLIFE, CLIFF_EDGE, SAFE_SHELTER, MEDICAL_CAMP, RESTRICTED_FOREST
    val riskLevel: RiskLevel,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Double,
    val warningMessage: String,
    val safeActionAdvice: String,
    val isSafeZone: Boolean = false
)

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val incidentId: String,
    val touristId: String,
    val touristName: String,
    val emergencyType: EmergencyType,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val riskLevel: RiskLevel,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val relayedVia: CommunicationChannel = CommunicationChannel.BLE_STORE_FORWARD,
    val hopCount: Int = 0,
    val batteryPercentage: Int = 78,
    val distressMessage: String,
    val responderStatus: ResponderStatus = ResponderStatus.OPEN,
    val responderNotes: String? = null,
    val digitalSignature: String
)

@Entity(tableName = "ble_mesh_packets")
data class BleMeshPacketEntity(
    @PrimaryKey val packetId: String,
    val incidentId: String,
    val sourceTouristId: String,
    val targetBroadcast: String = "ALL_NODES",
    val currentHop: Int,
    val maxHops: Int = 10,
    val ttlMinutes: Int = 30,
    val creationTimestamp: Long,
    val receivedTimestamp: Long = System.currentTimeMillis(),
    val forwardedTimestamp: Long? = null,
    val relayedByNodeId: String = "PEER_TOURIST_NODE_B",
    val payloadSummary: String,
    val isDeliveredToCloud: Boolean = false,
    val signatureValid: Boolean = true
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val routeId: String,
    val regionName: String,
    val routeTitle: String,
    val difficulty: String,
    val totalDistanceKm: Double,
    val estDurationHours: Double,
    val safeWaypointsJson: String,
    val hazardWarningsJson: String
)

enum class TransportMode(val displayName: String, val iconName: String, val averageSpeedKmh: Float) {
    HIKING_TREK("Hiking / Trekking", "hiking", 4.0f),
    MOUNTAIN_4X4("Mountain 4x4 / Jeep", "directions_car", 25.0f),
    MOUNTAIN_BIKE("Mountain Biking", "directions_bike", 14.0f),
    SHUTTLE_BUS("Eco Forest Shuttle", "directions_bus", 30.0f),
    CABLE_CAR("Ropeway / Cable Car", "cable", 18.0f)
}

enum class WeatherAlertSeverity(val displayName: String, val riskWeight: Int) {
    NONE("Safe / Clear", 0),
    ADVISORY("Weather Advisory", 15),
    WARNING("Severe Weather Warning", 35),
    EXTREME_DANGER("Extreme Danger / Flash Flood", 60)
}

enum class WeatherCondition(val label: String, val iconName: String) {
    CLEAR_SUNNY("Clear & Sunny", "sunny"),
    MIST_FOG("Heavy Mountain Fog", "foggy"),
    LIGHT_RAIN("Light Drizzle / Rain", "rain"),
    HEAVY_DOWNPOUR("Heavy Downpour / Cloudburst", "thunderstorm"),
    THUNDERSTORM_LIGHTNING("Thunderstorm & Lightning", "flash_on"),
    LANDSLIDE_WATCH("Landslide Hazard Rain", "landslide"),
    GALE_WINDS("High Gale Winds & Cold Snap", "air")
}

@Entity(tableName = "weather_forecasts")
data class WeatherForecastEntity(
    @PrimaryKey val regionId: String,
    val regionName: String,
    val condition: WeatherCondition,
    val severity: WeatherAlertSeverity,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val precipitationMmPerHour: Double,
    val windSpeedKmh: Double,
    val visibilityMeters: Int,
    val humidityPercent: Int,
    val alertHeadline: String,
    val alertDetails: String,
    val safetyGuidance: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusKm: Double,
    val isDownloadedOffline: Boolean = true,
    val downloadedAt: Long = System.currentTimeMillis(),
    val validUntil: Long = System.currentTimeMillis() + 86400000L
)

data class WaypointItem(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 2200.0,
    val stayDurationMinutes: Int = 15,
    val orderIndex: Int = 0
)

@Entity(tableName = "itineraries")
data class ItineraryEntity(
    @PrimaryKey val itineraryId: String,
    val touristId: String = "T-8492",
    val title: String,
    val description: String,
    val startPointName: String,
    val startLat: Double,
    val startLng: Double,
    val startAltitude: Double = 2240.0,
    val endPointName: String,
    val endLat: Double,
    val endLng: Double,
    val endAltitude: Double = 2240.0,
    val waypointsJson: String = "[]",
    val modeOfTransport: TransportMode = TransportMode.HIKING_TREK,
    val expectedDurationMinutes: Int = 180,
    val plannedDepartureTime: Long = System.currentTimeMillis(),
    val plannedArrivalTime: Long = System.currentTimeMillis() + (180 * 60 * 1000L),
    val totalDistanceKm: Double = 8.5,
    val difficultyLevel: String = "MODERATE",
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

