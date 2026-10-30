package com.priestemra.ui.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priestemra.ui.theme.SamsungOverlay
import com.priestemra.ui.theme.SamsungWhite
import com.priestemra.ui.theme.SamsungYellow

@Composable
fun ZoomControls(
    currentZoom: Float,
    availableZoomLevels: List<Float>,
    onZoomSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SamsungOverlay)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            availableZoomLevels.forEach { level ->
                val isSelected = kotlin.math.abs(currentZoom - level) < 0.15f
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) SamsungYellow else SamsungWhite,
                    label = "zoomTextColor"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0x33FFD500) else Color.Transparent,
                    label = "zoomBgColor"
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onZoomSelected(level) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val label = if (level == level.toInt().toFloat()) {
                        "${level.toInt()}"
                    } else {
                        "$level"
                    }
                    Text(
                        text = if (level < 1.0f) ".5" else label,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
