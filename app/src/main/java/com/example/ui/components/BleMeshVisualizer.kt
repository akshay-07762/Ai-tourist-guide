package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WifiOff
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
import com.example.data.engine.BleMeshRelayEvent
import com.example.data.engine.MeshPeerNode
import com.example.data.model.BleMeshPacketEntity
import com.example.ui.theme.AlertRed
import com.example.ui.theme.RescueOrange
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafetyCyan
import com.example.ui.theme.WarningAmber

@Composable
fun BleMeshVisualizer(
    meshPackets: List<BleMeshPacketEntity>,
    activePeers: List<MeshPeerNode>,
    meshEvents: List<BleMeshRelayEvent>,
    onSimulateHop: (BleMeshPacketEntity, MeshPeerNode) -> Unit,
    onAddPeer: (String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ble_mesh_visualizer"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PolishSubtleBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(SafetyCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Hub, contentDescription = null, tint = SafetyCyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OFFLINE STORE-AND-FORWARD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SafetyCyan
                        )
                        Text(
                            text = "BLE Mesh Peer Topology",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = SafeGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${activePeers.size} PEERS ACTIVE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SafeGreen,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Node Chain (Tourist A -> Tourist B -> Gateway)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Store-and-Forward Multi-Hop Pathway:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MeshNodeItem(name = "You (T-8492)", role = "Zero Signal", isYou = true, hasNet = false)
                        Text("⇄", color = SafetyCyan, fontWeight = FontWeight.Black)
                        MeshNodeItem(name = "Peer B", role = "Trekker", isYou = false, hasNet = false)
                        Text("⇄", color = SafetyCyan, fontWeight = FontWeight.Black)
                        MeshNodeItem(name = "Ranger R08", role = "Patrol", isYou = false, hasNet = false)
                        Text("➔", color = SafeGreen, fontWeight = FontWeight.Black)
                        MeshNodeItem(name = "Cloud Hub", role = "Uplink", isYou = false, hasNet = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nearby Discovered Peers
            Text(
                text = "Nearby Participating BLE Devices:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(activePeers) { peer ->
                    Surface(
                        modifier = Modifier
                            .width(200.dp)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = peer.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (peer.hasInternetAccess) Icons.Default.CloudDone else Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = if (peer.hasInternetAccess) SafeGreen else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "RSSI: ${peer.rssiDb} dBm • ~${peer.distanceApproxMeters}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Relay action button if there are packets
                            if (meshPackets.isNotEmpty()) {
                                Button(
                                    onClick = { onSimulateHop(meshPackets.first(), peer) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SafetyCyan),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Relay Packet ➔", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Peer Injection Simulation Controls for Judges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddPeer("Tourist Group Beta", "TOURIST_DEVICE", false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Offline Peer", fontSize = 10.sp)
                }
                OutlinedButton(
                    onClick = { onAddPeer("Rescue Satellite Van", "CLOUD_GATEWAY", true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(14.dp), tint = SafeGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Cloud Relay", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Mesh Event Telemetry Logs
            Text(
                text = "Live Store-and-Forward Audit Trail:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0B1320),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                if (meshEvents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No mesh packets broadcasted yet.\nTrigger SOS to see store-and-forward hopping.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(meshEvents) { event ->
                            Column(modifier = Modifier.padding(vertical = 3.dp)) {
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (event.eventType) {
                                        "PACKET_BROADCAST" -> RescueOrange
                                        "CLOUD_UPLOADED" -> SafeGreen
                                        "DUPLICATE_DROPPED" -> WarningAmber
                                        else -> SafetyCyan
                                    },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeshNodeItem(name: String, role: String, isYou: Boolean, hasNet: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    if (isYou) RescueOrange else if (hasNet) SafeGreen else Color(0xFF334155),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasNet) Icons.Default.CloudDone else Icons.Default.Bluetooth,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(role, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontSize = 8.sp)
    }
}
