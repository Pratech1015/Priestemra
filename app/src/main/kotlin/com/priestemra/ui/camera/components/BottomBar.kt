package com.priestemra.ui.camera.components

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.priestemra.camera.CameraMode
import com.priestemra.ui.theme.*

@Composable
fun BottomBar(
    mode: CameraMode,
    isRecording: Boolean,
    isVideoPaused: Boolean = false,
    isFrontCamera: Boolean,
    lastMediaUri: Uri?,
    onShutterClick: () -> Unit,
    onSwitchCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onPauseResumeVideo: () -> Unit = {},
    onVideoSnapshot: () -> Unit = {},
    onBurstStart: (() -> Unit)? = null,
    onBurstStop: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var cameraFlipRotation by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = cameraFlipRotation,
        animationSpec = tween(durationMillis = 350),
        label = "cameraFlip"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRecording) {
            // Video Mode Active: Left is Pause / Resume
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SamsungOverlay)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPauseResumeVideo
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVideoPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                    contentDescription = if (isVideoPaused) "Resume Video" else "Pause Video",
                    tint = SamsungWhite,
                    modifier = Modifier.size(26.dp)
                )
            }
        } else {
            // Left: Gallery Thumbnail (56dp circular)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SamsungOverlay)
                    .border(2.dp, SamsungWhite40, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onGalleryClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (lastMediaUri != null) {
                    AsyncImage(
                        model = lastMediaUri,
                        contentDescription = "Last captured photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "Gallery",
                        tint = SamsungWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Center: Samsung-style Shutter Button
        ShutterButton(
            mode = mode,
            isRecording = isRecording,
            onShutterClick = onShutterClick,
            onBurstStart = onBurstStart,
            onBurstStop = onBurstStop
        )

        if (isRecording) {
            // Video Mode Active: Right is Snapshot Still Capture Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SamsungOverlay)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onVideoSnapshot
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = "Take Snapshot",
                    tint = SamsungWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Right: Camera switch / flip button (48dp with 3D rotation)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SamsungOverlay)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            cameraFlipRotation += 180f
                            onSwitchCameraClick()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FlipCameraAndroid,
                    contentDescription = "Switch Camera",
                    tint = SamsungWhite,
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            rotationY = animatedRotation
                        }
                )
            }
        }
    }
}
