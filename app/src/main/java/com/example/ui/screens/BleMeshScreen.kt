package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.BleMeshRelayEvent
import com.example.data.engine.MeshPeerNode
import com.example.data.model.BleMeshPacketEntity
import com.example.ui.components.BleMeshVisualizer
import com.example.ui.theme.RescueOrange
import com.example.ui.theme.SafetyCyan

@Composable
fun BleMeshScreen(
    meshPackets: List<BleMeshPacketEntity>,
    activePeers: List<MeshPeerNode>,
    meshEvents: List<BleMeshRelayEvent>,
    onSimulateHop: (BleMeshPacketEntity, MeshPeerNode) -> Unit,
    onAddPeer: (String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("ble_mesh_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "STORE-AND-FORWARD MESH",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SafetyCyan
                )
                Text(
                    text = "BLE Peer Relay Network",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "When mobile signal drops to 0 bars, SOS packets hop opportunistically across nearby tourist devices until a gateway is reached.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Mesh Visualizer Component
        item {
            BleMeshVisualizer(
                meshPackets = meshPackets,
                activePeers = activePeers,
                meshEvents = meshEvents,
                onSimulateHop = onSimulateHop,
                onAddPeer = onAddPeer
            )
        }

        // 3. Stored Mesh Packets
        item {
            Text(
                text = "Buffered BLE Packets in Local Room DB (${meshPackets.size}):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (meshPackets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PolishSubtleBorder)
                ) {
                    Text(
                        text = "No buffered mesh packets. Trigger an SOS alarm from the Radar tab to initiate store-and-forward hopping.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(meshPackets) { packet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mesh_packet_item_${packet.packetId}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PolishSubtleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = packet.packetId,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RescueOrange
                        )
                        Text(
                            text = packet.payloadSummary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hop ${packet.currentHop}/${packet.maxHops} • TTL: ${packet.ttlMinutes} mins • Relayed By: ${packet.relayedByNodeId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
