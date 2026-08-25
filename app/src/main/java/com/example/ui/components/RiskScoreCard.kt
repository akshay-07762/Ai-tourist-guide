package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.AiSafetyAdvisory
import com.example.data.engine.RiskAnalysisResult
import com.example.data.model.RiskLevel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.RescueOrange
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafetyCyan
import com.example.ui.theme.WarningAmber

@Composable
fun RiskScoreCard(
    riskAnalysis: RiskAnalysisResult,
    aiAdvisory: AiSafetyAdvisory?,
    isGeneratingAi: Boolean,
    onRequestAiCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = riskAnalysis.riskScore.toFloat(),
        animationSpec = tween(700),
        label = "risk_score_anim"
    )

    val riskColor by animateColorAsState(
        targetValue = when (riskAnalysis.riskLevel) {
            RiskLevel.CRITICAL -> AlertRed
            RiskLevel.HIGH -> RescueOrange
            RiskLevel.MEDIUM -> WarningAmber
            RiskLevel.LOW -> SafetyCyan
            RiskLevel.SAFE -> SafeGreen
        },
        label = "risk_color_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("risk_score_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.PolishSubtleBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Safety Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(riskColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (riskAnalysis.riskLevel) {
                                RiskLevel.SAFE -> Icons.Default.Shield
                                RiskLevel.CRITICAL -> Icons.Default.Warning
                                else -> Icons.Default.Info
                            },
                            contentDescription = "Risk Level",
                            tint = riskColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI SAFETY MONITOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = riskAnalysis.safetySummary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Status Badge Pill
                Surface(
                    color = riskColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, riskColor)
                ) {
                    Text(
                        text = riskAnalysis.riskLevel.displayName,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gauge & Risk Breakdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Risk Gauge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        // Background Track
                        drawArc(
                            color = Color(0xFFE2E8F0),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Active Score Sweep
                        val sweep = (animatedScore / 100f) * 270f
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(SafeGreen, WarningAmber, RescueOrange, AlertRed)
                            ),
                            startAngle = 135f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${animatedScore.toInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = riskColor
                        )
                        Text(
                            text = "/ 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Risk Factors List
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Active Risk Factors:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    riskAnalysis.riskFactors.take(3).forEach { factor ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", color = riskColor, fontWeight = FontWeight.Bold)
                            Text(
                                text = factor,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Advice Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (riskAnalysis.riskLevel >= RiskLevel.HIGH) AlertRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (riskAnalysis.riskLevel >= RiskLevel.HIGH) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = riskAnalysis.recommendedAction,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // AI Situational Directive Section (if available)
            if (aiAdvisory != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafetyCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = SafetyCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (aiAdvisory.isAiGenerated) "Gemini Safety Advisory" else "Offline Safety Protocol",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SafetyCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "ON-DEVICE / CLOUD",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = aiAdvisory.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        aiAdvisory.immediateSteps.take(2).forEach { step ->
                            Text(
                                text = "→ $step",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Safety Directives Button
            Button(
                onClick = onRequestAiCheck,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_safety_check_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isGeneratingAi) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing Terrain Threat Matrix...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = RescueOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-Evaluate AI Situational Advisory", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
