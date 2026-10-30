package com.priestemra.ui.camera.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.priestemra.camera.MeteringMode
import com.priestemra.camera.ProControl
import com.priestemra.camera.ProControlState
import com.priestemra.ui.theme.*

@Composable
fun ProControls(
    state: ProControlState,
    onControlSelected: (ProControl?) -> Unit,
    onIsoChanged: (Int) -> Unit,
    onShutterSpeedChanged: (Int) -> Unit,
    onEvChanged: (Int) -> Unit,
    onFocusChanged: (Float) -> Unit,
    onWbChanged: (Int) -> Unit,
    onMeteringChanged: (MeteringMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expandable fine-tuning slider/dial
        AnimatedVisibility(
            visible = state.selectedControl != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SamsungCard)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                when (state.selectedControl) {
                    ProControl.ISO -> {
                        IsoSelector(
                            currentIso = state.iso,
                            onIsoSelected = onIsoChanged
                        )
                    }
                    ProControl.SPEED -> {
                        ShutterSpeedSelector(
                            currentIndex = state.shutterSpeedIndex,
                            onSpeedSelected = onShutterSpeedChanged
                        )
                    }
                    ProControl.EV -> {
                        EvSlider(
                            currentStep = state.exposureCompensation,
                            onEvStepChanged = onEvChanged
                        )
                    }
                    ProControl.FOCUS -> {
                        FocusSlider(
                            currentDistance = state.manualFocusDistance,
                            onFocusChanged = onFocusChanged
                        )
                    }
                    ProControl.WB -> {
                        WbSelector(
                            currentKelvin = state.whiteBalanceKelvin,
                            onWbSelected = onWbChanged
                        )
                    }
                    ProControl.METERING -> {
                        MeteringSelector(
                            currentMode = state.meteringMode,
                            onModeSelected = onMeteringChanged
                        )
                    }
                    null -> {}
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontal strip of Pro controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SamsungOverlay)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProControl.values().forEach { control ->
                val isSelected = state.selectedControl == control
                val valueLabel = when (control) {
                    ProControl.ISO -> if (state.iso == 0) "AUTO" else "${state.iso}"
                    ProControl.SPEED -> ProControlState.SHUTTER_SPEEDS[state.shutterSpeedIndex]
                    ProControl.EV -> {
                        val ev = state.exposureCompensation * 0.33f
                        if (state.exposureCompensation == 0) "0.0" else String.format("%+.1f", ev)
                    }
                    ProControl.FOCUS -> if (state.manualFocusDistance < 0f) "AF" else "MF"
                    ProControl.WB -> if (state.whiteBalanceKelvin == 0) "AUTO" else "${state.whiteBalanceKelvin}K"
                    ProControl.METERING -> state.meteringMode.label
                }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0x33FFD500) else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onControlSelected(if (isSelected) null else control)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = control.label,
                        color = if (isSelected) SamsungYellow else SamsungWhite85,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = valueLabel,
                        color = if (isSelected) SamsungYellow else SamsungWhite60,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun IsoSelector(
    currentIso: Int,
    onIsoSelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(ProControlState.ISO_VALUES) { _, iso ->
            val isSelected = iso == currentIso
            val label = if (iso == 0) "AUTO" else "$iso"
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) SamsungYellow else Color(0x33FFFFFF))
                    .clickable { onIsoSelected(iso) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) SamsungBlack else SamsungWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ShutterSpeedSelector(
    currentIndex: Int,
    onSpeedSelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(ProControlState.SHUTTER_SPEEDS) { index, speed ->
            val isSelected = index == currentIndex
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) SamsungYellow else Color(0x33FFFFFF))
                    .clickable { onSpeedSelected(index) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = speed,
                    color = if (isSelected) SamsungBlack else SamsungWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EvSlider(
    currentStep: Int,
    onEvStepChanged: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val ev = currentStep * 0.33f
        val display = if (currentStep == 0) "0.0 EV" else String.format("%+.1f EV", ev)
        Text(
            text = display,
            color = SamsungYellow,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = currentStep.toFloat(),
            onValueChange = { onEvStepChanged(it.toInt()) },
            valueRange = -6f..6f,
            steps = 11,
            colors = SliderDefaults.colors(
                thumbColor = SamsungYellow,
                activeTrackColor = SamsungYellow,
                inactiveTrackColor = SamsungWhite40
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun FocusSlider(
    currentDistance: Float,
    onFocusChanged: (Float) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Macro", color = SamsungWhite60, fontSize = 11.sp)
            Text(
                text = if (currentDistance < 0f) "AUTO FOCUS" else "MANUAL: ${String.format("%.1f", currentDistance)}",
                color = SamsungYellow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Infinity", color = SamsungWhite60, fontSize = 11.sp)
        }
        Slider(
            value = if (currentDistance < 0f) 0f else currentDistance,
            onValueChange = { onFocusChanged(it) },
            valueRange = 0f..10f,
            colors = SliderDefaults.colors(
                thumbColor = SamsungYellow,
                activeTrackColor = SamsungYellow,
                inactiveTrackColor = SamsungWhite40
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun WbSelector(
    currentKelvin: Int,
    onWbSelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(ProControlState.WB_PRESETS) { _, kelvin ->
            val isSelected = kelvin == currentKelvin
            val label = if (kelvin == 0) "AUTO" else "${kelvin}K"
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) SamsungYellow else Color(0x33FFFFFF))
                    .clickable { onWbSelected(kelvin) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) SamsungBlack else SamsungWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MeteringSelector(
    currentMode: MeteringMode,
    onModeSelected: (MeteringMode) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MeteringMode.values().forEach { mode ->
            val isSelected = mode == currentMode
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) SamsungYellow else Color(0x33FFFFFF))
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    color = if (isSelected) SamsungBlack else SamsungWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
