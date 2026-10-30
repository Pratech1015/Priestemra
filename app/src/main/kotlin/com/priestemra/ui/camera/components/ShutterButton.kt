package com.priestemra.ui.camera.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.priestemra.camera.CameraMode
import com.priestemra.ui.theme.SamsungRed
import com.priestemra.ui.theme.SamsungWhite
import com.priestemra.ui.theme.SamsungWhite85

@Composable
fun ShutterButton(
    mode: CameraMode,
    isRecording: Boolean,
    onShutterClick: () -> Unit,
    onBurstStart: (() -> Unit)? = null,
    onBurstStop: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isBursting by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(),
        label = "shutterScale"
    )

    val innerCornerRadius by animateDpAsState(
        targetValue = if (isRecording) 8.dp else 34.dp,
        animationSpec = spring(),
        label = "innerCornerRadius"
    )

    val innerSize by animateDpAsState(
        targetValue = if (isRecording) 32.dp else 68.dp,
        animationSpec = spring(),
        label = "innerSize"
    )

    Box(
        modifier = modifier
            .size(88.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val released = tryAwaitRelease()
                        isPressed = false
                        if (isBursting) {
                            isBursting = false
                            onBurstStop?.invoke()
                        } else if (released) {
                            onShutterClick()
                        }
                    },
                    onLongPress = {
                        if (mode != CameraMode.VIDEO && !isRecording && onBurstStart != null) {
                            isBursting = true
                            onBurstStart()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(88.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer white ring
            drawCircle(
                color = SamsungWhite85,
                radius = 42.dp.toPx(),
                center = center,
                style = Stroke(width = 4.5.dp.toPx())
            )

            // Inner element based on mode
            when {
                isRecording -> {
                    // Red Stop Square
                    val squareSizePx = innerSize.toPx()
                    val cornerRadiusPx = innerCornerRadius.toPx()
                    drawRoundRect(
                        color = SamsungRed,
                        topLeft = Offset(center.x - squareSizePx / 2f, center.y - squareSizePx / 2f),
                        size = Size(squareSizePx, squareSizePx),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )
                }
                mode == CameraMode.VIDEO -> {
                    // White circle with center red dot
                    drawCircle(
                        color = SamsungWhite,
                        radius = 33.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = SamsungRed,
                        radius = 14.dp.toPx(),
                        center = center
                    )
                }
                else -> {
                    // Solid white circle
                    drawCircle(
                        color = SamsungWhite,
                        radius = 33.dp.toPx(),
                        center = center
                    )
                }
            }
        }
    }
}
