package com.priestemra.camera

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import com.priestemra.processing.FilterPreset

enum class CameraMode(val displayName: String) {
    PRO("PRO"),
    NIGHT("NIGHT"),
    PORTRAIT("PORTRAIT"),
    PHOTO("PHOTO"),
    VIDEO("VIDEO"),
    MORE("MORE")
}

enum class MoreMode(val title: String, val subtitle: String) {
    FOOD("Food", "Warm color tones and radial focus for cuisine"),
    PRO_VIDEO("Pro Video", "Full manual controls (ISO, shutter, audio) for video"),
    PANORAMA("Panorama", "Ultra-wide sweeping panoramic capture"),
    SLOW_MO("Slow Motion", "High frame-rate smooth slow motion"),
    HYPERLAPSE("Hyperlapse", "Stabilized time-lapse video on the move"),
    SINGLE_TAKE("Single Take", "Multi-lens AI captures photos and clips simultaneously")
}

enum class FlashMode {
    OFF,
    AUTO,
    ON
}

enum class AspectRatioMode(val label: String, val ratio: Float) {
    RATIO_3_4("3:4", 3f / 4f),
    RATIO_9_16("9:16", 9f / 16f),
    RATIO_1_1("1:1", 1f),
    RATIO_FULL("FULL", 0f) // Matches screen aspect ratio
}

enum class ProControl(val label: String) {
    ISO("ISO"),
    SPEED("SPEED"),
    EV("EV"),
    FOCUS("FOCUS"),
    WB("WB"),
    METERING("METER")
}

enum class MeteringMode(val label: String) {
    MATRIX("Matrix"),
    CENTER_WEIGHTED("Center"),
    SPOT("Spot")
}

enum class PortraitEffect(val label: String) {
    BLUR("Blur"),
    STUDIO("Studio"),
    HIGH_KEY("High-Key"),
    LOW_KEY("Low-Key"),
    COLOR_POINT("Color Point")
}

data class ProControlState(
    val selectedControl: ProControl? = null,
    val iso: Int = 0, // 0 = Auto, else 50, 100, 200, 400, 800, 1600, 3200
    val shutterSpeedIndex: Int = 0, // 0 = Auto, index into shutterSpeeds list
    val exposureCompensation: Int = 0, // In index steps (-6..+6 for -2EV..+2EV)
    val manualFocusDistance: Float = -1f, // -1f = AF, 0.0f..10.0f diopters
    val whiteBalanceKelvin: Int = 0, // 0 = Auto, else 2300K..10000K
    val meteringMode: MeteringMode = MeteringMode.MATRIX,
    val isRawEnabled: Boolean = false
) {
    companion object {
        val ISO_VALUES = listOf(0, 50, 100, 200, 400, 800, 1600, 3200)
        val SHUTTER_SPEEDS = listOf(
            "AUTO", "1/4000", "1/2000", "1/1000", "1/500", "1/250",
            "1/125", "1/60", "1/30", "1/15", "1/8", "1/4", "1/2", "1s", "2s", "4s"
        )
        val WB_PRESETS = listOf(0, 2800, 3200, 4000, 5500, 6500, 7500)
    }
}

data class NightModeState(
    val estimatedDurationSec: Int = 3,
    val isCapturingMultiFrame: Boolean = false,
    val captureProgress: Float = 0f,
    val isTripodDetected: Boolean = false
)

data class PortraitModeState(
    val blurLevel: Int = 5, // 0..7
    val effect: PortraitEffect = PortraitEffect.BLUR
)

data class CameraSettings(
    val enableAiFlagshipUpscaler: Boolean = true,
    val showGridLines: Boolean = true,
    val showLevelIndicator: Boolean = true,
    val showHistogram: Boolean = false,
    val enableWatermark: Boolean = true,
    val enableRawDngCopies: Boolean = false,
    val shutterSound: Boolean = false,
    val autoHdr: Boolean = true,
    val highEfficiencyPictures: Boolean = false
)

data class CameraUiState(
    val mode: CameraMode = CameraMode.PHOTO,
    val isFrontCamera: Boolean = false,
    val flashMode: FlashMode = FlashMode.OFF,
    val aspectRatio: AspectRatioMode = AspectRatioMode.RATIO_3_4,
    val timerSeconds: Int = 0, // 0, 2, 5, 10
    val timerCountdownRemaining: Int = 0,
    val zoomRatio: Float = 1.0f,
    val maxZoomRatio: Float = 10.0f,
    val minZoomRatio: Float = 0.5f,
    val availableZoomLevels: List<Float> = listOf(0.5f, 1.0f, 2.0f, 3.0f),
    val isRecordingVideo: Boolean = false,
    val isVideoPaused: Boolean = false,
    val recordingDurationSeconds: Int = 0,
    val isShutterFlashing: Boolean = false,
    val focusRingPosition: Offset? = null,
    val lastCapturedMediaUri: Uri? = null,
    val selectedFilter: FilterPreset = FilterPreset.ORIGINAL,
    val filterIntensity: Float = 1.0f,
    val isFilterTrayOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val settings: CameraSettings = CameraSettings(),
    val horizonTiltDegrees: Float = 0f,
    val histogramData: FloatArray = FloatArray(32) { (it * 3 % 20 + 5) / 25f },
    val proState: ProControlState = ProControlState(),
    val nightState: NightModeState = NightModeState(),
    val portraitState: PortraitModeState = PortraitModeState(),
    val isGalleryViewerOpen: Boolean = false,
    val activeMoreMode: MoreMode? = null,
    val isBurstCapturing: Boolean = false,
    val burstCount: Int = 0,
    val errorMessage: String? = null
)
