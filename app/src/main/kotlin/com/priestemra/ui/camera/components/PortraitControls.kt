package com.priestemra.ui.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.priestemra.camera.PortraitEffect
import com.priestemra.camera.PortraitModeState
import com.priestemra.ui.theme.*

@Composable
fun PortraitControls(
    state: PortraitModeState,
    onBlurLevelChanged: (Int) -> Unit,
    onEffectSelected: (PortraitEffect) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Blur intensity slider
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SamsungOverlay)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Blur: ${state.blurLevel}",
                    color = SamsungYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = state.blurLevel.toFloat(),
                    onValueChange = { onBlurLevelChanged(it.toInt()) },
                    valueRange = 0f..7f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = SamsungYellow,
                        activeTrackColor = SamsungYellow,
                        inactiveTrackColor = SamsungWhite40
                    ),
                    modifier = Modifier.width(160.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Portrait Effects Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(PortraitEffect.values()) { effect ->
                val isSelected = effect == state.effect
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) SamsungYellow else SamsungOverlay)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onEffectSelected(effect) }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = effect.label,
                        color = if (isSelected) SamsungBlack else SamsungWhite,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
