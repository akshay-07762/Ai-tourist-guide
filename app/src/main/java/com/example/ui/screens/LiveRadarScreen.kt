package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.engine.AiSafetyAdvisory
import com.example.data.engine.RiskAnalysisResult
import com.example.data.model.EmergencyType
import com.example.data.model.GeoFenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.RiskLevel
import com.example.data.model.TouristEntity
import com.example.ui.components.DigitalIdBadge
import com.example.ui.components.OfflineCanvasMap
import com.example.ui.components.RiskScoreCard
import com.example.ui.components.SosEmergencyModal
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedDark
import com.example.ui.theme.AlertRedLight
import com.example.ui.theme.PolishMutedText
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryDark
import com.example.ui.theme.PolishPrimaryLight
import com.example.ui.theme.PolishSecondary
import com.example.ui.theme.PolishSecondaryContainer
import com.example.ui.theme.PolishSubtleBorder
import com.example.ui.theme.RescueOrange
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenLight
import com.example.ui.theme.SafetyCyan
import com.example.ui.theme.WarningAmber

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun LiveRadarScreen(
    tourist: TouristEntity?,
    currentLocation: LocationLogEntity,
    riskAnalysis: RiskAnalysisResult,
    geoFences: List<GeoFenceEntity>,
    batteryLevel: Int,
    isOfflineMode: Boolean,
    isSimulatedInactivity: Boolean,
    isSimulatedOffRoute: Boolean,
    activeSos: IncidentEntity?,
    aiAdvisory: AiSafetyAdvisory?,
    isGeneratingAi: Boolean,
    onTriggerSos: (EmergencyType, String) -> Unit,
    onCancelSos: () -> Unit,
    onRequestAiCheck: () -> Unit,
    onToggleOffline: () -> Unit,
    onToggleInactivity: () -> Unit,
    onToggleOffRoute: () -> Unit,
    onSimulateDangerBreach: () -> Unit,
    onSimulateSafeShelter: () -> Unit,
    onOpenFullMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSosModal by remember { mutableStateOf(false) }

    // Pulsing animation for Active SOS Warning Banner
    val infiniteTransition = rememberInfiniteTransition(label = "sos_alarm")
    val sosPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_banner_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("live_radar_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Mountain Safety Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0A111E))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_mountain_safety),
                    contentDescription = "Mountain Safety Hero",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x99001D35), Color(0xF0001D35))
                            )
                        )
                )

                // Top Floating Badges: Offline Mode & Battery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isOfflineMode) WarningAmber.copy(alpha = 0.95f) else SafeGreen.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { onToggleOffline() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isOfflineMode) Icons.Default.WifiOff else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOfflineMode) "OFFLINE MESH ACTIVE" else "ONLINE CLOUD SYNC",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0xCC001D35),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF535F70))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$batteryLevel%", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Banner Text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Nilgiris Mountain Safety Corridor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "GPS: ${"%.4f".format(currentLocation.latitude)}, ${"%.4f".format(currentLocation.longitude)} • Alt ${currentLocation.altitudeMeters.toInt()}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD3E4FF)
                    )
                }
            }
        }

        // 2. Active SOS Alert Banner (If triggered)
        if (activeSos != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(sosPulseScale)
                        .testTag("active_sos_banner"),
                    colors = CardDefaults.cardColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🚨 SOS DISTRESS BEACON ACTIVE",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Button(
                                onClick = onCancelSos,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancel", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Incident ID: ${activeSos.incidentId} • Channel: ${activeSos.relayedVia.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFCDD2)
                        )
                        Text(
                            text = "Broadcasting BLE store-and-forward packet to all nearby tourist nodes & mountain rangers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Proximity / Terrain Hazard Indicator (Professional Polish Accent)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishSubtleBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (riskAnalysis.riskLevel >= RiskLevel.HIGH) AlertRed else SafeGreen,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (riskAnalysis.riskLevel >= RiskLevel.HIGH) "PROXIMITY WARNING" else "TERRAIN STATUS NORMAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (riskAnalysis.riskLevel >= RiskLevel.HIGH) AlertRed else SafeGreen,
                                letterSpacing = 0.5.sp,
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (riskAnalysis.riskLevel >= RiskLevel.HIGH) "120m to Landslide Hazard Boundary" else "Inside Designated Safe Hiking Corridor",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        color = PolishPrimaryLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "OFFLINE TOPO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimaryDark,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Weather & Itinerary Live Telemetry Glance
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Weather Glance Card
                val weather = riskAnalysis.weatherAssessment
                val isWeatherThreat = weather?.isInsideSevereWeather == true || weather?.isHeadingTowardsSevereWeather == true
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isWeatherThreat) AlertRed else PolishSubtleBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isWeatherThreat) "⚠️" else "⛅", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WEATHER RADAR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isWeatherThreat) AlertRed else PolishMutedText,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = weather?.activeRegionForecast?.condition?.label ?: "Clear / Baseline",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${weather?.activeRegionForecast?.temperatureC ?: 14.0}°C • ${weather?.activeRegionForecast?.precipitationMmPerHour ?: 0.0}mm/h",
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishMutedText,
                            fontSize = 10.sp
                        )
                    }
                }

                // Route Deviation Glance Card
                val routeDev = riskAnalysis.routeDeviation
                val isRouteDev = routeDev?.isOffRoute == true
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isRouteDev) AlertRed else PolishSubtleBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isRouteDev) "🚨" else "🧭", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ITINERARY CORRIDOR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isRouteDev) AlertRed else PolishMutedText,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRouteDev) "${routeDev?.deviationDistanceMeters?.toInt() ?: 0}m Off Route!" else "On Track (<80m)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isRouteDev) AlertRed else SafeGreen
                        )
                        Text(
                            text = routeDev?.nearestWaypointName ?: "Active Trail Corridor",
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishMutedText,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 4. Metric Cards Grid: Compass Bearing & Elevation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishSubtleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = PolishPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BEARING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PolishMutedText, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("242° SW", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Stable heading", style = MaterialTheme.typography.bodySmall, color = PolishMutedText, fontSize = 10.sp)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishSubtleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = PolishPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ELEVATION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PolishMutedText, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${currentLocation.altitudeMeters.toInt()} m", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("+45m this hour", style = MaterialTheme.typography.bodySmall, color = PolishMutedText, fontSize = 10.sp)
                    }
                }
            }
        }

        // 5. Primary Emergency SOS Trigger Button Bar (Professional Polish Red CTA)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishSubtleBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EMERGENCY RESPONSE DISPATCH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PolishMutedText,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Big Red SOS Button
                        Button(
                            onClick = { showSosModal = true },
                            modifier = Modifier
                                .weight(1.4f)
                                .height(56.dp)
                                .testTag("main_sos_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("EMERGENCY SOS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Medical Fast Action
                        OutlinedButton(
                            onClick = {
                                onTriggerSos(EmergencyType.MEDICAL, "Medical Alert: Tourist requires urgent first-aid.")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("quick_medical_sos_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, AlertRed)
                        ) {
                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Medical", color = AlertRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6. Real-time Risk Score & Explainable AI Factors Card
        item {
            RiskScoreCard(
                riskAnalysis = riskAnalysis,
                aiAdvisory = aiAdvisory,
                isGeneratingAi = isGeneratingAi,
                onRequestAiCheck = onRequestAiCheck
            )
        }

        // 7. Interactive Live Map Mini-Preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishSubtleBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = PolishPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Live Offline Topo Radar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onOpenFullMap,
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Full Map ➔", color = PolishPrimaryDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OfflineCanvasMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        currentLocation = currentLocation,
                        geoFences = geoFences,
                        onLocationTapSimulate = { lat, lng -> }
                    )
                }
            }
        }

        // 8. Rapid Simulation Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishSubtleBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = PolishPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live Incident & Sensor Simulation Cockpit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Simulate real-world hill station hazards to test automatic geofence triggers and AI risk engine:",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishMutedText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulation Switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Simulate Inactivity (Fall Anomaly):", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = isSimulatedInactivity,
                            onCheckedChange = { onToggleInactivity() },
                            colors = SwitchDefaults.colors(checkedThumbColor = AlertRed)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Simulate Off-Route Deviation (380m):", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = isSimulatedOffRoute,
                            onCheckedChange = { onToggleOffRoute() },
                            colors = SwitchDefaults.colors(checkedThumbColor = WarningAmber)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Instant Teleport Simulation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSimulateDangerBreach,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("⚠️ Enter Hazard Zone", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onSimulateSafeShelter,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🛡️ Enter Safe Shelter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 9. Tourist Digital ID Snapshot
        item {
            DigitalIdBadge(tourist = tourist)
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // SOS Modal Dialog
    SosEmergencyModal(
        isOpen = showSosModal,
        currentLocation = currentLocation,
        isOfflineMode = isOfflineMode,
        onDismiss = { showSosModal = false },
        onConfirmSos = { type, msg ->
            onTriggerSos(type, msg)
            showSosModal = false
        }
    )
}
