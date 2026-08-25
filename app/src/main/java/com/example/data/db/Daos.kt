package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BleMeshPacketEntity
import com.example.data.model.GeoFenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.RouteEntity
import com.example.data.model.SyncStatus
import com.example.data.model.TouristEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TouristDao {
    @Query("SELECT * FROM tourists LIMIT 1")
    fun getTouristFlow(): Flow<TouristEntity?>

    @Query("SELECT * FROM tourists WHERE touristId = :id")
    suspend fun getTouristById(id: String): TouristEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTourist(tourist: TouristEntity)
}

@Dao
interface LocationLogDao {
    @Query("SELECT * FROM location_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLocationLogs(): Flow<List<LocationLogEntity>>

    @Query("SELECT * FROM location_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLocation(): Flow<LocationLogEntity?>

    @Query("SELECT * FROM location_logs WHERE syncStatus != 'SYNCED' ORDER BY timestamp ASC")
    suspend fun getUnsyncedLogs(): List<LocationLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationLog(log: LocationLogEntity): Long

    @Query("UPDATE location_logs SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<Long>, status: SyncStatus)
}

@Dao
interface GeoFenceDao {
    @Query("SELECT * FROM geofences")
    fun getAllGeoFencesFlow(): Flow<List<GeoFenceEntity>>

    @Query("SELECT * FROM geofences")
    suspend fun getAllGeoFences(): List<GeoFenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeoFences(fences: List<GeoFenceEntity>)

    @Query("SELECT * FROM geofences WHERE zoneId = :zoneId")
    suspend fun getGeoFenceById(zoneId: String): GeoFenceEntity?
}

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun getAllIncidentsFlow(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE syncStatus != 'SYNCED' ORDER BY timestamp ASC")
    suspend fun getUnsyncedIncidents(): List<IncidentEntity>

    @Query("SELECT * FROM incidents WHERE incidentId = :id")
    suspend fun getIncidentById(id: String): IncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    @Query("UPDATE incidents SET syncStatus = :status WHERE incidentId = :id")
    suspend fun updateIncidentSyncStatus(id: String, status: SyncStatus)

    @Query("SELECT COUNT(*) FROM incidents WHERE syncStatus != 'SYNCED'")
    fun getPendingSyncCount(): Flow<Int>
}

@Dao
interface BleMeshDao {
    @Query("SELECT * FROM ble_mesh_packets ORDER BY receivedTimestamp DESC")
    fun getAllMeshPacketsFlow(): Flow<List<BleMeshPacketEntity>>

    @Query("SELECT * FROM ble_mesh_packets WHERE incidentId = :incidentId LIMIT 1")
    suspend fun findPacketByIncidentId(incidentId: String): BleMeshPacketEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeshPacket(packet: BleMeshPacketEntity): Long

    @Query("UPDATE ble_mesh_packets SET isDeliveredToCloud = 1, forwardedTimestamp = :forwardTime WHERE packetId = :packetId")
    suspend fun markPacketDelivered(packetId: String, forwardTime: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM ble_mesh_packets")
    fun getMeshPacketCountFlow(): Flow<Int>
}

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes")
    fun getAllRoutesFlow(): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)

    @Query("SELECT * FROM routes WHERE routeId = :routeId")
    suspend fun getRouteById(routeId: String): RouteEntity?
}

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_forecasts")
    fun getAllWeatherForecastsFlow(): Flow<List<com.example.data.model.WeatherForecastEntity>>

    @Query("SELECT * FROM weather_forecasts")
    suspend fun getAllWeatherForecasts(): List<com.example.data.model.WeatherForecastEntity>

    @Query("SELECT * FROM weather_forecasts WHERE regionId = :regionId")
    suspend fun getWeatherForecastByRegion(regionId: String): com.example.data.model.WeatherForecastEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecasts(forecasts: List<com.example.data.model.WeatherForecastEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: com.example.data.model.WeatherForecastEntity)

    @Query("UPDATE weather_forecasts SET isDownloadedOffline = :downloaded, downloadedAt = :timestamp WHERE regionId = :regionId")
    suspend fun updateDownloadStatus(regionId: String, downloaded: Boolean, timestamp: Long)
}

@Dao
interface ItineraryDao {
    @Query("SELECT * FROM itineraries ORDER BY createdAt DESC")
    fun getAllItinerariesFlow(): Flow<List<com.example.data.model.ItineraryEntity>>

    @Query("SELECT * FROM itineraries WHERE isActive = 1 LIMIT 1")
    fun getActiveItineraryFlow(): Flow<com.example.data.model.ItineraryEntity?>

    @Query("SELECT * FROM itineraries WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveItinerary(): com.example.data.model.ItineraryEntity?

    @Query("SELECT * FROM itineraries WHERE itineraryId = :id")
    suspend fun getItineraryById(id: String): com.example.data.model.ItineraryEntity?

    @Query("SELECT * FROM itineraries WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedItineraries(): List<com.example.data.model.ItineraryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItinerary(itinerary: com.example.data.model.ItineraryEntity)

    @Update
    suspend fun updateItinerary(itinerary: com.example.data.model.ItineraryEntity)

    @Query("UPDATE itineraries SET isActive = 0")
    suspend fun deactivateAllItineraries()

    @Query("UPDATE itineraries SET isActive = 1 WHERE itineraryId = :id")
    suspend fun activateItinerary(id: String)

    @Query("UPDATE itineraries SET syncStatus = :status, lastSyncedAt = :timestamp WHERE itineraryId = :id")
    suspend fun updateSyncStatus(id: String, status: com.example.data.model.SyncStatus, timestamp: Long)

    @Query("DELETE FROM itineraries WHERE itineraryId = :id")
    suspend fun deleteItinerary(id: String)
}

