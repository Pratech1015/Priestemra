package com.priestemra.ui.camera

import android.content.ContentUris
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.priestemra.camera.*
import com.priestemra.processing.FilterPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.atan2

class CameraViewModel : ViewModel(), SensorEventListener {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var focusDismissJob: Job? = null
    private var timerJob: Job? = null
    private var nightCaptureJob: Job? = null
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    var cameraManager: CameraManager? = null

    fun initializeSensors(context: Context) {
        if (sensorManager == null) {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelerometer?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
        loadLatestMedia(context)
    }

    fun loadLatestMedia(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val projection = arrayOf(MediaStore.Images.Media._ID)
                val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                        _uiState.update { it.copy(lastCapturedMediaUri = uri) }
                    }
                }
            } catch (e: Exception) {
                // Ignore query error if storage not indexed
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            // Calculate tilt angle in degrees
            val angle = Math.toDegrees(atan2(x.toDouble(), y.toDouble())).toFloat()
            // Normalize around portrait (0 deg when upright)
            val tilt = when {
                angle in -45f..45f -> -angle
                angle > 45f && angle <= 135f -> -(angle - 90f)
                angle < -45f && angle >= -135f -> -(angle + 90f)
                else -> 0f
            }
            _uiState.update { it.copy(horizonTiltDegrees = tilt) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun onModeSelected(newMode: CameraMode) {
        if (newMode == _uiState.value.mode) return

        if (_uiState.value.isRecordingVideo) {
            stopVideoRecording()
        }

        _uiState.update { it.copy(mode = newMode) }

        if (newMode == CameraMode.PRO) {
            cameraManager?.applyProSettings(_uiState.value.proState)
        }
    }

    fun onFlashToggle() {
        val nextFlash = when (_uiState.value.flashMode) {
            FlashMode.OFF -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.OFF
        }
        _uiState.update { it.copy(flashMode = nextFlash) }
        cameraManager?.setFlashMode(nextFlash)
    }

    fun onTimerToggle() {
        val nextTimer = when (_uiState.value.timerSeconds) {
            0 -> 2
            2 -> 5
            5 -> 10
            else -> 0
        }
        _uiState.update { it.copy(timerSeconds = nextTimer) }
    }

    fun onAspectRatioToggle() {
        val modes = AspectRatioMode.values()
        val currentIndex = modes.indexOf(_uiState.value.aspectRatio)
        val nextMode = modes[(currentIndex + 1) % modes.size]
        _uiState.update { it.copy(aspectRatio = nextMode) }
    }

    fun onZoomSelected(ratio: Float) {
        val clamped = ratio.coerceIn(_uiState.value.minZoomRatio, _uiState.value.maxZoomRatio)
        _uiState.update { it.copy(zoomRatio = clamped) }
        cameraManager?.setZoomRatio(clamped)
    }

    fun onZoomPinchDelta(scaleFactor: Float) {
        val current = _uiState.value.zoomRatio
        val target = (current * scaleFactor).coerceIn(_uiState.value.minZoomRatio, _uiState.value.maxZoomRatio)
        _uiState.update { it.copy(zoomRatio = target) }
        cameraManager?.setZoomRatio(target)
    }

    fun onSwitchCamera() {
        val nextIsFront = !_uiState.value.isFrontCamera
        _uiState.update { it.copy(isFrontCamera = nextIsFront) }
    }

    fun onTapToFocus(offset: Offset, previewWidth: Float, previewHeight: Float) {
        _uiState.update { it.copy(focusRingPosition = offset) }
        cameraManager?.focusOnPoint(offset.x, offset.y, previewWidth, previewHeight)

        focusDismissJob?.cancel()
        focusDismissJob = viewModelScope.launch {
            delay(2500)
            _uiState.update { it.copy(focusRingPosition = null) }
        }
    }

    // Filter controls
    fun toggleFilterTray() {
        _uiState.update { it.copy(isFilterTrayOpen = !it.isFilterTrayOpen) }
    }

    fun onFilterSelected(preset: FilterPreset) {
        _uiState.update { it.copy(selectedFilter = preset) }
    }

    fun onFilterIntensityChanged(intensity: Float) {
        _uiState.update { it.copy(filterIntensity = intensity) }
    }

    private var burstJob: Job? = null

    fun openGalleryViewer() {
        if (_uiState.value.lastCapturedMediaUri != null) {
            _uiState.update { it.copy(isGalleryViewerOpen = true) }
        }
    }

    fun closeGalleryViewer() {
        _uiState.update { it.copy(isGalleryViewerOpen = false) }
    }

    fun onDeleteMedia(uri: Uri, context: Context) {
        try {
            context.contentResolver.delete(uri, null, null)
            _uiState.update {
                it.copy(
                    lastCapturedMediaUri = null,
                    isGalleryViewerOpen = false
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isGalleryViewerOpen = false) }
        }
    }

    fun onMoreModeSelected(moreMode: MoreMode) {
        when (moreMode) {
            MoreMode.FOOD -> {
                _uiState.update { it.copy(activeMoreMode = MoreMode.FOOD, mode = CameraMode.PHOTO) }
            }
            MoreMode.PRO_VIDEO -> {
                _uiState.update { it.copy(activeMoreMode = MoreMode.PRO_VIDEO, mode = CameraMode.VIDEO) }
            }
            MoreMode.SLOW_MO, MoreMode.HYPERLAPSE -> {
                _uiState.update { it.copy(activeMoreMode = moreMode, mode = CameraMode.VIDEO) }
            }
            else -> {
                _uiState.update { it.copy(activeMoreMode = moreMode, mode = CameraMode.PHOTO) }
            }
        }
    }

    fun clearActiveMoreMode() {
        _uiState.update { it.copy(activeMoreMode = null) }
    }

    fun startBurstCapture() {
        if (_uiState.value.isBurstCapturing || _uiState.value.isRecordingVideo) return

        burstJob?.cancel()
        burstJob = viewModelScope.launch {
            _uiState.update { it.copy(isBurstCapturing = true, burstCount = 0) }
            var count = 0
            while (count < 15) { // Burst up to 15 shots
                count++
                _uiState.update { it.copy(burstCount = count) }
                executeStillCapture()
                delay(280)
            }
            stopBurstCapture()
        }
    }

    fun stopBurstCapture() {
        burstJob?.cancel()
        burstJob = null
        _uiState.update { it.copy(isBurstCapturing = false, burstCount = 0) }
    }

    fun onPauseResumeVideo() {
        if (_uiState.value.isVideoPaused) {
            cameraManager?.resumeVideoRecording()
            _uiState.update { it.copy(isVideoPaused = false) }
        } else {
            cameraManager?.pauseVideoRecording()
            _uiState.update { it.copy(isVideoPaused = true) }
        }
    }

    fun onCaptureSnapshotDuringVideo() {
        executeStillCapture()
    }

    fun toggleSettings() {
        _uiState.update { it.copy(isSettingsOpen = !it.isSettingsOpen) }
    }

    fun onSettingsChanged(settings: CameraSettings) {
        _uiState.update { it.copy(settings = settings) }
    }

    fun onShutterPressed() {
        val currentState = _uiState.value

        when (currentState.mode) {
            CameraMode.VIDEO -> {
                if (currentState.isRecordingVideo) {
                    stopVideoRecording()
                } else {
                    startVideoRecording()
                }
            }
            CameraMode.NIGHT -> {
                startNightModeCapture()
            }
            else -> {
                // PHOTO, PORTRAIT, PRO
                if (currentState.timerSeconds > 0) {
                    startTimerCountdown {
                        executeStillCapture()
                    }
                } else {
                    executeStillCapture()
                }
            }
        }
    }

    private fun startTimerCountdown(onFinished: () -> Unit) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = _uiState.value.timerSeconds
            while (remaining > 0) {
                _uiState.update { it.copy(timerCountdownRemaining = remaining) }
                delay(1000)
                remaining--
            }
            _uiState.update { it.copy(timerCountdownRemaining = 0) }
            onFinished()
        }
    }

    private fun executeStillCapture() {
        // Trigger white flash animation
        viewModelScope.launch {
            _uiState.update { it.copy(isShutterFlashing = true) }
            delay(120)
            _uiState.update { it.copy(isShutterFlashing = false) }
        }

        val state = _uiState.value
        cameraManager?.takePicture(
            mode = state.mode,
            filter = state.selectedFilter,
            filterIntensity = state.filterIntensity,
            portraitBlurLevel = state.portraitState.blurLevel,
            portraitEffect = state.portraitState.effect,
            activeMoreMode = state.activeMoreMode,
            enableAiUpscaler = state.settings.enableAiFlagshipUpscaler,
            enableWatermark = state.settings.enableWatermark
        ) { savedUri ->
            _uiState.update { it.copy(lastCapturedMediaUri = savedUri) }
        }
    }

    private fun startNightModeCapture() {
        if (_uiState.value.nightState.isCapturingMultiFrame) return

        val durationSec = _uiState.value.nightState.estimatedDurationSec
        nightCaptureJob?.cancel()
        nightCaptureJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    nightState = it.nightState.copy(
                        isCapturingMultiFrame = true,
                        captureProgress = 0f
                    )
                )
            }

            val steps = 30
            val delayPerStep = (durationSec * 1000L) / steps
            for (step in 1..steps) {
                delay(delayPerStep)
                _uiState.update {
                    it.copy(
                        nightState = it.nightState.copy(
                            captureProgress = step.toFloat() / steps
                        )
                    )
                }
            }

            executeStillCapture()

            delay(300)
            _uiState.update {
                it.copy(
                    nightState = it.nightState.copy(
                        isCapturingMultiFrame = false,
                        captureProgress = 0f
                    )
                )
            }
        }
    }

    private fun startVideoRecording() {
        _uiState.update { it.copy(isRecordingVideo = true, recordingDurationSeconds = 0) }
        cameraManager?.startVideoRecording(
            onVideoSaved = { uri ->
                _uiState.update {
                    it.copy(
                        isRecordingVideo = false,
                        lastCapturedMediaUri = uri,
                        recordingDurationSeconds = 0
                    )
                }
            },
            onDurationUpdate = { seconds ->
                _uiState.update { it.copy(recordingDurationSeconds = seconds) }
            }
        )
    }

    private fun stopVideoRecording() {
        cameraManager?.stopVideoRecording()
        _uiState.update { it.copy(isRecordingVideo = false) }
    }

    // Pro Mode Adjustments
    fun onProControlSelected(control: ProControl?) {
        _uiState.update {
            it.copy(proState = it.proState.copy(selectedControl = control))
        }
    }

    fun onIsoChanged(iso: Int) {
        _uiState.update {
            it.copy(proState = it.proState.copy(iso = iso))
        }
        cameraManager?.applyProSettings(_uiState.value.proState)
    }

    fun onShutterSpeedChanged(index: Int) {
        _uiState.update {
            it.copy(proState = it.proState.copy(shutterSpeedIndex = index))
        }
        cameraManager?.applyProSettings(_uiState.value.proState)
    }

    fun onEvChanged(step: Int) {
        _uiState.update {
            it.copy(proState = it.proState.copy(exposureCompensation = step))
        }
        cameraManager?.applyProSettings(_uiState.value.proState)
    }

    fun onFocusChanged(distance: Float) {
        _uiState.update {
            it.copy(proState = it.proState.copy(manualFocusDistance = distance))
        }
        cameraManager?.applyProSettings(_uiState.value.proState)
    }

    fun onWbChanged(kelvin: Int) {
        _uiState.update {
            it.copy(proState = it.proState.copy(whiteBalanceKelvin = kelvin))
        }
        cameraManager?.applyProSettings(_uiState.value.proState)
    }

    fun onMeteringChanged(mode: MeteringMode) {
        _uiState.update {
            it.copy(proState = it.proState.copy(meteringMode = mode))
        }
        cameraManager?.applyProSettings(_uiState.value.proState)
    }

    // Portrait Adjustments
    fun onPortraitBlurChanged(level: Int) {
        _uiState.update {
            it.copy(portraitState = it.portraitState.copy(blurLevel = level))
        }
    }

    fun onPortraitEffectSelected(effect: PortraitEffect) {
        _uiState.update {
            it.copy(portraitState = it.portraitState.copy(effect = effect))
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }
}
