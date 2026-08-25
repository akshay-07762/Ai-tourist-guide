package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyType
import com.example.data.model.GeoFenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.ResponderStatus
import com.example.data.model.RiskLevel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.RescueOrange
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafetyCyan
import com.example.ui.theme.WarningAmber

@Composable
fun ControlCenterScreen(
    incidents: List<IncidentEntity>,
    currentLocation: LocationLogEntity,
    geoFences: List<GeoFenceEntity>,
    onAcknowledge: (String, String) -> Unit,
    onDispatchRescue: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("control_center_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "AUTHORITY EMERGENCY DASHBOARD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = RescueOrange
                )
                Text(
                    text = "Nilgiris Rescue Command Center",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Unified command dashboard for Forest Rangers, SDRF Mountain Rescue, and Police First Responders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. High-Level Metrics Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    title = "ACTIVE INCIDENTS",
                    value = "${incidents.count { it.responderStatus != ResponderStatus.RESOLVED }}",
                    color = AlertRed,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "MONITORED ZONES",
                    value = "${geoFences.size}",
                    color = com.example.ui.theme.PolishPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "RESCUE SQUAD",
                    value = "STANDBY",
                    color = SafeGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Live Tourist Telemetry Stream
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PolishSubtleBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = com.example.ui.theme.PolishPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Active Hiker Beacon: Alex Chen (T-8492)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            color = SafeGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("LIVE GPS", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = SafeGreen, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("COORDINATES", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontSize = 8.sp)
                                Text("${"%.4f".format(currentLocation.latitude)}, ${"%.4f".format(currentLocation.longitude)}", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("ALTITUDE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontSize = 8.sp)
                                Text("${currentLocation.altitudeMeters.toInt()} meters", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("SPEED", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontSize = 8.sp)
                                Text("${"%.1f".format(currentLocation.speedKmh)} km/h", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Incident Triage Queue
        item {
            Text(
                text = "Emergency Response Queue (${incidents.size}):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (incidents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PolishSubtleBorder)
                ) {
                    Text(
                        text = "No emergency calls in queue. All tourist corridors operating normally.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(incidents) { incident ->
                CommandIncidentCard(
                    incident = incident,
                    onAcknowledge = { onAcknowledge(incident.incidentId, "Acknowledged by Nilgiris District Emergency HQ") },
                    onDispatch = { onDispatchRescue(incident.incidentId, "SDRF Mountain Rescue Team Alpha Dispatched with Medical Trauma Kit") }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PolishSubtleBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun CommandIncidentCard(
    incident: IncidentEntity,
    onAcknowledge: () -> Unit,
    onDispatch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("command_incident_${incident.incidentId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PolishSubtleBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            .size(10.dp)
                            .background(
                                when (incident.responderStatus) {
                                    ResponderStatus.OPEN -> AlertRed
                                    ResponderStatus.RESCUE_DISPATCHED -> RescueOrange
                                    else -> SafeGreen
                                },
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${incident.incidentId} • ${incident.touristName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                }

                Surface(
                    color = when (incident.responderStatus) {
                        ResponderStatus.OPEN -> AlertRed.copy(alpha = 0.15f)
                        ResponderStatus.RESCUE_DISPATCHED -> RescueOrange.copy(alpha = 0.15f)
                        else -> SafeGreen.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = incident.responderStatus.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (incident.responderStatus) {
                            ResponderStatus.OPEN -> AlertRed
                            ResponderStatus.RESCUE_DISPATCHED -> RescueOrange
                            else -> SafeGreen
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Distress: ${incident.distressMessage}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            if (incident.responderNotes != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dispatch Log: ${incident.responderNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SafeGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (incident.responderStatus == ResponderStatus.OPEN) {
                    OutlinedButton(
                        onClick = onAcknowledge,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Acknowledge", fontSize = 11.sp)
                    }
                }

                if (incident.responderStatus != ResponderStatus.RESCUE_DISPATCHED && incident.responderStatus != ResponderStatus.RESOLVED) {
                    Button(
                        onClick = onDispatch,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dispatch Rescue Squad", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
