package com.priestemra.ui.camera.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priestemra.camera.CameraSettings
import com.priestemra.ui.theme.*

private val OneUiBackground = Color(0xFF000000)
private val OneUiCard = Color(0xFF1C1C1E)
private val OneUiDivider = Color(0xFF2C2C2E)
private val OneUiAccent = SamsungYellow

@Composable
fun SettingsScreen(
    settings: CameraSettings,
    onSettingsChanged: (CameraSettings) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val scrollState = rememberScrollState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OneUiBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // One UI Top Navigation Bar (Fixed)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate up",
                    tint = SamsungWhite
                )
            }
        }

        // One UI Signature Expanded Large Header (Fixed Viewing Area)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 14.dp)
        ) {
            Text(
                text = "Camera settings",
                color = SamsungWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Priestemra Flagship Computational Sensor",
                color = SamsungWhite60,
                fontSize = 13.sp
            )
        }

        // Scrollable One UI Settings Cards
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // SECTION 1: INTELLIGENT FEATURES
            OneUiSectionTitle("INTELLIGENT FEATURES")
            OneUiCardContainer {
                OneUiToggleRow(
                    title = "AI mode",
                    subtitle = "Reconstructs ultra-detailed flagship-grade resolution and micro-textures on any camera",
                    checked = settings.enableAiFlagshipUpscaler,
                    onCheckedChange = { onSettingsChanged(settings.copy(enableAiFlagshipUpscaler = it)) }
                )

                OneUiDividerLine()

                OneUiToggleRow(
                    title = "Auto HDR",
                    subtitle = "Capture more detail in bright and dark areas via exposure fusion",
                    checked = settings.autoHdr,
                    onCheckedChange = { onSettingsChanged(settings.copy(autoHdr = it)) }
                )

                OneUiDividerLine()

                OneUiToggleRow(
                    title = "Watermark",
                    subtitle = "PRIESTEMRA | COMPUTATIONAL SENSOR",
                    checked = settings.enableWatermark,
                    onCheckedChange = { onSettingsChanged(settings.copy(enableWatermark = it)) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: PICTURES
            OneUiSectionTitle("PICTURES")
            OneUiCardContainer {
                OneUiClickableRow(
                    title = "Swipe Shutter button to",
                    subtitle = "Take burst shot",
                    onClick = {}
                )

                OneUiDividerLine()

                OneUiToggleRow(
                    title = "High efficiency pictures",
                    subtitle = "Save space by capturing photos in HEIF format",
                    checked = settings.highEfficiencyPictures,
                    onCheckedChange = { onSettingsChanged(settings.copy(highEfficiencyPictures = it)) }
                )

                OneUiDividerLine()

                OneUiToggleRow(
                    title = "RAW (DNG) copies",
                    subtitle = "Save uncompressed 16-bit DNG files alongside JPEG in Pro mode",
                    checked = settings.enableRawDngCopies,
                    onCheckedChange = { onSettingsChanged(settings.copy(enableRawDngCopies = it)) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 3: GENERAL & COMPOSITION
            OneUiSectionTitle("GENERAL & COMPOSITION")
            OneUiCardContainer {
                OneUiToggleRow(
                    title = "Grid lines",
                    subtitle = "Show 3x3 Rule of Thirds grid on viewfinder",
                    checked = settings.showGridLines,
                    onCheckedChange = { onSettingsChanged(settings.copy(showGridLines = it)) }
                )

                OneUiDividerLine()

                OneUiToggleRow(
                    title = "Horizon level indicator",
                    subtitle = "Show orientation level line on viewfinder (turns green when level)",
                    checked = settings.showLevelIndicator,
                    onCheckedChange = { onSettingsChanged(settings.copy(showLevelIndicator = it)) }
                )

                OneUiDividerLine()

                OneUiToggleRow(
                    title = "Histogram in Pro mode",
                    subtitle = "Real-time shadow and highlight distribution curve",
                    checked = settings.showHistogram,
                    onCheckedChange = { onSettingsChanged(settings.copy(showHistogram = it)) }
                )

                OneUiDividerLine()

                OneUiToggleRow(
                    title = "Shutter sound",
                    subtitle = "Play sound when capturing photos or starting video",
                    checked = settings.shutterSound,
                    onCheckedChange = { onSettingsChanged(settings.copy(shutterSound = it)) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4: ABOUT PRIESTEMRA
            OneUiSectionTitle("ABOUT")
            OneUiCardContainer {
                OneUiClickableRow(
                    title = "About Camera",
                    subtitle = "Priestemra v1.0.0 (Flagship Build)",
                    onClick = {}
                )

                OneUiDividerLine()

                OneUiClickableRow(
                    title = "Open source licenses",
                    subtitle = "CameraX, Jetpack Compose, Vulkan NDK",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun OneUiSectionTitle(text: String) {
    Text(
        text = text,
        color = SamsungWhite60,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 28.dp, bottom = 8.dp)
    )
}

@Composable
private fun OneUiCardContainer(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = OneUiCard,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun OneUiToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                color = SamsungWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = SamsungWhite60,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SamsungBlack,
                checkedTrackColor = OneUiAccent,
                uncheckedThumbColor = SamsungWhite60,
                uncheckedTrackColor = SamsungOverlay,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun OneUiClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                color = SamsungWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = SamsungWhite60,
                    fontSize = 13.sp
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = SamsungWhite40,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun OneUiDividerLine() {
    HorizontalDivider(
        color = OneUiDivider,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 20.dp)
    )
}
