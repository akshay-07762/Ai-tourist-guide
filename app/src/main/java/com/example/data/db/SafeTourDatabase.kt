package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BleMeshPacketEntity
import com.example.data.model.GeoFenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.ItineraryEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.RiskLevel
import com.example.data.model.RouteEntity
import com.example.data.model.TouristEntity
import com.example.data.model.TransportMode
import com.example.data.model.WeatherAlertSeverity
import com.example.data.model.WeatherCondition
import com.example.data.model.WeatherForecastEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TouristEntity::class,
        LocationLogEntity::class,
        GeoFenceEntity::class,
        IncidentEntity::class,
        BleMeshPacketEntity::class,
        RouteEntity::class,
        WeatherForecastEntity::class,
        ItineraryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SafeTourDatabase : RoomDatabase() {
    abstract fun touristDao(): TouristDao
    abstract fun locationLogDao(): LocationLogDao
    abstract fun geoFenceDao(): GeoFenceDao
    abstract fun incidentDao(): IncidentDao
    abstract fun bleMeshDao(): BleMeshDao
    abstract fun routeDao(): RouteDao
    abstract fun weatherDao(): WeatherDao
    abstract fun itineraryDao(): ItineraryDao

    companion object {
        @Volatile
        private var INSTANCE: SafeTourDatabase? = null

        fun getInstance(context: Context): SafeTourDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeTourDatabase::class.java,
                    "safe_tour_database.db"
                ).addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(db: SafeTourDatabase) {
    // 1. Initial Tourist Digital ID
    val tourist = TouristEntity(
        touristId = "T-8492",
        credentialId = "DID:SAFE:IN-88294",
        fullName = "Alex Chen",
        nationality = "Visitor / Hiker",
        emergencyContactName = "Sarah Chen (Spouse)",
        emergencyContactPhone = "+91 98765 43210",
        bloodGroup = "O+ Positive",
        currentHotel = "Highland Haven Resort, Ooty",
        isVerified = true,
        blockchainTxHash = "0x89b1c7a45e09d123e4fa88921b764c9103e5c9b13994d509ff1360170a41bc38",
        credentialSignature = "SIG_ED25519_88a91cf23b49103de9a98e21...",
        registeredAt = System.currentTimeMillis()
    )
    db.touristDao().insertTourist(tourist)

    // 2. Real-World Geo-Fences for Ooty / Nilgiris Hill Station
    val initialGeoFences = listOf(
        GeoFenceEntity(
            zoneId = "GF-OOTY-001",
            name = "Avalanche Emerald Slope - Landslide Prone",
            description = "Unstable scree slope with active landslide risk during rainfall.",
            category = "LANDSLIDE",
            riskLevel = RiskLevel.HIGH,
            centerLat = 11.3142,
            centerLng = 76.5891,
            radiusMeters = 380.0,
            warningMessage = "⚠️ WARNING: High landslide vulnerability zone. Avoid footpaths along lower cliff.",
            safeActionAdvice = "Divert 200m north onto the paved Forestry Trail #4.",
            isSafeZone = false
        ),
        GeoFenceEntity(
            zoneId = "GF-OOTY-002",
            name = "Mukkurthi Tiger Reserve Border - Restricted Zone",
            description = "Protected core forest sanctuary. Strict anti-poaching & elephant corridor.",
            category = "RESTRICTED_FOREST",
            riskLevel = RiskLevel.CRITICAL,
            centerLat = 11.3820,
            centerLng = 76.5410,
            radiusMeters = 500.0,
            warningMessage = "🚨 CRITICAL: Strictly prohibited forest boundary. Heavy fine and severe wildlife hazard.",
            safeActionAdvice = "Immediately turn around towards the Glenmorgan checkpoint.",
            isSafeZone = false
        ),
        GeoFenceEntity(
            zoneId = "GF-OOTY-003",
            name = "Doddabetta Eastern Ridge - Steep Cliff Edge",
            description = "Sudden 400m vertical drop with dense fog and gusty winds.",
            category = "CLIFF_EDGE",
            riskLevel = RiskLevel.HIGH,
            centerLat = 11.4010,
            centerLng = 76.7360,
            radiusMeters = 250.0,
            warningMessage = "⚠️ CAUTION: Unfenced cliff edge with sudden zero-visibility fog.",
            safeActionAdvice = "Stay behind stone barriers and stay on marked observatory stairs.",
            isSafeZone = false
        ),
        GeoFenceEntity(
            zoneId = "GF-OOTY-004",
            name = "Needle Rock Ravine - Wild Elephant Corridor",
            description = "Active migratory corridor for wild elephant herds from Gudalur valley.",
            category = "WILDLIFE",
            riskLevel = RiskLevel.HIGH,
            centerLat = 11.4550,
            centerLng = 76.5120,
            radiusMeters = 420.0,
            warningMessage = "⚠️ ALERT: Elephant corridor active between 4 PM and 8 AM. Trekking prohibited.",
            safeActionAdvice = "Move calmly towards the Forest Ranger Station 600m east.",
            isSafeZone = false
        ),
        GeoFenceEntity(
            zoneId = "GF-OOTY-005",
            name = "Upper Bhavani Ranger Outpost & Emergency Shelter",
            description = "Official 24/7 Forest Ranger Base with satellite comms, first aid, and mountain rescue unit.",
            category = "SAFE_SHELTER",
            riskLevel = RiskLevel.SAFE,
            centerLat = 11.2350,
            centerLng = 76.5380,
            radiusMeters = 300.0,
            warningMessage = "🛡️ SAFE ZONE: Certified Emergency Shelter with satellite emergency phone.",
            safeActionAdvice = "Rest, recharge, and check in with the duty ranger.",
            isSafeZone = true
        ),
        GeoFenceEntity(
            zoneId = "GF-OOTY-006",
            name = "Pykara Lake Medical Camp & Help Desk",
            description = "Tourist Medical Outpost with AED, paramedic team, and emergency ambulance dispatch.",
            category = "MEDICAL_CAMP",
            riskLevel = RiskLevel.SAFE,
            centerLat = 11.4420,
            centerLng = 76.6010,
            radiusMeters = 200.0,
            warningMessage = "🏥 MEDICAL SAFE ZONE: First aid and paramedic support available here.",
            safeActionAdvice = "Seek medical evaluation or hydration assistance.",
            isSafeZone = true
        )
    )
    db.geoFenceDao().insertGeoFences(initialGeoFences)

    // 3. Pre-populate initial location
    val initialLocation = LocationLogEntity(
        touristId = "T-8492",
        latitude = 11.4102,
        longitude = 76.6950,
        altitudeMeters = 2240.0,
        speedKmh = 3.8f,
        bearingDegrees = 45.0f,
        accuracyMeters = 4.5f,
        timestamp = System.currentTimeMillis(),
        riskScore = 12
    )
    db.locationLogDao().insertLocationLog(initialLocation)

    // 4. Pre-populate sample routes
    val initialRoutes = listOf(
        RouteEntity(
            routeId = "ROUTE-NILGIRIS-01",
            regionName = "Ooty - Nilgiris Ridge Trek",
            routeTitle = "Doddabetta to Avalanche Lake Trail",
            difficulty = "Moderate",
            totalDistanceKm = 14.5,
            estDurationHours = 4.5,
            safeWaypointsJson = """[{"name":"Doddabetta Peak","lat":11.4010,"lng":76.7360},{"name":"Botanical Gap","lat":11.4102,"lng":76.6950},{"name":"Pykara Viewpoint","lat":11.4420,"lng":76.6010},{"name":"Avalanche Base","lat":11.3142,"lng":76.5891}]""",
            hazardWarningsJson = """["Dense Fog after 3:00 PM","Slippery Scree slope near mile 6","Elephant crossing zone"]"""
        ),
        RouteEntity(
            routeId = "ROUTE-MANALI-02",
            regionName = "Manali - Rohtang Pass Route",
            routeTitle = "Solang Valley to Anjani Mahadev",
            difficulty = "Challenging",
            totalDistanceKm = 18.2,
            estDurationHours = 6.0,
            safeWaypointsJson = """[{"name":"Solang Valley Base","lat":32.3167,"lng":77.1578},{"name":"Anjani Waterfall","lat":32.3290,"lng":77.1640},{"name":"Dhundi Camp","lat":32.3550,"lng":77.1280}]""",
            hazardWarningsJson = """["Sub-zero temperatures","Rockfall hazard on northern face"]"""
        )
    )
    db.routeDao().insertRoutes(initialRoutes)

    // 5. Offline Weather Forecasts for Downloaded Map Regions
    val initialWeatherForecasts = listOf(
        WeatherForecastEntity(
            regionId = "WEATHER-REG-OOTY",
            regionName = "Ooty Valley & Doddabetta Ridge",
            condition = WeatherCondition.MIST_FOG,
            severity = WeatherAlertSeverity.ADVISORY,
            temperatureC = 14.2,
            feelsLikeC = 12.5,
            precipitationMmPerHour = 2.4,
            windSpeedKmh = 18.5,
            visibilityMeters = 350,
            humidityPercent = 88,
            alertHeadline = "Dense Mountain Mist & Reduced Visibility",
            alertDetails = "Rapidly shifting fog bank rolling over Doddabetta Ridge reducing trail visibility below 350m.",
            safetyGuidance = "Use headlamps and stick to GPS trail markers. Avoid steep ridge edges.",
            centerLat = 11.4080,
            centerLng = 76.7020,
            radiusKm = 6.5,
            isDownloadedOffline = true,
            downloadedAt = System.currentTimeMillis()
        ),
        WeatherForecastEntity(
            regionId = "WEATHER-REG-AVALANCHE",
            regionName = "Avalanche Valley & Emerald Basin",
            condition = WeatherCondition.HEAVY_DOWNPOUR,
            severity = WeatherAlertSeverity.WARNING,
            temperatureC = 12.0,
            feelsLikeC = 9.8,
            precipitationMmPerHour = 34.0,
            windSpeedKmh = 38.0,
            visibilityMeters = 180,
            humidityPercent = 96,
            alertHeadline = "Severe Downpour & Scree Landslide Hazard",
            alertDetails = "Intense localized cloudburst forecasted. Soil saturation level high across south scree slope.",
            safetyGuidance = "Divert immediately from lower ravines. Seek high ground or Upper Bhavani Forest Shelter.",
            centerLat = 11.3142,
            centerLng = 76.5891,
            radiusKm = 8.0,
            isDownloadedOffline = true,
            downloadedAt = System.currentTimeMillis()
        ),
        WeatherForecastEntity(
            regionId = "WEATHER-REG-PYKARA",
            regionName = "Pykara Waterfalls & Glenmorgan",
            condition = WeatherCondition.LIGHT_RAIN,
            severity = WeatherAlertSeverity.NONE,
            temperatureC = 16.5,
            feelsLikeC = 15.8,
            precipitationMmPerHour = 1.2,
            windSpeedKmh = 12.0,
            visibilityMeters = 1200,
            humidityPercent = 75,
            alertHeadline = "Scattered Light Mountain Showers",
            alertDetails = "Intermittent rain showers. Water currents at normal baseline.",
            safetyGuidance = "Carry waterproof poncho. Stay on timber designated trails.",
            centerLat = 11.4550,
            centerLng = 76.5920,
            radiusKm = 5.0,
            isDownloadedOffline = true,
            downloadedAt = System.currentTimeMillis()
        ),
        WeatherForecastEntity(
            regionId = "WEATHER-REG-MUKURTHI",
            regionName = "Mukurthi National Park Peak",
            condition = WeatherCondition.THUNDERSTORM_LIGHTNING,
            severity = WeatherAlertSeverity.EXTREME_DANGER,
            temperatureC = 10.5,
            feelsLikeC = 7.0,
            precipitationMmPerHour = 48.0,
            windSpeedKmh = 52.0,
            visibilityMeters = 90,
            humidityPercent = 99,
            alertHeadline = "🚨 Severe Thunderstorm & Lightning Hazard",
            alertDetails = "Severe convective storm cell with active ground lightning strikes and flash flood risk along streams.",
            safetyGuidance = "Cease ridge ascents immediately. Descend from exposed peaks and seek certified shelter.",
            centerLat = 11.3820,
            centerLng = 76.5410,
            radiusKm = 9.0,
            isDownloadedOffline = true,
            downloadedAt = System.currentTimeMillis()
        )
    )
    db.weatherDao().insertForecasts(initialWeatherForecasts)

    // 6. Pre-populate active itinerary
    val initialItinerary = ItineraryEntity(
        itineraryId = "ITIN-NILGIRIS-001",
        touristId = "T-8492",
        title = "Ooty Botanical to Avalanche Alpine Circuit",
        description = "High-altitude day trek traversing Ooty Botanical Ridge, Pykara Viewpoint, and terminating at Avalanche Lake Shelter.",
        startPointName = "Ooty Botanical Base (2,240m)",
        startLat = 11.4102,
        startLng = 76.6950,
        startAltitude = 2240.0,
        endPointName = "Avalanche Ranger Post (2,180m)",
        endLat = 11.3142,
        endLng = 76.5891,
        endAltitude = 2180.0,
        waypointsJson = """[{"id":"WP-1","name":"Botanical Pine Gap","latitude":11.4102,"longitude":76.6950,"altitudeMeters":2240.0,"stayDurationMinutes":10,"orderIndex":0},{"id":"WP-2","name":"Pykara Valley Viewpoint","latitude":11.4420,"longitude":76.6010,"altitudeMeters":2190.0,"stayDurationMinutes":25,"orderIndex":1},{"id":"WP-3","name":"Avalanche Forest Shelter","latitude":11.3142,"longitude":76.5891,"altitudeMeters":2180.0,"stayDurationMinutes":60,"orderIndex":2}]""",
        modeOfTransport = TransportMode.HIKING_TREK,
        expectedDurationMinutes = 240,
        plannedDepartureTime = System.currentTimeMillis() - (45 * 60 * 1000L),
        plannedArrivalTime = System.currentTimeMillis() + (195 * 60 * 1000L),
        totalDistanceKm = 12.4,
        difficultyLevel = "MODERATE",
        syncStatus = com.example.data.model.SyncStatus.SYNCED,
        isActive = true,
        createdAt = System.currentTimeMillis() - (2 * 3600 * 1000L),
        lastSyncedAt = System.currentTimeMillis() - (1800 * 1000L)
    )
    db.itineraryDao().insertItinerary(initialItinerary)
}
