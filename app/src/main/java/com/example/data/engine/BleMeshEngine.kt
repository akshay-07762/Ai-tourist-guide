package com.example.data.engine

import com.example.data.crypto.DigitalIdManager
import com.example.data.db.BleMeshDao
import com.example.data.db.IncidentDao
import com.example.data.model.BleMeshPacketEntity
import com.example.data.model.CommunicationChannel
import com.example.data.model.EmergencyType
import com.example.data.model.IncidentEntity
import com.example.data.model.RiskLevel
import com.example.data.model.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class MeshPeerNode(
    val nodeId: String,
    val name: String,
    val type: String, // TOURIST_DEVICE, VALLEY_REPEATER, RANGER_PATROL, CLOUD_GATEWAY
    val rssiDb: Int,
    val distanceApproxMeters: Int,
    val hasInternetAccess: Boolean,
    val lastSeenSecondsAgo: Int
)

data class BleMeshRelayEvent(
    val eventId: String = UUID.randomUUID().toString().take(8),
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // PACKET_BROADCAST, PEER_DISCOVERED, PACKET_STORED, HOP_FORWARDED, CLOUD_UPLOADED, DUPLICATE_DROPPED
    val description: String,
    val nodeId: String
)

class BleMeshEngine(
    private val bleMeshDao: BleMeshDao,
    private val incidentDao: IncidentDao
) {
    private val _meshEvents = MutableStateFlow<List<BleMeshRelayEvent>>(emptyList())
    val meshEvents: StateFlow<List<BleMeshRelayEvent>> = _meshEvents.asStateFlow()

    private val _activePeers = MutableStateFlow<List<MeshPeerNode>>(
        listOf(
            MeshPeerNode("NODE-B204", "Tourist Trekker (Priya S.)", "TOURIST_DEVICE", -72, 28, false, 2),
            MeshPeerNode("NODE-R08", "Forest Ranger Mobile Patrol", "RANGER_PATROL", -84, 95, false, 8),
            MeshPeerNode("NODE-GATE-01", "Avalanche Valley Satellite Hub", "CLOUD_GATEWAY", -91, 140, true, 14)
        )
    )
    val activePeers: StateFlow<List<MeshPeerNode>> = _activePeers.asStateFlow()

    private fun logEvent(type: String, description: String, nodeId: String) {
        val event = BleMeshRelayEvent(
            eventType = type,
            description = description,
            nodeId = nodeId
        )
        _meshEvents.value = (listOf(event) + _meshEvents.value).take(50)
    }

    /**
     * Creates an initial BLE Store-and-Forward SOS packet when internet is offline.
     */
    suspend fun broadcastSosPacket(incident: IncidentEntity): BleMeshPacketEntity {
        val packetId = "PKT-${incident.incidentId}-${System.currentTimeMillis().toString().takeLast(4)}"
        val signature = "SIG_ED25519_" + DigitalIdManager.generateSha256Hash("${incident.incidentId}|${incident.touristId}|${incident.latitude}|${incident.longitude}").take(24)

        val packet = BleMeshPacketEntity(
            packetId = packetId,
            incidentId = incident.incidentId,
            sourceTouristId = incident.touristId,
            targetBroadcast = "ALL_MESH_NODES",
            currentHop = 1,
            maxHops = 8,
            ttlMinutes = 45,
            creationTimestamp = incident.timestamp,
            receivedTimestamp = System.currentTimeMillis(),
            forwardedTimestamp = null,
            relayedByNodeId = "LOCAL_DEVICE",
            payloadSummary = "${incident.emergencyType.label} | Lat:${incident.latitude}, Lng:${incident.longitude} | Risk:${incident.riskLevel.displayName}",
            isDeliveredToCloud = false,
            signatureValid = true
        )

        bleMeshDao.insertMeshPacket(packet)
        logEvent(
            type = "PACKET_BROADCAST",
            description = "🚨 SOS BLE packet broadcasted: ${incident.incidentId} (TTL: 45m, Max Hops: 8)",
            nodeId = "LOCAL_DEVICE"
        )
        return packet
    }

    /**
     * Simulates receiving a packet from a nearby peer or forwarding an existing packet.
     * Demonstrates store-and-forward + duplicate filtering.
     */
    suspend fun simulateStoreAndForwardHop(
        sourcePacket: BleMeshPacketEntity,
        relayNode: MeshPeerNode
    ): Boolean {
        // 1. Check for Duplicate Packet
        val existing = bleMeshDao.findPacketByIncidentId(sourcePacket.incidentId)
        if (existing != null && existing.currentHop >= sourcePacket.currentHop + 1) {
            logEvent(
                type = "DUPLICATE_DROPPED",
                description = "Duplicate packet ${sourcePacket.incidentId} dropped from ${relayNode.nodeId} (Already stored)",
                nodeId = relayNode.nodeId
            )
            return false
        }

        // 2. Check Hop Limit
        if (sourcePacket.currentHop >= sourcePacket.maxHops) {
            logEvent(
                type = "HOP_EXPIRED",
                description = "Packet ${sourcePacket.incidentId} reached max hop count (${sourcePacket.maxHops})",
                nodeId = relayNode.nodeId
            )
            return false
        }

        // 3. Store and Forward
        val newHopPacket = sourcePacket.copy(
            packetId = "PKT-${sourcePacket.incidentId}-HOP${sourcePacket.currentHop + 1}",
            currentHop = sourcePacket.currentHop + 1,
            relayedByNodeId = relayNode.nodeId,
            receivedTimestamp = System.currentTimeMillis(),
            forwardedTimestamp = System.currentTimeMillis(),
            isDeliveredToCloud = relayNode.hasInternetAccess
        )
        bleMeshDao.insertMeshPacket(newHopPacket)

        logEvent(
            type = if (relayNode.hasInternetAccess) "CLOUD_UPLOADED" else "HOP_FORWARDED",
            description = if (relayNode.hasInternetAccess)
                "🌐 Node ${relayNode.name} connected to Cloud! Uplinked incident ${sourcePacket.incidentId} to Control Centre."
            else
                "🔁 Stored & Forwarded by ${relayNode.name} (Hop ${newHopPacket.currentHop}/${newHopPacket.maxHops})",
            nodeId = relayNode.nodeId
        )

        // If relay node has internet, sync the incident
        if (relayNode.hasInternetAccess) {
            incidentDao.updateIncidentSyncStatus(sourcePacket.incidentId, SyncStatus.SYNCED)
        }

        return true
    }

    /**
     * Injects a peer encounter for user demonstration.
     */
    fun addSimulatedPeer(name: String, role: String, hasNet: Boolean) {
        val newNode = MeshPeerNode(
            nodeId = "NODE-" + UUID.randomUUID().toString().take(4).uppercase(),
            name = name,
            type = role,
            rssiDb = -65,
            distanceApproxMeters = 15,
            hasInternetAccess = hasNet,
            lastSeenSecondsAgo = 0
        )
        _activePeers.value = listOf(newNode) + _activePeers.value
        logEvent("PEER_DISCOVERED", "Discovered nearby peer mesh node: $name (${role})", newNode.nodeId)
    }
}
