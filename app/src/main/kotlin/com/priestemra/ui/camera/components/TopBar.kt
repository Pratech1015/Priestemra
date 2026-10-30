package com.priestemra.ui.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priestemra.camera.AspectRatioMode
import com.priestemra.camera.FlashMode
import com.priestemra.ui.theme.SamsungOverlay
import com.priestemra.ui.theme.SamsungWhite
import com.priestemra.ui.theme.SamsungWhite85
import com.priestemra.ui.theme.SamsungYellow

@Composable
fun TopBar(
    flashMode: FlashMode,
    timerSeconds: Int,
    aspectRatio: AspectRatioMode,
    isFilterActive: Boolean,
    onFlashClick: () -> Unit,
    onTimerClick: () -> Unit,
    onAspectRatioClick: () -> Unit,
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flash Button
        TopBarIconButton(onClick = onFlashClick) {
            val (icon, tint) = when (flashMode) {
                FlashMode.OFF -> Icons.Outlined.FlashOff to SamsungWhite85
                FlashMode.AUTO -> Icons.Outlined.FlashAuto to SamsungYellow
                FlashMode.ON -> Icons.Outlined.FlashOn to SamsungYellow
            }
            Icon(
                imageVector = icon,
                contentDescription = "Flash",
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }

        // Timer Button
        TopBarIconButton(onClick = onTimerClick) {
            val isTimerActive = timerSeconds > 0
            val tint = if (isTimerActive) SamsungYellow else SamsungWhite85
            if (isTimerActive) {
                Text(
                    text = "${timerSeconds}s",
                    color = tint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = "Timer",
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Aspect Ratio Button
        TopBarIconButton(onClick = onAspectRatioClick) {
            Text(
                text = aspectRatio.label,
                color = SamsungWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Filter / Film Look Wand Button (Samsung Filter tray)
        TopBarIconButton(onClick = onFilterClick) {
            Icon(
                imageVector = Icons.Outlined.AutoFixHigh,
                contentDescription = "Film Filters",
                tint = if (isFilterActive) SamsungYellow else SamsungWhite85,
                modifier = Modifier.size(24.dp)
            )
        }

        // Settings Button
        TopBarIconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = SamsungWhite85,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(SamsungOverlay)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
