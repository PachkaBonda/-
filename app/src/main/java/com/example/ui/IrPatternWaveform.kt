package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun IrPatternWaveform(pattern: List<Int>, modifier: Modifier = Modifier) {
    // Limit to first 40 pulses for visual clarity
    val cleanPattern = pattern.take(40) 
    val transitionState = rememberInfiniteTransition(label = "pulse")
    val translationFactor by transitionState.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_offset"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1C1B1F))
    ) {
        val path = Path()
        var currentX = 0f
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2f

        // Let's draw horizontal reference line
        drawLine(
            color = Color.DarkGray.copy(alpha = 0.3f),
            start = Offset(0f, centerY),
            end = Offset(canvasWidth, centerY),
            strokeWidth = 1f
        )

        // Calculate direct scaling factor for time intervals to fit completely in width
        val totalMs = cleanPattern.sum().toFloat()
        val scale = if (totalMs > 0) canvasWidth / totalMs else 1f

        path.moveTo(0f, centerY + 15f)

        var high = true
        for (pulse in cleanPattern) {
            val segmentWidth = pulse * scale
            val yPos = if (high) centerY - 15f else centerY + 15f
            
            // Draw square vertical jump
            path.lineTo(currentX, yPos)
            currentX += segmentWidth
            // Draw square horizontal run
            path.lineTo(currentX, yPos)

            high = !high
        }

        drawPath(
            path = path,
            color = Color(0xFF10B981),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Draw animated glowing laser tracking dot
        val dotX = (translationFactor * canvasWidth)
        drawCircle(
            color = Color(0xFFFF3366),
            radius = 4.dp.toPx(),
            center = Offset(dotX, centerY)
        )
    }
}
