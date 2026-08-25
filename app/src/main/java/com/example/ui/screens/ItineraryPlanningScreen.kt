package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.ItineraryEngine
import com.example.data.model.ItineraryEntity
import com.example.data.model.SyncStatus
import com.example.data.model.TransportMode
import com.example.data.model.WaypointItem
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.MediumAmber
import com.example.ui.theme.PolishMutedText
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryLight
import com.example.ui.theme.PolishSubtleBorder
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.viewmodel.SafetyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItineraryPlanningScreen(
    viewModel: SafetyViewModel,
    modifier: Modifier = Modifier
) {
    val itineraries by viewModel.itineraries.collectAsState()
    val activeItinerary by viewModel.activeItinerary.collectAsState()
    val riskAnalysis by viewModel.riskAnalysis.collectAsState()
    val isOffline by viewModel.isSimulatedOffline.collectAsState()
    val routeDeviation = riskAnalysis.routeDeviation

    var showCreateDialog by remember { mutableStateOf(false) }

    // State for planner form
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startName by remember { mutableStateOf("Ooty Botanical Trailhead") }
    var startLat by remember { mutableStateOf(11.4102) }
    var startLng by remember { mutableStateOf(76.6950) }
    var endName by remember { mutableStateOf("Avalanche Lake Sanctuary") }
    var endLat by remember { mutableStateOf(11.3120) }
    var endLng by remember { mutableStateOf(76.6180) }
    var selectedTransport by remember { mutableStateOf(TransportMode.HIKING_TREK) }
    var durationMinutes by remember { mutableIntStateOf(180) }
    var difficulty by remember { mutableStateOf("Moderate") }
    val waypoints = remember {
        mutableStateListOf(
            WaypointItem("WP-1", "Doddabetta Pine Forest Ridge", 11.3980, 76.7120, 2400.0, 30, 0),
            WaypointItem("WP-2", "Emerald Tea Valley Overlook", 11.3450, 76.6520, 2050.0, 20, 1)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Itinerary & Route Tracking",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isOffline) "Local Offline GPS Deviation Engine Active" else "Synced with Hill Response Control Cloud",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishMutedText
                    )
                }

                Button(
                    onClick = { showCreateDialog = !showCreateDialog },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("plan_new_route_btn")
                ) {
                    Icon(
                        imageVector = if (showCreateDialog) Icons.Default.Stop else Icons.Default.Add,
                        contentDescription = "Plan Route",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showCreateDialog) "Close Form" else "New Route", fontSize = 12.sp)
                }
            }
        }

        // Active Route & AI Deviation Assessment Card
        item {
            if (activeItinerary != null) {
                val itin = activeItinerary!!
                val isDeviated = routeDeviation?.isOffRoute == true
                val deviationColor = if (isDeviated) AlertRed else SafeGreen

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, if (isDeviated) AlertRed else PolishSubtleBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PolishPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getTransportIcon(itin.modeOfTransport),
                                        contentDescription = itin.modeOfTransport.displayName,
                                        tint = PolishPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ACTIVE MONITORED ITINERARY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishPrimary
                                    )
                                    Text(
                                        text = itin.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlate
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDeviated) AlertRed.copy(alpha = 0.15f) else SafeGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isDeviated) "ROUTE DEVIATION" else "ON TRACK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = deviationColor,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Route corridor metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Start: ${itin.startPointName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkSlate
                                )
                                Text(
                                    text = "Destination: ${itin.endPointName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkSlate
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Mode: ${itin.modeOfTransport.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PolishMutedText
                                )
                                Text(
                                    text = "Est. ${itin.expectedDurationMinutes} mins (${itin.difficultyLevel})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PolishMutedText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Route Deviation Engine Feedback Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDeviated) AlertRed.copy(alpha = 0.08f) else SafeGreen.copy(alpha = 0.08f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isDeviated) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = "Deviation Status",
                                        tint = deviationColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isDeviated) {
                                            "AI Route Deviation Alert (${routeDeviation?.deviationDistanceMeters?.toInt() ?: 0}m off trail)"
                                        } else {
                                            "Within Safety Corridor (${routeDeviation?.deviationDistanceMeters?.toInt() ?: 0}m from polyline)"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = deviationColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isDeviated) {
                                        "Tourist is ${routeDeviation?.deviationDistanceMeters?.toInt() ?: 0}m off the designated route. Nearest checkpoint: ${routeDeviation?.nearestWaypointName ?: itin.startPointName}."
                                    } else {
                                        "Position matches planned corridor towards ${routeDeviation?.nearestWaypointName ?: itin.endPointName}. Deviation severity: ${routeDeviation?.deviationSeverity ?: "NORMAL"}."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkSlate
                                )
                            }
                        }

                        // Waypoints list
                        val waypointList = ItineraryEngine.parseWaypoints(itin.waypointsJson)
                        if (waypointList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Planned Waypoints & Checkpoints:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            waypointList.forEachIndexed { idx, wp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(PolishPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PolishPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = wp.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = DarkSlate,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${wp.altitudeMeters.toInt()}m • ${wp.stayDurationMinutes}m rest",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PolishMutedText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.deactivateItinerary() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Pause Tracking", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PolishSubtleBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = "No active itinerary",
                            tint = PolishPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Active Itinerary Selected",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                        Text(
                            text = "Plan a route or activate a saved itinerary below to enable AI route deviation monitoring and weather trajectory safety alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PolishMutedText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // New Itinerary Planning Form (Expandable)
        if (showCreateDialog) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PolishPrimary, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Plan New Itinerary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Hill Station Presets
                        Text(
                            text = "Quick Nilgiris Presets:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            FilterChip(
                                selected = title == "Doddabetta to Avalanche Circuit",
                                onClick = {
                                    title = "Doddabetta to Avalanche Circuit"
                                    description = "High altitude scenic trek connecting Nilgiris peak with southern reservoir."
                                    startName = "Doddabetta Base Station"
                                    startLat = 11.4102
                                    startLng = 76.6950
                                    endName = "Avalanche Lake Outpost"
                                    endLat = 11.3120
                                    endLng = 76.6180
                                    selectedTransport = TransportMode.HIKING_TREK
                                    durationMinutes = 240
                                    difficulty = "Challenging"
                                },
                                label = { Text("Doddabetta Trek", fontSize = 11.sp) }
                            )

                            FilterChip(
                                selected = title == "Pykara Falls to Mukurthi Pass",
                                onClick = {
                                    title = "Pykara Falls to Mukurthi Pass"
                                    description = "Western ridge trail along tea plantations and Mukurthi national park perimeter."
                                    startName = "Pykara Boathouse Gate"
                                    startLat = 11.4580
                                    startLng = 76.5920
                                    endName = "Mukurthi Sanctuary Outpost"
                                    endLat = 11.3850
                                    endLng = 76.5200
                                    selectedTransport = TransportMode.MOUNTAIN_BIKE
                                    durationMinutes = 150
                                    difficulty = "Moderate"
                                },
                                label = { Text("Pykara - Mukurthi", fontSize = 11.sp) }
                            )

                            FilterChip(
                                selected = title == "Glenmorgan Tea Safari",
                                onClick = {
                                    title = "Glenmorgan Tea Safari"
                                    description = "Mountain safari trail through eucalyptus valleys and Toda tribal hamlets."
                                    startName = "Glenmorgan Ropeway Point"
                                    startLat = 11.4890
                                    startLng = 76.6450
                                    endName = "Singara Valley Camp"
                                    endLat = 11.5320
                                    endLng = 76.6120
                                    selectedTransport = TransportMode.MOUNTAIN_4X4
                                    durationMinutes = 120
                                    difficulty = "Easy"
                                },
                                label = { Text("Glenmorgan Safari", fontSize = 11.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Itinerary Name") },
                            modifier = Modifier.fillMaxWidth().testTag("itinerary_title_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Route Description & Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mode of Transport Selector
                        Text(
                            text = "Mode of Transport:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            TransportMode.values().forEach { mode ->
                                FilterChip(
                                    selected = selectedTransport == mode,
                                    onClick = { selectedTransport = mode },
                                    label = { Text(mode.displayName, fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getTransportIcon(mode),
                                            contentDescription = mode.displayName,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Start and End points
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = startName,
                                onValueChange = { startName = it },
                                label = { Text("Start Point") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = endName,
                                onValueChange = { endName = it },
                                label = { Text("End Point") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Duration & Difficulty
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = durationMinutes.toString(),
                                onValueChange = { durationMinutes = it.toIntOrNull() ?: 60 },
                                label = { Text("Est. Minutes") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = difficulty,
                                onValueChange = { difficulty = it },
                                label = { Text("Difficulty") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Waypoints
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Intermediate Waypoints (${waypoints.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )
                            IconButton(
                                onClick = {
                                    val nextIndex = waypoints.size
                                    waypoints.add(
                                        WaypointItem(
                                            id = "WP-${nextIndex + 1}",
                                            name = "Checkpoint #${nextIndex + 1}",
                                            latitude = startLat + (0.01 * (nextIndex + 1)),
                                            longitude = startLng - (0.01 * (nextIndex + 1)),
                                            altitudeMeters = 2200.0,
                                            stayDurationMinutes = 15,
                                            orderIndex = nextIndex
                                        )
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Waypoint", tint = PolishPrimary)
                            }
                        }

                        waypoints.forEachIndexed { index, wp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${wp.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                    color = DarkSlate
                                )
                                IconButton(
                                    onClick = { waypoints.removeAt(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = AlertRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    viewModel.createItinerary(
                                        title = title,
                                        description = description,
                                        startName = startName,
                                        startLat = startLat,
                                        startLng = startLng,
                                        endName = endName,
                                        endLat = endLat,
                                        endLng = endLng,
                                        waypoints = waypoints.toList(),
                                        transportMode = selectedTransport,
                                        durationMinutes = durationMinutes,
                                        difficulty = difficulty,
                                        setAsActive = true
                                    )
                                    showCreateDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                            modifier = Modifier.fillMaxWidth().testTag("save_itinerary_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save & Activate for AI Deviation Tracking")
                        }
                    }
                }
            }
        }

        // Section: Saved Itineraries
        item {
            Text(
                text = "Saved Itineraries (${itineraries.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(itineraries, key = { it.itineraryId }) { itin ->
            SavedItineraryCard(
                itinerary = itin,
                isActive = itin.isActive,
                onActivate = { viewModel.selectActiveItinerary(itin.itineraryId) },
                onDelete = { viewModel.deleteItinerary(itin.itineraryId) }
            )
        }
    }
}

@Composable
fun SavedItineraryCard(
    itinerary: ItineraryEntity,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isActive) PolishPrimary else PolishSubtleBorder,
                RoundedCornerShape(12.dp)
            ),
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
                            .background(if (isActive) PolishPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getTransportIcon(itinerary.modeOfTransport),
                            contentDescription = itinerary.modeOfTransport.displayName,
                            tint = if (isActive) PolishPrimary else DarkSlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = itinerary.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                        Text(
                            text = "${itinerary.startPointName} ➔ ${itinerary.endPointName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PolishMutedText
                        )
                    }
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PolishPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = itinerary.description,
                style = MaterialTheme.typography.bodySmall,
                color = DarkSlate,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "⏱️ ${itinerary.expectedDurationMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = PolishMutedText
                    )
                    Text(
                        text = "⛰️ ${itinerary.difficultyLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PolishMutedText
                    )
                    Text(
                        text = if (itinerary.syncStatus == SyncStatus.SYNCED) "☁️ Synced" else "💾 Local Room",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (itinerary.syncStatus == SyncStatus.SYNCED) SafeGreen else MediumAmber
                    )
                }

                Row {
                    if (!isActive) {
                        Button(
                            onClick = onActivate,
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Activate", fontSize = 11.sp)
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AlertRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getTransportIcon(mode: TransportMode): ImageVector {
    return when (mode) {
        TransportMode.HIKING_TREK -> Icons.Default.Hiking
        TransportMode.MOUNTAIN_4X4 -> Icons.Default.DirectionsCar
        TransportMode.MOUNTAIN_BIKE -> Icons.Default.DirectionsBike
        TransportMode.SHUTTLE_BUS -> Icons.Default.DirectionsBus
        TransportMode.CABLE_CAR -> Icons.Default.Route
    }
}
