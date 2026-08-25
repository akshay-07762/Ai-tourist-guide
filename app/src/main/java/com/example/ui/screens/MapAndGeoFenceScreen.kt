package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeoFenceEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.RiskLevel
import com.example.ui.components.OfflineCanvasMap
import com.example.ui.theme.AlertRed
import com.example.ui.theme.RescueOrange
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafetyCyan
import com.example.ui.theme.WarningAmber

@Composable
fun MapAndGeoFenceScreen(
    currentLocation: LocationLogEntity,
    geoFences: List<GeoFenceEntity>,
    onTeleportToCoordinate: (Double, Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedFence by remember { mutableStateOf<GeoFenceEntity?>(null) }

    val filteredFences = remember(geoFences, selectedCategory) {
        if (selectedCategory == "ALL") geoFences
        else if (selectedCategory == "SAFE") geoFences.filter { it.isSafeZone }
        else if (selectedCategory == "HAZARD") geoFences.filter { !it.isSafeZone }
        else geoFences.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("map_and_geofence_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Filter Chips
        item {
            Column {
                Text(
                    text = "OFFLINE VECTOR MAP & GEO-FENCING",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = RescueOrange
                )
                Text(
                    text = "Terrain Topography & Safety Zones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(10.dp))

                val filterOptions = listOf(
                    Pair("ALL", "All Zones (${geoFences.size})"),
                    Pair("HAZARD", "⚠️ Danger Zones"),
                    Pair("SAFE", "🛡️ Safe Shelters"),
                    Pair("LANDSLIDE", "Landslide Scree"),
                    Pair("WILDLIFE", "Elephant Corridor")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions) { (key, label) ->
                        val isSelected = selectedCategory == key
                        Surface(
                            modifier = Modifier
                                .clickable { selectedCategory = key }
                                .testTag("filter_chip_$key"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) com.example.ui.theme.PolishPrimary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive Map View
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PolishSubtleBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OfflineCanvasMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        currentLocation = currentLocation,
                        geoFences = filteredFences,
                        selectedZone = selectedFence,
                        onZoneClick = { selectedFence = it },
                        onLocationTapSimulate = { lat, lng ->
                            onTeleportToCoordinate(lat, lng, 2250.0)
                        }
                    )
                }
            }
        }

        // 3. Selected Geo-Fence Detailed Card (If selected)
        if (selectedFence != null) {
            val fence = selectedFence!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (fence.isSafeZone) SafeGreen.copy(alpha = 0.1f) else AlertRed.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (fence.isSafeZone) SafeGreen else AlertRed
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (fence.isSafeZone) Icons.Default.Shield else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (fence.isSafeZone) SafeGreen else AlertRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fence.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                color = if (fence.isSafeZone) SafeGreen else AlertRed,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = fence.riskLevel.displayName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = fence.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "SAFETY DIRECTIVE:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SafetyCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = fence.warningMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Advice: ${fence.safeActionAdvice}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onTeleportToCoordinate(fence.centerLat, fence.centerLng, 2200.0)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (fence.isSafeZone) SafeGreen else AlertRed
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Walking Here", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Geo-Fence Database List
        item {
            Text(
                text = "Monitored Safety Perimeters (${filteredFences.size}):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(filteredFences) { fence ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedFence = fence }
                    .testTag("geofence_item_${fence.zoneId}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PolishSubtleBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (fence.isSafeZone) SafeGreen.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (fence.isSafeZone) Icons.Default.Shield else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (fence.isSafeZone) SafeGreen else AlertRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fence.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Radius: ${fence.radiusMeters.toInt()}m • ${fence.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            selectedFence = fence
                            onTeleportToCoordinate(fence.centerLat, fence.centerLng, 2200.0)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Jump", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
