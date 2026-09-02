package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribTextMuted

@Composable
fun SaribLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    ringColor1: Color = SaribCyanAccent,
    ringColor2: Color = SaribElectricBlue,
    label: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sarib_loader")

    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot1"
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot2"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Outer Ring
            Canvas(
                modifier = Modifier
                    .size(size)
                    .rotate(rotation1)
            ) {
                val strokeWidth = 3.5.dp.toPx()
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            ringColor1.copy(alpha = 0.05f),
                            ringColor1.copy(alpha = 0.4f),
                            ringColor1
                        )
                    ),
                    startAngle = 0f,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Inner Reverse Ring
            Canvas(
                modifier = Modifier
                    .size(size * 0.65f)
                    .rotate(rotation2)
            ) {
                val strokeWidth = 2.5.dp.toPx()
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            ringColor2.copy(alpha = 0.05f),
                            ringColor2.copy(alpha = 0.5f),
                            ringColor2
                        )
                    ),
                    startAngle = 45f,
                    sweepAngle = 220f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Center Pulsing Core
            Canvas(
                modifier = Modifier
                    .size(size * 0.28f)
                    .scale(pulseScale)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(ringColor1, ringColor2, Color.Transparent)
                    )
                )
            }
        }

        if (!label.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = SaribTextMuted)
            )
        }
    }
}
