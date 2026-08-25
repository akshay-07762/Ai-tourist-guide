package com.example.data.engine

import com.example.data.model.ItineraryEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.TransportMode
import com.example.data.model.WaypointItem
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.sqrt

data class RouteDeviationAnalysis(
    val isOffRoute: Boolean,
    val deviationDistanceMeters: Double,
    val nearestWaypointName: String?,
    val distanceToNearestWaypointMeters: Double,
    val remainingDistanceKm: Double,
    val estimatedArrivalTimestamp: Long,
    val deviationSeverity: String, // "NORMAL", "MINOR_DRIFT", "SIGNIFICANT_DEVIATION", "CRITICAL_LOST"
    val completedWaypointsCount: Int,
    val totalWaypointsCount: Int
)

object ItineraryEngine {

    fun parseWaypoints(jsonString: String): List<WaypointItem> {
        val list = mutableListOf<WaypointItem>()
        if (jsonString.isBlank() || jsonString == "[]") return list
        try {
            val arr = JSONArray(jsonString)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    WaypointItem(
                        id = obj.optString("id", "WP-$i"),
                        name = obj.optString("name", "Waypoint #${i + 1}"),
                        latitude = obj.optDouble("latitude", obj.optDouble("lat", 0.0)),
                        longitude = obj.optDouble("longitude", obj.optDouble("lng", 0.0)),
                        altitudeMeters = obj.optDouble("altitudeMeters", 2200.0),
                        stayDurationMinutes = obj.optInt("stayDurationMinutes", 15),
                        orderIndex = obj.optInt("orderIndex", i)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeWaypoints(waypoints: List<WaypointItem>): String {
        val arr = JSONArray()
        for (wp in waypoints) {
            val obj = JSONObject().apply {
                put("id", wp.id)
                put("name", wp.name)
                put("latitude", wp.latitude)
                put("longitude", wp.longitude)
                put("altitudeMeters", wp.altitudeMeters)
                put("stayDurationMinutes", wp.stayDurationMinutes)
                put("orderIndex", wp.orderIndex)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    /**
     * Calculates route deviation by measuring minimum distance to polyline formed by Start -> Waypoints -> End.
     */
    fun analyzeDeviation(
        currentLocation: LocationLogEntity,
        itinerary: ItineraryEntity?
    ): RouteDeviationAnalysis {
        if (itinerary == null || !itinerary.isActive) {
            return RouteDeviationAnalysis(
                isOffRoute = currentLocation.isOffRoute,
                deviationDistanceMeters = if (currentLocation.isOffRoute) 380.0 else 0.0,
                nearestWaypointName = null,
                distanceToNearestWaypointMeters = 0.0,
                remainingDistanceKm = 0.0,
                estimatedArrivalTimestamp = System.currentTimeMillis(),
                deviationSeverity = if (currentLocation.isOffRoute) "SIGNIFICANT_DEVIATION" else "NORMAL",
                completedWaypointsCount = 0,
                totalWaypointsCount = 0
            )
        }

        val waypoints = parseWaypoints(itinerary.waypointsJson)
        val polylinePoints = mutableListOf<Pair<Double, Double>>()
        polylinePoints.add(Pair(itinerary.startLat, itinerary.startLng))
        waypoints.sortedBy { it.orderIndex }.forEach {
            polylinePoints.add(Pair(it.latitude, it.longitude))
        }
        polylinePoints.add(Pair(itinerary.endLat, itinerary.endLng))

        var minDistanceToPolyline = Double.MAX_VALUE
        for (i in 0 until polylinePoints.size - 1) {
            val p1 = polylinePoints[i]
            val p2 = polylinePoints[i + 1]
            val dist = distanceToSegmentMeters(
                currentLocation.latitude,
                currentLocation.longitude,
                p1.first,
                p1.second,
                p2.first,
                p2.second
            )
            if (dist < minDistanceToPolyline) {
                minDistanceToPolyline = dist
            }
        }

        // Find nearest waypoint
        var nearestWp: WaypointItem? = null
        var minWpDist = Double.MAX_VALUE
        var completedCount = 0
        for (wp in waypoints) {
            val dist = SafetyRiskEngine.calculateDistanceMeters(
                currentLocation.latitude,
                currentLocation.longitude,
                wp.latitude,
                wp.longitude
            )
            if (dist < minWpDist) {
                minWpDist = dist
                nearestWp = wp
            }
            if (dist < 120.0) {
                completedCount++
            }
        }

        // Distance to destination
        val distToEndMeters = SafetyRiskEngine.calculateDistanceMeters(
            currentLocation.latitude,
            currentLocation.longitude,
            itinerary.endLat,
            itinerary.endLng
        )
        val remainingDistKm = distToEndMeters / 1000.0

        val speed = itinerary.modeOfTransport.averageSpeedKmh.coerceAtLeast(1.0f)
        val hoursRemaining = remainingDistKm / speed
        val etaMillis = System.currentTimeMillis() + (hoursRemaining * 3600 * 1000L).toLong()

        // Threshold based on transport mode (Hiking has tighter corridor, 4x4 allows wider corridor)
        val allowedCorridorMeters = when (itinerary.modeOfTransport) {
            TransportMode.HIKING_TREK -> 180.0
            TransportMode.MOUNTAIN_BIKE -> 250.0
            TransportMode.MOUNTAIN_4X4 -> 450.0
            TransportMode.SHUTTLE_BUS -> 350.0
            TransportMode.CABLE_CAR -> 120.0
        }

        val isOffRoute = (minDistanceToPolyline > allowedCorridorMeters) || currentLocation.isOffRoute

        val severity = when {
            minDistanceToPolyline > allowedCorridorMeters * 3 -> "CRITICAL_LOST"
            minDistanceToPolyline > allowedCorridorMeters -> "SIGNIFICANT_DEVIATION"
            minDistanceToPolyline > allowedCorridorMeters * 0.7 -> "MINOR_DRIFT"
            else -> "NORMAL"
        }

        return RouteDeviationAnalysis(
            isOffRoute = isOffRoute,
            deviationDistanceMeters = minDistanceToPolyline,
            nearestWaypointName = nearestWp?.name ?: itinerary.endPointName,
            distanceToNearestWaypointMeters = if (minWpDist == Double.MAX_VALUE) distToEndMeters else minWpDist,
            remainingDistanceKm = remainingDistKm,
            estimatedArrivalTimestamp = etaMillis,
            deviationSeverity = severity,
            completedWaypointsCount = completedCount,
            totalWaypointsCount = waypoints.size
        )
    }

    /**
     * Distance from point P to line segment AB in meters.
     */
    private fun distanceToSegmentMeters(
        pLat: Double, pLng: Double,
        aLat: Double, aLng: Double,
        bLat: Double, bLng: Double
    ): Double {
        val abDist = SafetyRiskEngine.calculateDistanceMeters(aLat, aLng, bLat, bLng)
        if (abDist < 1.0) {
            return SafetyRiskEngine.calculateDistanceMeters(pLat, pLng, aLat, aLng)
        }

        val apDist = SafetyRiskEngine.calculateDistanceMeters(aLat, aLng, pLat, pLng)
        val bpDist = SafetyRiskEngine.calculateDistanceMeters(bLat, bLng, pLat, pLng)

        // Projection check (dot product approx)
        val latDiff = bLat - aLat
        val lngDiff = bLng - aLng
        val t = (((pLat - aLat) * latDiff) + ((pLng - aLng) * lngDiff)) / ((latDiff * latDiff) + (lngDiff * lngDiff))

        return when {
            t <= 0.0 -> apDist
            t >= 1.0 -> bpDist
            else -> {
                val projLat = aLat + (t * latDiff)
                val projLng = aLng + (t * lngDiff)
                SafetyRiskEngine.calculateDistanceMeters(pLat, pLng, projLat, projLng)
            }
        }
    }
}
