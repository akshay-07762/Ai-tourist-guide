package com.example.data.engine

import com.example.data.model.GeoFenceEntity
import com.example.data.model.ItineraryEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.RiskLevel
import com.example.data.model.WeatherForecastEntity
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class RiskAnalysisResult(
    val riskScore: Int, // 0 to 100
    val riskLevel: RiskLevel,
    val activeWarningZone: GeoFenceEntity?,
    val activeSafeShelter: GeoFenceEntity?,
    val distanceToNearestShelterMeters: Double,
    val isOffRoute: Boolean,
    val isInactivityAlert: Boolean,
    val riskFactors: List<String>,
    val recommendedAction: String,
    val safetySummary: String,
    val weatherAssessment: WeatherRiskAssessment? = null,
    val routeDeviation: RouteDeviationAnalysis? = null
)

object SafetyRiskEngine {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Haversine formula for exact distance between two GPS coordinates in meters.
     */
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Analyzes tourist state against geofences, movement trajectory, weather forecasts, and itinerary route.
     */
    fun evaluateRisk(
        currentLocation: LocationLogEntity,
        previousLogs: List<LocationLogEntity>,
        geoFences: List<GeoFenceEntity>,
        batteryPercent: Int = 82,
        isSimulatedInactivity: Boolean = false,
        isSimulatedDeviatedRoute: Boolean = false,
        weatherForecasts: List<WeatherForecastEntity> = emptyList(),
        activeItinerary: ItineraryEntity? = null
    ): RiskAnalysisResult {
        var baseScore = 5
        val factors = mutableListOf<String>()

        var matchedDangerZone: GeoFenceEntity? = null
        var matchedSafeShelter: GeoFenceEntity? = null
        var nearestShelterDistance = Double.MAX_VALUE

        // 1. Geo-Fence Evaluation
        for (fence in geoFences) {
            val dist = calculateDistanceMeters(
                currentLocation.latitude,
                currentLocation.longitude,
                fence.centerLat,
                fence.centerLng
            )

            if (fence.isSafeZone) {
                if (dist < nearestShelterDistance) {
                    nearestShelterDistance = dist
                }
                if (dist <= fence.radiusMeters) {
                    matchedSafeShelter = fence
                }
            } else {
                if (dist <= fence.radiusMeters) {
                    matchedDangerZone = fence
                    when (fence.riskLevel) {
                        RiskLevel.CRITICAL -> {
                            baseScore += 55
                            factors.add("Breached Critical Hazard Zone: ${fence.name}")
                        }
                        RiskLevel.HIGH -> {
                            baseScore += 40
                            factors.add("Inside High-Risk Boundary: ${fence.name}")
                        }
                        RiskLevel.MEDIUM -> {
                            baseScore += 25
                            factors.add("Near Moderate Danger Zone: ${fence.name}")
                        }
                        else -> {
                            baseScore += 10
                            factors.add("Inside Monitored Zone: ${fence.name}")
                        }
                    }
                } else if (dist <= fence.radiusMeters + 150) {
                    baseScore += 15
                    factors.add("Perimeter Warning: within 150m of ${fence.name}")
                }
            }
        }

        // 2. Itinerary & Dynamic Route Deviation Analysis
        val deviationAnalysis = ItineraryEngine.analyzeDeviation(
            currentLocation = if (isSimulatedDeviatedRoute) currentLocation.copy(isOffRoute = true) else currentLocation,
            itinerary = activeItinerary
        )
        val isOffRoute = isSimulatedDeviatedRoute || deviationAnalysis.isOffRoute || currentLocation.isOffRoute
        if (isOffRoute) {
            val devMeters = if (deviationAnalysis.deviationDistanceMeters > 0) deviationAnalysis.deviationDistanceMeters.toInt() else 380
            baseScore += when (deviationAnalysis.deviationSeverity) {
                "CRITICAL_LOST" -> 40
                "SIGNIFICANT_DEVIATION" -> 28
                "MINOR_DRIFT" -> 15
                else -> 25
            }
            factors.add("Route Deviation: ${devMeters}m off planned ${activeItinerary?.title ?: "trail"}")
        }

        // 3. Offline Weather Hazard Assessment
        val weatherAssessment = WeatherAlertEngine.assessWeatherRisk(currentLocation, weatherForecasts)
        if (weatherAssessment.weatherRiskScoreContribution > 0) {
            baseScore += weatherAssessment.weatherRiskScoreContribution
            factors.addAll(weatherAssessment.weatherFactors)
        }

        // 4. Movement & Inactivity Anomaly Detection
        val isInactivity = isSimulatedInactivity || (currentLocation.speedKmh < 0.5f && previousLogs.size >= 5)
        if (isInactivity && matchedDangerZone != null) {
            baseScore += 20
            factors.add("Stationary in hazard zone for > 15 mins (Potential Fall/Accident)")
        } else if (isInactivity && (baseScore > 20 || weatherAssessment.isInsideSevereWeather)) {
            baseScore += 15
            factors.add("No movement detected during severe weather condition")
        }

        // 5. Environmental & Device Telemetry
        if (batteryPercent < 20) {
            baseScore += 10
            factors.add("Critical Battery ($batteryPercent%) in remote sector")
        }

        // 6. Altitude / Mountain Slope Hazard
        if (currentLocation.altitudeMeters > 2400) {
            baseScore += 5
            factors.add("High Altitude (> 2,400m) - Rapid Hypothermia & Fog Risk")
        }

        // Safe shelter mitigation
        if (matchedSafeShelter != null) {
            baseScore = (baseScore - 30).coerceAtLeast(0)
            factors.add("Sheltered at: ${matchedSafeShelter.name}")
        }

        val clampedScore = baseScore.coerceIn(0, 100)
        val riskLevel = when {
            clampedScore >= 85 -> RiskLevel.CRITICAL
            clampedScore >= 65 -> RiskLevel.HIGH
            clampedScore >= 40 -> RiskLevel.MEDIUM
            clampedScore >= 20 -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }

        val action = when {
            riskLevel == RiskLevel.CRITICAL -> "IMMEDIATE ACTION: Turn back or trigger Emergency SOS. Move towards ${matchedSafeShelter?.name ?: "nearest Ranger post"}."
            weatherAssessment.isInsideSevereWeather -> "WEATHER ALERT: Extreme localized storm. Cease ridge progression and seek solid shelter."
            weatherAssessment.isHeadingTowardsSevereWeather -> "WEATHER WARNING: Heading towards storm cell. Halt forward trek or seek nearby checkpoint."
            isOffRoute -> "CAUTION: Deviated ${deviationAnalysis.deviationDistanceMeters.toInt()}m from planned path. Re-align with trail markers."
            riskLevel == RiskLevel.HIGH -> "CAUTION: Return to designated marked path immediately. Avoid steep slopes."
            riskLevel == RiskLevel.MEDIUM -> "ADVISORY: Terrain risk elevated. Maintain group contact and check trail markers."
            riskLevel == RiskLevel.LOW -> "NORMAL: Proceed on trail, stay mindful of daylight hours."
            else -> "SECURE: Inside verified safe area with emergency infrastructure."
        }

        val summary = when {
            riskLevel == RiskLevel.CRITICAL -> "Critical safety threat detected. Emergency response channel on standby."
            weatherAssessment.isInsideSevereWeather -> "Severe weather hazard active in current sector (${weatherAssessment.weatherAlertHeadline})."
            isOffRoute -> "Route deviation detected from planned itinerary."
            riskLevel == RiskLevel.HIGH -> "High safety risk detected due to terrain & geofence violation."
            riskLevel == RiskLevel.MEDIUM -> "Moderate caution advised on current hill station route."
            riskLevel == RiskLevel.LOW -> "Low risk. Normal route progression."
            else -> "All safety telemetry normal. You are in a protected zone."
        }

        return RiskAnalysisResult(
            riskScore = clampedScore,
            riskLevel = riskLevel,
            activeWarningZone = matchedDangerZone,
            activeSafeShelter = matchedSafeShelter,
            distanceToNearestShelterMeters = if (nearestShelterDistance == Double.MAX_VALUE) 0.0 else nearestShelterDistance,
            isOffRoute = isOffRoute,
            isInactivityAlert = isInactivity,
            riskFactors = if (factors.isEmpty()) listOf("Normal route progression", "Stable GPS fix") else factors,
            recommendedAction = action,
            safetySummary = summary,
            weatherAssessment = weatherAssessment,
            routeDeviation = deviationAnalysis
        )
    }
}

