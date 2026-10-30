package com.priestemra.ui.camera.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.priestemra.ui.theme.SamsungYellow

@Composable
fun FocusRing(
    position: Offset,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "focusPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "focusRingScale"
    )

    val ringSize = 72.dp

    Canvas(
        modifier = modifier
            .offset {
                IntOffset(
                    (position.x - ringSize.toPx() / 2f).toInt(),
                    (position.y - ringSize.toPx() / 2f).toInt()
                )
            }
            .size(ringSize)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (size.width / 2f - 4.dp.toPx()) * scale

        // Main focus circle
        drawCircle(
            color = SamsungYellow,
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Center dot
        drawCircle(
            color = SamsungYellow,
            radius = 3.dp.toPx(),
            center = center
        )

        // 4 outer corner ticks (Samsung style)
        val tickLength = 6.dp.toPx()
        // Top
        drawLine(
            color = SamsungYellow,
            start = Offset(center.x, center.y - radius - tickLength),
            end = Offset(center.x, center.y - radius),
            strokeWidth = 2.dp.toPx()
        )
        // Bottom
        drawLine(
            color = SamsungYellow,
            start = Offset(center.x, center.y + radius),
            end = Offset(center.x, center.y + radius + tickLength),
            strokeWidth = 2.dp.toPx()
        )
        // Left
        drawLine(
            color = SamsungYellow,
            start = Offset(center.x - radius - tickLength, center.y),
            end = Offset(center.x - radius, center.y),
            strokeWidth = 2.dp.toPx()
        )
        // Right
        drawLine(
            color = SamsungYellow,
            start = Offset(center.x + radius, center.y),
            end = Offset(center.x + radius + tickLength, center.y),
            strokeWidth = 2.dp.toPx()
        )
    }
}
