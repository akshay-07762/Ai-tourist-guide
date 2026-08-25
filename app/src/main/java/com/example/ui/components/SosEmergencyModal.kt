package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExploreOff
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyType
import com.example.data.model.LocationLogEntity
import com.example.ui.theme.AlertRed
import com.example.ui.theme.RescueOrange
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafetyCyan
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun SosEmergencyModal(
    isOpen: Boolean,
    currentLocation: LocationLogEntity,
    isOfflineMode: Boolean,
    onDismiss: () -> Unit,
    onConfirmSos: (EmergencyType, String) -> Unit
) {
    if (!isOpen) return

    var selectedType by remember { mutableStateOf(EmergencyType.SOS_MANUAL) }
    var customMessage by remember { mutableStateOf("") }
    var countdownSeconds by remember { mutableIntStateOf(5) }
    var isCountdownActive by remember { mutableStateOf(false) }

    // Pulsing animation for SOS beacon button
    val infiniteTransition = rememberInfiniteTransition(label = "sos_modal_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_scale"
    )

    LaunchedEffect(isCountdownActive) {
        if (isCountdownActive) {
            countdownSeconds = 5
            while (countdownSeconds > 0) {
                delay(1000)
                countdownSeconds--
            }
            if (countdownSeconds == 0) {
                onConfirmSos(selectedType, customMessage.ifBlank { "EMERGENCY: Tourist triggered SOS beacon." })
                onDismiss()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            isCountdownActive = false
            onDismiss()
        },
        modifier = Modifier.testTag("sos_emergency_modal"),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(AlertRed, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EMERGENCY SOS DISPATCH",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = AlertRed
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Glowing SOS Button
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(AlertRed, Color(0xFFB71C1C), Color(0xFF5F0909))
                            ),
                            shape = CircleShape
                        )
                        .border(4.dp, Color(0xFFFFCDD2), CircleShape)
                        .clickable {
                            if (!isCountdownActive) {
                                isCountdownActive = true
                            }
                        }
                        .testTag("modal_sos_beacon_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isCountdownActive) "$countdownSeconds" else "🚨 SOS",
                            style = if (isCountdownActive) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        if (!isCountdownActive) {
                            Text(
                                text = "TAP TO TRIGGER",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFCDD2),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multi-Channel Failover Status Breakdown
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Multi-Channel Transmission Fallback:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChannelPill(
                                title = "1. Cloud / 4G",
                                icon = Icons.Default.Public,
                                isActive = !isOfflineMode,
                                activeColor = SafeGreen
                            )
                            ChannelPill(
                                title = "2. BLE Mesh Relay",
                                icon = Icons.Default.Bluetooth,
                                isActive = true, // Always ready for opportunistic peer hopping
                                activeColor = SafetyCyan
                            )
                            ChannelPill(
                                title = "3. Sat Buffer",
                                icon = Icons.Default.Landscape,
                                isActive = isOfflineMode,
                                activeColor = WarningAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Emergency Type Selector
                Text(
                    text = "Select Emergency Category:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val emergencyTypes = listOf(
                    Pair(EmergencyType.SOS_MANUAL, "Manual SOS"),
                    Pair(EmergencyType.MEDICAL, "Medical Alert"),
                    Pair(EmergencyType.LOST_STRANDED, "Lost / Off-Route"),
                    Pair(EmergencyType.LANDSLIDE_DISASTER, "Landslide / Hazard"),
                    Pair(EmergencyType.WILD_ANIMAL, "Wild Animal")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emergencyTypes) { (type, label) ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .clickable { selectedType = type }
                                .testTag("emergency_type_${type.name}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AlertRed else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GPS Telemetry Snapshot
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GPS: ${"%.4f".format(currentLocation.latitude)}, ${"%.4f".format(currentLocation.longitude)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "Alt: ${currentLocation.altitudeMeters.toInt()}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isCountdownActive = false
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onConfirmSos(selectedType, customMessage.ifBlank { "EMERGENCY: Tourist triggered SOS beacon." })
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_sos_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) {
                        Text("Send SOS Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) activeColor.copy(alpha = 0.15f) else Color(0xFFE2E8F0),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, activeColor) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) activeColor else Color.Gray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) activeColor else Color.Gray
            )
        }
    }
}
