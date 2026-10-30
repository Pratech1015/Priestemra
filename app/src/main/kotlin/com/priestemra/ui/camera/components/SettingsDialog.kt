package com.priestemra.ui.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.priestemra.camera.CameraSettings
import com.priestemra.ui.theme.*

@Composable
fun SettingsDialog(
    settings: CameraSettings,
    onSettingsChanged: (CameraSettings) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SamsungCard)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Camera Settings",
                    color = SamsungWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingToggleRow(
                    title = "Grid lines",
                    subtitle = "3x3 Rule of Thirds grid for framing",
                    checked = settings.showGridLines,
                    onCheckedChange = { onSettingsChanged(settings.copy(showGridLines = it)) }
                )

                SettingToggleRow(
                    title = "Horizon level indicator",
                    subtitle = "Align camera angle with optical horizon",
                    checked = settings.showLevelIndicator,
                    onCheckedChange = { onSettingsChanged(settings.copy(showLevelIndicator = it)) }
                )

                SettingToggleRow(
                    title = "Live histogram in Pro mode",
                    subtitle = "Real-time shadow and highlight distribution",
                    checked = settings.showHistogram,
                    onCheckedChange = { onSettingsChanged(settings.copy(showHistogram = it)) }
                )

                SettingToggleRow(
                    title = "Watermark",
                    subtitle = "Embed 'PRIESTEMRA | COMPUTATIONAL SENSOR'",
                    checked = settings.enableWatermark,
                    onCheckedChange = { onSettingsChanged(settings.copy(enableWatermark = it)) }
                )

                SettingToggleRow(
                    title = "RAW (DNG) copies",
                    subtitle = "Save uncompressed DNG alongside JPEG in Pro mode",
                    checked = settings.enableRawDngCopies,
                    onCheckedChange = { onSettingsChanged(settings.copy(enableRawDngCopies = it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SamsungYellow),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Text(
                        text = "Done",
                        color = SamsungBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                color = SamsungWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = SamsungWhite60,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SamsungBlack,
                checkedTrackColor = SamsungYellow,
                uncheckedThumbColor = SamsungWhite60,
                uncheckedTrackColor = SamsungOverlay
            )
        )
    }
}
