package com.priestemra.ui.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun GridAndLevelOverlay(
    showGrid: Boolean,
    showLevel: Boolean,
    tiltDegrees: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. 3x3 Rule of Thirds Grid Lines
        if (showGrid) {
            val gridColor = Color.White.copy(alpha = 0.28f)
            val strokeW = 1.dp.toPx()

            // Vertical lines
            drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeW)
            drawLine(gridColor, Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), strokeW)

            // Horizontal lines
            drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeW)
            drawLine(gridColor, Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), strokeW)
        }

        // 2. Horizon Level Indicator
        if (showLevel) {
            val isLevel = abs(tiltDegrees) < 1.0f
            val levelColor = if (isLevel) Color(0xFF00E676) else Color.White.copy(alpha = 0.6f)
            val lineLength = w * 0.28f
            val center = Offset(w / 2f, h / 2f)

            rotate(degrees = tiltDegrees, pivot = center) {
                // Left wing
                drawLine(
                    color = levelColor,
                    start = Offset(center.x - lineLength, center.y),
                    end = Offset(center.x - lineLength * 0.2f, center.y),
                    strokeWidth = 2.dp.toPx()
                )

                // Right wing
                drawLine(
                    color = levelColor,
                    start = Offset(center.x + lineLength * 0.2f, center.y),
                    end = Offset(center.x + lineLength, center.y),
                    strokeWidth = 2.dp.toPx()
                )

                // Center cross / level dot
                drawCircle(
                    color = levelColor,
                    radius = 3.dp.toPx(),
                    center = center
                )
            }
        }
    }
}
