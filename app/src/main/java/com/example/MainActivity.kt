package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyType
import com.example.ui.screens.BleMeshScreen
import com.example.ui.screens.ControlCenterScreen
import com.example.ui.screens.DigitalPassportScreen
import com.example.ui.screens.IncidentHistoryScreen
import com.example.ui.screens.ItineraryPlanningScreen
import com.example.ui.screens.LiveRadarScreen
import com.example.ui.screens.MapAndGeoFenceScreen
import com.example.ui.screens.WeatherAlertScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedDark
import com.example.ui.theme.AlertRedLight
import com.example.ui.theme.MyApplicationTheme
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
import com.example.ui.theme.SafetyNavy
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.SafetyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SafetyViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val tourist by viewModel.tourist.collectAsState()
                val currentLocation by viewModel.currentLocation.collectAsState()
                val riskAnalysis by viewModel.riskAnalysis.collectAsState()
                val geoFences by viewModel.geoFences.collectAsState()
                val incidents by viewModel.incidents.collectAsState()
                val meshPackets by viewModel.meshPackets.collectAsState()
                val meshEvents by viewModel.meshEvents.collectAsState()
                val activePeers by viewModel.activePeers.collectAsState()

                val batteryLevel by viewModel.batteryLevel.collectAsState()
                val isOfflineMode by viewModel.isSimulatedOffline.collectAsState()
                val isSimulatedInactivity by viewModel.isSimulatedInactivity.collectAsState()
                val isSimulatedOffRoute by viewModel.isSimulatedOffRoute.collectAsState()
                val activeSos by viewModel.activeSosAlert.collectAsState()
                val aiAdvisory by viewModel.aiAdvisory.collectAsState()
                val isGeneratingAi by viewModel.isGeneratingAiAdvisory.collectAsState()

                val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()
                val isSyncing by viewModel.isSyncing.collectAsState()
                val syncMessage by viewModel.syncMessage.collectAsState()
                val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishSubtleBorder),
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                // Top Status Bar: Tracking Active & Telemetry Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { viewModel.toggleOfflineMode() }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    if (isOfflineMode) WarningAmber else SafeGreen,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isOfflineMode) "OFFLINE MESH TRACKING" else "ONLINE CLOUD ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = PolishSecondary,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Quick Sync Action
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = "Sync",
                                            tint = if (pendingSyncCount > 0) WarningAmber else PolishSecondary,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable { viewModel.triggerCloudSync() }
                                                .testTag("top_bar_sync_button")
                                        )

                                        // Mesh / Bluetooth Indicator
                                        Icon(
                                            imageVector = Icons.Default.Bluetooth,
                                            contentDescription = "BLE Mesh",
                                            tint = PolishSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )

                                        // Admin Switch
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = "Control Center",
                                            tint = if (currentScreen == AppScreen.CONTROL_CENTRE) AlertRed else PolishSecondary,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable {
                                                    if (currentScreen == AppScreen.CONTROL_CENTRE) {
                                                        viewModel.navigateTo(AppScreen.RADAR)
                                                    } else {
                                                        viewModel.navigateTo(AppScreen.CONTROL_CENTRE)
                                                    }
                                                }
                                                .testTag("top_bar_admin_button")
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Bottom Header Row: Trail Info & Digital ID Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "Nilgiris Trekking",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Avalanche Valley Safety Circuit",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PolishMutedText,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Verified ID Pill
                                    Surface(
                                        color = PolishPrimaryLight,
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.DIGITAL_ID) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = PolishPrimaryDark,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "ID: ${tourist?.touristId ?: "T-8492"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PolishPrimaryDark,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishSubtleBorder)
                        ) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(64.dp)
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.RADAR,
                                    onClick = { viewModel.navigateTo(AppScreen.RADAR) },
                                    icon = { Icon(Icons.Default.Radar, contentDescription = "Radar") },
                                    label = { Text("Radar", fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.RADAR) FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PolishPrimaryDark,
                                        selectedTextColor = PolishPrimaryDark,
                                        indicatorColor = PolishPrimaryLight,
                                        unselectedIconColor = PolishSecondary,
                                        unselectedTextColor = PolishSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_radar")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.ITINERARY,
                                    onClick = { viewModel.navigateTo(AppScreen.ITINERARY) },
                                    icon = { Icon(Icons.Default.Route, contentDescription = "Itinerary") },
                                    label = { Text("Itinerary", fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.ITINERARY) FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PolishPrimaryDark,
                                        selectedTextColor = PolishPrimaryDark,
                                        indicatorColor = PolishPrimaryLight,
                                        unselectedIconColor = PolishSecondary,
                                        unselectedTextColor = PolishSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_itinerary")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.WEATHER,
                                    onClick = { viewModel.navigateTo(AppScreen.WEATHER) },
                                    icon = { Icon(Icons.Default.Thunderstorm, contentDescription = "Weather Radar") },
                                    label = { Text("Weather", fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.WEATHER) FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PolishPrimaryDark,
                                        selectedTextColor = PolishPrimaryDark,
                                        indicatorColor = PolishPrimaryLight,
                                        unselectedIconColor = PolishSecondary,
                                        unselectedTextColor = PolishSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_weather")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.MAP,
                                    onClick = { viewModel.navigateTo(AppScreen.MAP) },
                                    icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                                    label = { Text("Map", fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.MAP) FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PolishPrimaryDark,
                                        selectedTextColor = PolishPrimaryDark,
                                        indicatorColor = PolishPrimaryLight,
                                        unselectedIconColor = PolishSecondary,
                                        unselectedTextColor = PolishSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_map")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.INCIDENTS,
                                    onClick = { viewModel.navigateTo(AppScreen.INCIDENTS) },
                                    icon = {
                                        Box {
                                            Icon(Icons.Default.Warning, contentDescription = "Incidents")
                                            if (pendingSyncCount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(AlertRed, CircleShape)
                                                        .align(Alignment.TopEnd)
                                                )
                                            }
                                        }
                                    },
                                    label = { Text("SOS Logs", fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.INCIDENTS) FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AlertRed,
                                        selectedTextColor = AlertRed,
                                        indicatorColor = AlertRedLight,
                                        unselectedIconColor = PolishSecondary,
                                        unselectedTextColor = PolishSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_incidents")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.BLE_MESH,
                                    onClick = { viewModel.navigateTo(AppScreen.BLE_MESH) },
                                    icon = { Icon(Icons.Default.Hub, contentDescription = "BLE Mesh") },
                                    label = { Text("Mesh", fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.BLE_MESH) FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PolishPrimaryDark,
                                        selectedTextColor = PolishPrimaryDark,
                                        indicatorColor = PolishPrimaryLight,
                                        unselectedIconColor = PolishSecondary,
                                        unselectedTextColor = PolishSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_mesh")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.DIGITAL_ID,
                                    onClick = { viewModel.navigateTo(AppScreen.DIGITAL_ID) },
                                    icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Digital ID") },
                                    label = { Text("Passport", fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.DIGITAL_ID) FontWeight.Bold else FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PolishPrimaryDark,
                                        selectedTextColor = PolishPrimaryDark,
                                        indicatorColor = PolishPrimaryLight,
                                        unselectedIconColor = PolishSecondary,
                                        unselectedTextColor = PolishSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_passport")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                AppScreen.RADAR -> LiveRadarScreen(
                                    tourist = tourist,
                                    currentLocation = currentLocation,
                                    riskAnalysis = riskAnalysis,
                                    geoFences = geoFences,
                                    batteryLevel = batteryLevel,
                                    isOfflineMode = isOfflineMode,
                                    isSimulatedInactivity = isSimulatedInactivity,
                                    isSimulatedOffRoute = isSimulatedOffRoute,
                                    activeSos = activeSos,
                                    aiAdvisory = aiAdvisory,
                                    isGeneratingAi = isGeneratingAi,
                                    onTriggerSos = { type, msg -> viewModel.triggerSos(type, msg) },
                                    onCancelSos = { viewModel.cancelSos() },
                                    onRequestAiCheck = { viewModel.requestAiSafetyCheck() },
                                    onToggleOffline = { viewModel.toggleOfflineMode() },
                                    onToggleInactivity = { viewModel.toggleInactivity() },
                                    onToggleOffRoute = { viewModel.toggleOffRoute() },
                                    onSimulateDangerBreach = {
                                        // Teleport to Avalanche Emerald scree landslide hazard
                                        viewModel.simulateMoveTo(11.3142, 76.5891, 2350.0)
                                    },
                                    onSimulateSafeShelter = {
                                        // Teleport to Upper Bhavani Ranger shelter
                                        viewModel.simulateMoveTo(11.2350, 76.5380, 2180.0)
                                    },
                                    onOpenFullMap = { viewModel.navigateTo(AppScreen.MAP) }
                                )

                                AppScreen.ITINERARY -> ItineraryPlanningScreen(
                                    viewModel = viewModel
                                )

                                AppScreen.WEATHER -> WeatherAlertScreen(
                                    viewModel = viewModel
                                )

                                AppScreen.MAP -> MapAndGeoFenceScreen(
                                    currentLocation = currentLocation,
                                    geoFences = geoFences,
                                    onTeleportToCoordinate = { lat, lng, alt ->
                                        viewModel.simulateMoveTo(lat, lng, alt)
                                    }
                                )

                                AppScreen.INCIDENTS -> IncidentHistoryScreen(
                                    incidents = incidents,
                                    pendingSyncCount = pendingSyncCount,
                                    isSyncing = isSyncing,
                                    syncMessage = syncMessage,
                                    lastSyncTimestamp = lastSyncTimestamp,
                                    onTriggerSync = { viewModel.triggerCloudSync() }
                                )

                                AppScreen.BLE_MESH -> BleMeshScreen(
                                    meshPackets = meshPackets,
                                    activePeers = activePeers,
                                    meshEvents = meshEvents,
                                    onSimulateHop = { packet, peer -> viewModel.simulateMeshRelayHop(packet, peer) },
                                    onAddPeer = { name, role, hasNet -> viewModel.addSimulatedPeer(name, role, hasNet) }
                                )

                                AppScreen.DIGITAL_ID -> DigitalPassportScreen(
                                    tourist = tourist,
                                    onUpdateProfile = { name, phone, blood, hotel ->
                                        viewModel.updateTouristProfile(name, phone, blood, hotel)
                                    }
                                )

                                AppScreen.CONTROL_CENTRE -> ControlCenterScreen(
                                    incidents = incidents,
                                    currentLocation = currentLocation,
                                    geoFences = geoFences,
                                    onAcknowledge = { id, note -> viewModel.acknowledgeIncident(id, note) },
                                    onDispatchRescue = { id, note -> viewModel.dispatchRescueTeam(id, note) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
