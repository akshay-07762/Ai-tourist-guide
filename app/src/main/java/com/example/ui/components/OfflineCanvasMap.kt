package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeoFenceEntity
import com.example.data.model.LocationLogEntity
import com.example.data.model.RiskLevel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.RescueOrange
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafetyCyan
import com.example.ui.theme.WarningAmber
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OfflineCanvasMap(
    modifier: Modifier = Modifier,
    currentLocation: LocationLogEntity,
    geoFences: List<GeoFenceEntity>,
    selectedZone: GeoFenceEntity? = null,
    onZoneClick: (GeoFenceEntity) -> Unit = {},
    onLocationTapSimulate: (Double, Double) -> Unit = { _, _ -> },
    showTopoContours: Boolean = true
) {
    // Map view parameters
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0.0f) }
    var panOffsetY by remember { mutableFloatStateOf(0.0f) }

    // Pulsing animation for active GPS beacon & hazard zones
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beacon_pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beacon_alpha"
    )

    val textMeasurer = rememberTextMeasurer()

    // Reference center of Ooty hill station
    val baseLat = 11.3800
    val baseLng = 76.6500
    val latLngToPixelsRatio = 2200.0f * zoomScale

    Box(
        modifier = modifier
            .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    panOffsetX += dragAmount.x
                    panOffsetY += dragAmount.y
                }
            }
            .testTag("offline_canvas_map")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenterX = size.width / 2f + panOffsetX
            val canvasCenterY = size.height / 2f + panOffsetY

            // Helper to project GPS (lat, lng) to canvas pixels
            fun project(lat: Double, lng: Double): Offset {
                val x = canvasCenterX + ((lng - baseLng) * latLngToPixelsRatio).toFloat()
                val y = canvasCenterY - ((lat - baseLat) * latLngToPixelsRatio).toFloat()
                return Offset(x, y)
            }

            // 1. Draw Mountain Topo Elevation Contours
            if (showTopoContours) {
                drawTopoElevationContours(canvasCenterX, canvasCenterY, size.width, size.height, zoomScale)
            }

            // 2. Draw Trekking Trails / Route Lines
            drawTrekkingTrails(::project)

            // 3. Draw Geo-Fence Danger & Safe Zones
            for (fence in geoFences) {
                val centerOffset = project(fence.centerLat, fence.centerLng)
                val radiusPx = (fence.radiusMeters * 0.000009 * latLngToPixelsRatio).toFloat().coerceAtLeast(35f * zoomScale)

                val zoneColor = when {
                    fence.isSafeZone -> SafeGreen
                    fence.riskLevel == RiskLevel.CRITICAL -> AlertRed
                    fence.riskLevel == RiskLevel.HIGH -> RescueOrange
                    fence.riskLevel == RiskLevel.MEDIUM -> WarningAmber
                    else -> SafetyCyan
                }

                // Shaded hazard / safe polygon circle
                drawCircle(
                    color = zoneColor.copy(alpha = 0.22f),
                    radius = radiusPx,
                    center = centerOffset
                )

                // Perimeter dashed border
                drawCircle(
                    color = zoneColor.copy(alpha = 0.85f),
                    radius = radiusPx,
                    center = centerOffset,
                    style = Stroke(
                        width = 2.5f * zoomScale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                // Center Icon/Badge Label
                val labelText = when {
                    fence.isSafeZone -> "🛡️ " + fence.name.take(18)
                    fence.riskLevel == RiskLevel.CRITICAL -> "🚨 " + fence.name.take(18)
                    else -> "⚠️ " + fence.name.take(18)
                }

                val textLayoutResult = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(color = Color.White, fontSize = (10 * zoomScale).sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(centerOffset.x - textLayoutResult.size.width / 2f, centerOffset.y - 12f)
                )
            }

            // 4. Draw Peer BLE Mesh Nodes nearby
            val peer1 = project(currentLocation.latitude + 0.003, currentLocation.longitude - 0.002)
            drawCircle(color = SafetyCyan.copy(alpha = 0.8f), radius = 7f * zoomScale, center = peer1)
            drawCircle(color = SafetyCyan.copy(alpha = 0.3f), radius = 18f * zoomScale, center = peer1)
            val peerText = textMeasurer.measure(
                text = "📶 Peer Hiker (BLE Mesh)",
                style = TextStyle(color = SafetyCyan, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            )
            drawText(peerText, topLeft = Offset(peer1.x - peerText.size.width / 2f, peer1.y + 10f))

            // 5. Draw Tourist Current GPS Beacon
            val touristPos = project(currentLocation.latitude, currentLocation.longitude)

            // Animated radar ripple
            drawCircle(
                color = RescueOrange.copy(alpha = pulseAlpha),
                radius = pulseRadius * zoomScale,
                center = touristPos
            )

            // Tourist Outer ring
            drawCircle(
                color = Color.White,
                radius = 11f * zoomScale,
                center = touristPos
            )

            // Tourist Core Dot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RescueOrange, Color(0xFFD84315)),
                    center = touristPos,
                    radius = 9f * zoomScale
                ),
                radius = 8.5f * zoomScale,
                center = touristPos
            )

            // Tourist Label
            val youText = textMeasurer.measure(
                text = "📍 YOU (Alt: ${currentLocation.altitudeMeters.toInt()}m)",
                style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            )
            drawText(
                textLayoutResult = youText,
                topLeft = Offset(touristPos.x - youText.size.width / 2f, touristPos.y - 28f * zoomScale)
            )
        }

        // Map Overlay Controls (Zoom + Center)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.0f) },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.5f) },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    zoomScale = 1.0f
                },
                modifier = Modifier.size(40.dp),
                containerColor = RescueOrange,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center on GPS", modifier = Modifier.size(20.dp))
            }
        }

        // Top Map Badge (Offline Vector Pack Status)
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            color = Color(0xCC0F172A),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SafeGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Offline Topo Vector Map: Nilgiris & Ooty Hills",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun DrawScope.drawTopoElevationContours(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    zoom: Float
) {
    val contourColor = Color(0xFF1E293B)
    val majorContourColor = Color(0xFF334155)

    // Render topographical elevation contour loops
    for (i in 1..8) {
        val radiusX = (80f * i + 40f) * zoom
        val radiusY = (55f * i + 25f) * zoom
        val isMajor = i % 3 == 0

        val path = Path().apply {
            for (angle in 0..360 step 15) {
                val rad = Math.toRadians(angle.toDouble())
                // Subtle organic perturbation to simulate ridge contour lines
                val perturb = (sin(rad * 3) * 12.0 * zoom).toFloat()
                val px = centerX + (radiusX + perturb) * cos(rad).toFloat()
                val py = centerY + (radiusY + perturb) * sin(rad).toFloat()
                if (angle == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }

        drawPath(
            path = path,
            color = if (isMajor) majorContourColor else contourColor,
            style = Stroke(width = if (isMajor) 1.5f else 0.8f)
        )
    }
}

private fun DrawScope.drawTrekkingTrails(
    project: (Double, Double) -> Offset
) {
    val trailWaypoints = listOf(
        Pair(11.4010, 76.7360), // Doddabetta
        Pair(11.4102, 76.6950), // Tourist Point
        Pair(11.4280, 76.6520), // Mid Ridge
        Pair(11.4420, 76.6010), // Pykara
        Pair(11.3142, 76.5891), // Avalanche Base
        Pair(11.2350, 76.5380)  // Upper Bhavani Outpost
    )

    val path = Path()
    trailWaypoints.forEachIndexed { index, point ->
        val offset = project(point.first, point.second)
        if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
    }

    // Draw Trail Shadow
    drawPath(
        path = path,
        color = Color.Black.copy(alpha = 0.5f),
        style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), 0f))
    )

    // Draw Main Trail (Safety Amber Dash)
    drawPath(
        path = path,
        color = WarningAmber.copy(alpha = 0.9f),
        style = Stroke(width = 3.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), 0f))
    )
}
