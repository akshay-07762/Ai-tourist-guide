package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherAlertSeverity
import com.example.data.model.WeatherCondition
import com.example.data.model.WeatherForecastEntity
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.HighOrange
import com.example.ui.theme.MediumAmber
import com.example.ui.theme.PolishMutedText
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishSubtleBorder
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.viewmodel.SafetyViewModel

@Composable
fun WeatherAlertScreen(
    viewModel: SafetyViewModel,
    modifier: Modifier = Modifier
) {
    val forecasts by viewModel.weatherForecasts.collectAsState()
    val riskAnalysis by viewModel.riskAnalysis.collectAsState()
    val isOffline by viewModel.isSimulatedOffline.collectAsState()
    val weatherAssessment = riskAnalysis.weatherAssessment

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner with Sync / Download Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Offline Weather Radar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isOffline) "Offline Cached Weather Grid (4 Regions Loaded)" else "Live Satellite & Doppler Weather Sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishMutedText
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshWeatherTelemetry() },
                    modifier = Modifier.testTag("refresh_weather_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh weather data",
                        tint = PolishPrimary
                    )
                }
            }
        }

        // Severe Proximity / Trajectory Warning Banner (if approaching or inside severe weather)
        if (weatherAssessment?.isInsideSevereWeather == true || weatherAssessment?.isHeadingTowardsSevereWeather == true) {
            item {
                val isInside = weatherAssessment.isInsideSevereWeather
                val alertColor = if (isInside) AlertRed else HighOrange
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, alertColor, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = alertColor.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(alertColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Severe weather warning",
                                    tint = alertColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isInside) "🚨 SEVERE WEATHER ACTIVE IN SECTOR" else "⚠️ SEVERE STORM TRAJECTORY ALERT",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = alertColor
                                )
                                Text(
                                    text = weatherAssessment.weatherAlertHeadline ?: "Dangerous meteorological change detected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlate
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = weatherAssessment.weatherSafetyGuidance ?: "Avoid exposed ridges, descend from peaks, and move toward certified shelters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PolishMutedText
                        )
                    }
                }
            }
        }

        // Active Sector Weather Card
        item {
            val activeForecast = weatherAssessment?.activeRegionForecast ?: forecasts.firstOrNull()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishSubtleBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT SECTOR FORECAST",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimary
                            )
                            Text(
                                text = activeForecast?.regionName ?: "Nilgiris Mountain Sector",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )
                        }

                        WeatherSeverityChip(severity = activeForecast?.severity ?: WeatherAlertSeverity.NONE)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary metrics grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeatherMetricItem(
                            icon = Icons.Default.DeviceThermostat,
                            label = "Temp",
                            value = "${activeForecast?.temperatureC ?: 14.0}°C",
                            subValue = "Feels ${activeForecast?.feelsLikeC ?: 12.0}°C",
                            color = PolishPrimary
                        )
                        WeatherMetricItem(
                            icon = Icons.Default.WaterDrop,
                            label = "Precipitation",
                            value = "${activeForecast?.precipitationMmPerHour ?: 0.0} mm/h",
                            subValue = if ((activeForecast?.precipitationMmPerHour ?: 0.0) > 20.0) "Heavy Rain" else "Moderate",
                            color = if ((activeForecast?.precipitationMmPerHour ?: 0.0) > 20.0) HighOrange else PolishPrimary
                        )
                        WeatherMetricItem(
                            icon = Icons.Default.Air,
                            label = "Wind Speed",
                            value = "${activeForecast?.windSpeedKmh?.toInt() ?: 18} km/h",
                            subValue = if ((activeForecast?.windSpeedKmh ?: 0.0) > 35.0) "Gale Warning" else "Normal",
                            color = if ((activeForecast?.windSpeedKmh ?: 0.0) > 35.0) AlertRed else PolishPrimary
                        )
                        WeatherMetricItem(
                            icon = Icons.Default.Visibility,
                            label = "Visibility",
                            value = "${activeForecast?.visibilityMeters ?: 1000}m",
                            subValue = if ((activeForecast?.visibilityMeters ?: 1000) < 300) "Dense Fog" else "Clear",
                            color = if ((activeForecast?.visibilityMeters ?: 1000) < 300) HighOrange else SafeGreen
                        )
                    }

                    if (activeForecast != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "AI Mountain Hazard Assessment:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlate
                                )
                                Text(
                                    text = activeForecast.alertDetails,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PolishMutedText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Downloaded Regional Map Weather Packages
        item {
            Text(
                text = "Downloaded Map Regional Forecasts",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Regional weather forecast list
        items(forecasts, key = { it.regionId }) { forecast ->
            RegionalForecastCard(
                forecast = forecast,
                onDownloadClick = { viewModel.downloadWeatherForecast(forecast.regionId) }
            )
        }
    }
}

@Composable
fun RegionalForecastCard(
    forecast: WeatherForecastEntity,
    onDownloadClick: () -> Unit
) {
    val severityColor = when (forecast.severity) {
        WeatherAlertSeverity.EXTREME_DANGER -> AlertRed
        WeatherAlertSeverity.WARNING -> HighOrange
        WeatherAlertSeverity.ADVISORY -> MediumAmber
        WeatherAlertSeverity.NONE -> SafeGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PolishSubtleBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(severityColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (forecast.condition) {
                                WeatherCondition.CLEAR_SUNNY -> Icons.Default.WbSunny
                                WeatherCondition.HEAVY_DOWNPOUR, WeatherCondition.THUNDERSTORM_LIGHTNING -> Icons.Default.Thunderstorm
                                else -> Icons.Default.WaterDrop
                            },
                            contentDescription = forecast.condition.label,
                            tint = severityColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = forecast.regionName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                        Text(
                            text = forecast.condition.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = PolishMutedText
                        )
                    }
                }

                WeatherSeverityChip(severity = forecast.severity)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = forecast.alertHeadline,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (forecast.severity != WeatherAlertSeverity.NONE) severityColor else DarkSlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "🌡️ ${forecast.temperatureC}°C",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkSlate
                    )
                    Text(
                        text = "🌧️ ${forecast.precipitationMmPerHour}mm/h",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkSlate
                    )
                    Text(
                        text = "💨 ${forecast.windSpeedKmh.toInt()}km/h",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkSlate
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SafeGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✓ Offline Cached",
                            style = MaterialTheme.typography.labelSmall,
                            color = SafeGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherMetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = DarkSlate
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PolishMutedText,
            fontSize = 10.sp
        )
    }
}

@Composable
fun WeatherSeverityChip(severity: WeatherAlertSeverity) {
    val (bg, fg, label) = when (severity) {
        WeatherAlertSeverity.EXTREME_DANGER -> Triple(AlertRed.copy(alpha = 0.15f), AlertRed, "EXTREME")
        WeatherAlertSeverity.WARNING -> Triple(HighOrange.copy(alpha = 0.15f), HighOrange, "WARNING")
        WeatherAlertSeverity.ADVISORY -> Triple(MediumAmber.copy(alpha = 0.15f), MediumAmber, "ADVISORY")
        WeatherAlertSeverity.NONE -> Triple(SafeGreen.copy(alpha = 0.15f), SafeGreen, "CLEAR")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            fontSize = 11.sp
        )
    }
}
