package com.priestemra.ui.camera.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priestemra.camera.NightModeState
import com.priestemra.ui.theme.SamsungOverlay
import com.priestemra.ui.theme.SamsungWhite
import com.priestemra.ui.theme.SamsungYellow

@Composable
fun NightControls(
    state: NightModeState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Moon badge with exposure duration
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SamsungOverlay)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Nightlight,
                    contentDescription = "Night Mode",
                    tint = SamsungYellow,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${state.estimatedDurationSec}s",
                    color = SamsungYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                if (state.isTripodDetected) {
                    Text(
                        text = "• Tripod",
                        color = SamsungWhite,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Multi-frame capture progress overlay
        AnimatedVisibility(
            visible = state.isCapturingMultiFrame,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SamsungOverlay)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { state.captureProgress },
                        color = SamsungYellow,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Hold your phone steady...",
                        color = SamsungWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
