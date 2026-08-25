package com.example.data.engine

import com.example.data.model.LocationLogEntity
import com.example.data.model.WeatherAlertSeverity
import com.example.data.model.WeatherCondition
import com.example.data.model.WeatherForecastEntity
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class WeatherRiskAssessment(
    val activeRegionForecast: WeatherForecastEntity?,
    val approachingSevereRegion: WeatherForecastEntity?,
    val distanceToSevereRegionKm: Double,
    val isInsideSevereWeather: Boolean,
    val isHeadingTowardsSevereWeather: Boolean,
    val weatherRiskScoreContribution: Int,
    val weatherAlertHeadline: String?,
    val weatherSafetyGuidance: String?,
    val weatherFactors: List<String>
)

object WeatherAlertEngine {

    /**
     * Assesses real-time location against offline cached regional weather forecasts.
     * Detects if tourist is inside or heading towards an area with predicted severe weather.
     */
    fun assessWeatherRisk(
        currentLocation: LocationLogEntity,
        forecasts: List<WeatherForecastEntity>
    ): WeatherRiskAssessment {
        if (forecasts.isEmpty()) {
            return WeatherRiskAssessment(
                activeRegionForecast = null,
                approachingSevereRegion = null,
                distanceToSevereRegionKm = 0.0,
                isInsideSevereWeather = false,
                isHeadingTowardsSevereWeather = false,
                weatherRiskScoreContribution = 0,
                weatherAlertHeadline = null,
                weatherSafetyGuidance = null,
                weatherFactors = emptyList()
            )
        }

        var activeRegion: WeatherForecastEntity? = null
        var closestDistanceToActive = Double.MAX_VALUE

        var approachingSevere: WeatherForecastEntity? = null
        var closestSevereDistKm = Double.MAX_VALUE

        val factors = mutableListOf<String>()
        var weatherScore = 0

        for (forecast in forecasts) {
            val distMeters = SafetyRiskEngine.calculateDistanceMeters(
                currentLocation.latitude,
                currentLocation.longitude,
                forecast.centerLat,
                forecast.centerLng
            )
            val distKm = distMeters / 1000.0

            // Inside regional perimeter
            if (distKm <= forecast.radiusKm) {
                if (distKm < closestDistanceToActive) {
                    closestDistanceToActive = distKm
                    activeRegion = forecast
                }
            }

            // Check if severe weather zone
            val isSevere = forecast.severity == WeatherAlertSeverity.WARNING ||
                    forecast.severity == WeatherAlertSeverity.EXTREME_DANGER ||
                    forecast.precipitationMmPerHour >= 25.0 ||
                    forecast.windSpeedKmh >= 40.0

            if (isSevere) {
                if (distKm < closestSevereDistKm) {
                    closestSevereDistKm = distKm
                    // Check if tourist is moving towards this severe zone
                    val bearingToZone = calculateBearing(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        forecast.centerLat,
                        forecast.centerLng
                    )
                    val bearingDiff = abs(bearingToZone - currentLocation.bearingDegrees)
                    val isHeadingTowards = (bearingDiff < 55.0 || bearingDiff > 305.0) && distKm <= (forecast.radiusKm + 4.0)

                    if (distKm <= forecast.radiusKm || isHeadingTowards) {
                        approachingSevere = forecast
                    }
                }
            }
        }

        val isInsideSevere = activeRegion != null && (
                activeRegion.severity == WeatherAlertSeverity.WARNING ||
                activeRegion.severity == WeatherAlertSeverity.EXTREME_DANGER ||
                activeRegion.precipitationMmPerHour >= 25.0
        )

        val isHeadingTowardsSevere = approachingSevere != null && !isInsideSevere && closestSevereDistKm <= ((approachingSevere.radiusKm) + 4.0)

        // Calculate weather hazard score contribution
        if (activeRegion != null) {
            when (activeRegion.severity) {
                WeatherAlertSeverity.EXTREME_DANGER -> {
                    weatherScore += 45
                    factors.add("Extreme Weather Hazard: ${activeRegion.condition.label} (${activeRegion.precipitationMmPerHour}mm/h rain)")
                }
                WeatherAlertSeverity.WARNING -> {
                    weatherScore += 30
                    factors.add("Severe Weather Warning: ${activeRegion.alertHeadline}")
                }
                WeatherAlertSeverity.ADVISORY -> {
                    weatherScore += 15
                    factors.add("Weather Advisory: ${activeRegion.condition.label} (Visibility: ${activeRegion.visibilityMeters}m)")
                }
                WeatherAlertSeverity.NONE -> {
                    if (activeRegion.precipitationMmPerHour > 5.0) {
                        weatherScore += 5
                        factors.add("Light Rain in region (${activeRegion.precipitationMmPerHour}mm/h)")
                    }
                }
            }

            // Severe wind / visibility additions
            if (activeRegion.windSpeedKmh > 35.0) {
                weatherScore += 10
                factors.add("High Mountain Gale Winds (${activeRegion.windSpeedKmh.toInt()} km/h)")
            }
            if (activeRegion.visibilityMeters < 200) {
                weatherScore += 10
                factors.add("Critical Low Visibility (< ${activeRegion.visibilityMeters}m) - Fog/Mist")
            }
        }

        if (isHeadingTowardsSevere && approachingSevere != null) {
            weatherScore += 20
            factors.add("Trajectory Alert: Heading directly towards ${approachingSevere.regionName} Severe Storm Cell (~${String.format("%.1f", closestSevereDistKm)} km ahead)")
        }

        val headline = when {
            isInsideSevere -> activeRegion?.alertHeadline ?: "Severe Weather Hazard Detected"
            isHeadingTowardsSevere -> "⚠️ Heading Towards Severe Weather: ${approachingSevere?.alertHeadline}"
            activeRegion?.severity == WeatherAlertSeverity.ADVISORY -> activeRegion.alertHeadline
            else -> activeRegion?.alertHeadline ?: "Regional Weather Stable"
        }

        val guidance = when {
            isInsideSevere -> activeRegion?.safetyGuidance ?: "Seek immediate storm shelter and avoid open terrain."
            isHeadingTowardsSevere -> "Divert trajectory or hold at nearest safe shelter before entering storm perimeter."
            else -> activeRegion?.safetyGuidance ?: "Maintain regular mountain trail precautions."
        }

        return WeatherRiskAssessment(
            activeRegionForecast = activeRegion,
            approachingSevereRegion = approachingSevere,
            distanceToSevereRegionKm = if (closestSevereDistKm == Double.MAX_VALUE) 0.0 else closestSevereDistKm,
            isInsideSevereWeather = isInsideSevere,
            isHeadingTowardsSevereWeather = isHeadingTowardsSevere,
            weatherRiskScoreContribution = weatherScore.coerceIn(0, 50),
            weatherAlertHeadline = headline,
            weatherSafetyGuidance = guidance,
            weatherFactors = factors
        )
    }

    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLonRad = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLonRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLonRad)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        if (bearing < 0) {
            bearing += 360f
        }
        return bearing
    }
}
