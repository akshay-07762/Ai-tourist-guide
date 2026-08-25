package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.ItineraryEngine
import com.example.data.engine.WeatherAlertEngine
import com.example.data.model.ItineraryEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.TransportMode
import com.example.data.model.WaypointItem
import com.example.data.model.WeatherAlertSeverity
import com.example.data.model.WeatherCondition
import com.example.data.model.WeatherForecastEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SafeTour Guard", appName)
    }

    @Test
    fun `test offline weather alert assessment for severe thunderstorm`() {
        val severeForecast = WeatherForecastEntity(
            regionId = "AVALANCHE_BASIN",
            regionName = "Avalanche Lake & Mukurthi Basin",
            condition = WeatherCondition.THUNDERSTORM_LIGHTNING,
            severity = WeatherAlertSeverity.EXTREME_DANGER,
            temperatureC = 8.5,
            feelsLikeC = 4.0,
            precipitationMmPerHour = 45.0,
            windSpeedKmh = 52.0,
            visibilityMeters = 150,
            humidityPercent = 98,
            alertHeadline = "Cloudburst & Flash Flood Threat",
            alertDetails = "Severe torrential cloudburst active over Avalanche ridge.",
            safetyGuidance = "Halt valley trekking. Move to elevated emergency shelters immediately.",
            centerLat = 11.3120,
            centerLng = 76.6180,
            radiusKm = 6.0
        )

        val locInside = LocationLogEntity(
            touristId = "T-8492",
            latitude = 11.3125,
            longitude = 76.6185,
            altitudeMeters = 2240.0,
            speedKmh = 3.5f,
            bearingDegrees = 180f,
            accuracyMeters = 8.0f
        )

        val assessment = WeatherAlertEngine.assessWeatherRisk(
            currentLocation = locInside,
            forecasts = listOf(severeForecast)
        )

        assertTrue("Should detect tourist is inside severe weather region", assessment.isInsideSevereWeather)
        assertEquals(WeatherAlertSeverity.EXTREME_DANGER, assessment.activeRegionForecast?.severity)
        assertTrue(assessment.weatherRiskScoreContribution >= 40)
    }

    @Test
    fun `test itinerary route deviation calculation`() {
        val waypoints = listOf(
            WaypointItem("WP-1", "Doddabetta Summit", 11.4102, 76.6950, 2600.0, 20, 0),
            WaypointItem("WP-2", "Avalanche Lake Outpost", 11.3120, 76.6180, 2100.0, 30, 1)
        )
        val waypointsJson = ItineraryEngine.serializeWaypoints(waypoints)

        val itinerary = ItineraryEntity(
            itineraryId = "ITIN-TEST-1",
            touristId = "T-8492",
            title = "Doddabetta to Avalanche",
            description = "High altitude trek",
            startPointName = "Doddabetta",
            startLat = 11.4102,
            startLng = 76.6950,
            endPointName = "Avalanche",
            endLat = 11.3120,
            endLng = 76.6180,
            waypointsJson = waypointsJson,
            modeOfTransport = TransportMode.HIKING_TREK,
            isActive = true
        )

        // Point right on start
        val onRouteLoc = LocationLogEntity(
            touristId = "T-8492",
            latitude = 11.4102,
            longitude = 76.6950,
            altitudeMeters = 2600.0,
            speedKmh = 4.0f,
            bearingDegrees = 200f,
            accuracyMeters = 5.0f
        )

        val onRouteDeviation = ItineraryEngine.analyzeDeviation(onRouteLoc, itinerary)
        assertFalse("Should not be off route when at trailhead", onRouteDeviation.isOffRoute)
        assertTrue("Deviation distance should be near 0m", onRouteDeviation.deviationDistanceMeters < 50.0)

        // Point far off route (10km east)
        val offRouteLoc = LocationLogEntity(
            touristId = "T-8492",
            latitude = 11.4102,
            longitude = 76.8500,
            altitudeMeters = 1800.0,
            speedKmh = 2.0f,
            bearingDegrees = 90f,
            accuracyMeters = 10.0f
        )

        val offRouteDeviation = ItineraryEngine.analyzeDeviation(offRouteLoc, itinerary)
        assertTrue("Should detect off route when kilometers away from trail", offRouteDeviation.isOffRoute)
        assertTrue("Deviation distance should be large", offRouteDeviation.deviationDistanceMeters > 500.0)
    }
}
