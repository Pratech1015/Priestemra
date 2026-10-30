package com.priestemra.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.priestemra.camera.AspectRatioMode
import com.priestemra.camera.CameraManager
import com.priestemra.camera.CameraMode
import com.priestemra.processing.FilterPreset
import com.priestemra.ui.camera.components.*
import com.priestemra.ui.theme.*

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        viewModel.initializeSensors(context)
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    val cameraManager = remember {
        CameraManager(context).also {
            viewModel.cameraManager = it
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.shutdown()
        }
    }

    var previewWidth by remember { mutableStateOf(1f) }
    var previewHeight by remember { mutableStateOf(1f) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(uiState.isFrontCamera) {
        previewView?.surfaceProvider?.let { surfaceProvider ->
            cameraManager.bindCameraUseCases(
                lifecycleOwner = lifecycleOwner,
                surfaceProvider = surfaceProvider,
                isFrontCamera = uiState.isFrontCamera,
                flashMode = uiState.flashMode,
                zoomRatio = uiState.zoomRatio
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SamsungBlack)
    ) {
        if (hasCameraPermission) {
            // Viewfinder Container with Aspect Ratio framing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .let { baseModifier ->
                        if (uiState.aspectRatio.ratio > 0f) {
                            baseModifier.aspectRatio(uiState.aspectRatio.ratio, matchHeightConstraintsFirst = true)
                        } else {
                            baseModifier
                        }
                    }
                    .align(Alignment.Center)
            ) {
                // Viewfinder PreviewView
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            previewView = this
                            cameraManager.initialize(
                                lifecycleOwner = lifecycleOwner,
                                surfaceProvider = this.surfaceProvider,
                                isFrontCamera = uiState.isFrontCamera,
                                flashMode = uiState.flashMode,
                                zoomRatio = uiState.zoomRatio,
                                onInitialized = {}
                            )
                        }
                    },
                    update = {},
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            previewWidth = coordinates.size.width.toFloat()
                            previewHeight = coordinates.size.height.toFloat()
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                if (zoom != 1f) {
                                    viewModel.onZoomPinchDelta(zoom)
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    viewModel.onTapToFocus(offset, previewWidth, previewHeight)
                                }
                            )
                        }
                )

                // Grid Lines & Horizon Level Overlay
                GridAndLevelOverlay(
                    showGrid = uiState.settings.showGridLines,
                    showLevel = uiState.settings.showLevelIndicator,
                    tiltDegrees = uiState.horizonTiltDegrees
                )

                // Focus Ring Indicator
                uiState.focusRingPosition?.let { pos ->
                    FocusRing(position = pos)
                }

                // Shutter Flash Animation Overlay
                AnimatedVisibility(
                    visible = uiState.isShutterFlashing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    )
                }
            }

            // Top Status Bar (Hide when in MORE mode)
            if (uiState.mode != CameraMode.MORE) {
                TopBar(
                    flashMode = uiState.flashMode,
                    timerSeconds = uiState.timerSeconds,
                    aspectRatio = uiState.aspectRatio,
                    isFilterActive = uiState.selectedFilter != FilterPreset.ORIGINAL,
                    onFlashClick = { viewModel.onFlashToggle() },
                    onTimerClick = { viewModel.onTimerToggle() },
                    onAspectRatioClick = { viewModel.onAspectRatioToggle() },
                    onFilterClick = { viewModel.toggleFilterTray() },
                    onSettingsClick = { viewModel.toggleSettings() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // Pro Mode Live Histogram
            if (uiState.mode == CameraMode.PRO && uiState.settings.showHistogram) {
                HistogramView(
                    data = uiState.histogramData,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 90.dp, end = 16.dp)
                )
            }

            // Active Timer Countdown Overlay
            if (uiState.timerCountdownRemaining > 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${uiState.timerCountdownRemaining}",
                        color = SamsungYellow,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Video Recording Duration Banner
            if (uiState.isRecordingVideo) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SamsungOverlay)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    val minutes = uiState.recordingDurationSeconds / 60
                    val seconds = uiState.recordingDurationSeconds % 60
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(SamsungRed)
                        )
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            color = SamsungWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Active Burst Count Overlay
            if (uiState.isBurstCapturing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(80.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(SamsungYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${uiState.burstCount}",
                        color = SamsungBlack,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Active More Submode Indicator
            if (uiState.mode != CameraMode.MORE) {
                uiState.activeMoreMode?.let { moreMode ->
                    Surface(
                        color = SamsungOverlay,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 110.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = moreMode.title.uppercase(),
                                color = SamsungYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Exit mode",
                                tint = SamsungWhite85,
                                modifier = Modifier
                                    .size(16.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures {
                                            viewModel.clearActiveMoreMode()
                                        }
                                    }
                            )
                        }
                    }
                }
            }

            // Samsung Scene Optimizer AI Active Badge (Iconic bottom-right sparkle)
            if (uiState.settings.enableAiFlagshipUpscaler && uiState.mode != CameraMode.PRO && uiState.mode != CameraMode.MORE) {
                Surface(
                    color = SamsungOverlay,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 220.dp, end = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Mode Active",
                            tint = SamsungYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom Cluster (Filter tray, Overlays, Zoom, Controls, Modes, Shutter)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Real-Time Filter Tray
                AnimatedVisibility(
                    visible = uiState.isFilterTrayOpen,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    FilterTray(
                        selectedFilter = uiState.selectedFilter,
                        intensity = uiState.filterIntensity,
                        onFilterSelected = { viewModel.onFilterSelected(it) },
                        onIntensityChanged = { viewModel.onFilterIntensityChanged(it) }
                    )
                }

                // Mode-specific overlays above zoom
                when (uiState.mode) {
                    CameraMode.PRO -> {
                        ProControls(
                            state = uiState.proState,
                            onControlSelected = { viewModel.onProControlSelected(it) },
                            onIsoChanged = { viewModel.onIsoChanged(it) },
                            onShutterSpeedChanged = { viewModel.onShutterSpeedChanged(it) },
                            onEvChanged = { viewModel.onEvChanged(it) },
                            onFocusChanged = { viewModel.onFocusChanged(it) },
                            onWbChanged = { viewModel.onWbChanged(it) },
                            onMeteringChanged = { viewModel.onMeteringChanged(it) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    CameraMode.NIGHT -> {
                        NightControls(state = uiState.nightState)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    CameraMode.PORTRAIT -> {
                        PortraitControls(
                            state = uiState.portraitState,
                            onBlurLevelChanged = { viewModel.onPortraitBlurChanged(it) },
                            onEffectSelected = { viewModel.onPortraitEffectSelected(it) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    else -> {}
                }

                // Zoom Pills
                ZoomControls(
                    currentZoom = uiState.zoomRatio,
                    availableZoomLevels = uiState.availableZoomLevels,
                    onZoomSelected = { viewModel.onZoomSelected(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Bar (Gallery, Shutter, Flip, Video Controls)
                BottomBar(
                    mode = uiState.mode,
                    isRecording = uiState.isRecordingVideo,
                    isVideoPaused = uiState.isVideoPaused,
                    isFrontCamera = uiState.isFrontCamera,
                    lastMediaUri = uiState.lastCapturedMediaUri,
                    onShutterClick = { viewModel.onShutterPressed() },
                    onSwitchCameraClick = { viewModel.onSwitchCamera() },
                    onGalleryClick = { viewModel.openGalleryViewer() },
                    onPauseResumeVideo = { viewModel.onPauseResumeVideo() },
                    onVideoSnapshot = { viewModel.onCaptureSnapshotDuringVideo() },
                    onBurstStart = { viewModel.startBurstCapture() },
                    onBurstStop = { viewModel.stopBurstCapture() }
                )

                // Mode Carousel
                ModeCarousel(
                    currentMode = uiState.mode,
                    onModeSelected = { viewModel.onModeSelected(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // More Modes Fullscreen Overlay
            if (uiState.mode == CameraMode.MORE) {
                MoreModesOverlay(
                    activeMoreMode = uiState.activeMoreMode,
                    onSelectMoreMode = { viewModel.onMoreModeSelected(it) },
                    onClose = { viewModel.onModeSelected(CameraMode.PHOTO) }
                )
            }

            // Samsung One UI Full-Page Settings Screen
            AnimatedVisibility(
                visible = uiState.isSettingsOpen,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                SettingsScreen(
                    settings = uiState.settings,
                    onSettingsChanged = { viewModel.onSettingsChanged(it) },
                    onDismiss = { viewModel.toggleSettings() }
                )
            }

            // Fullscreen Gallery / Photo Viewer Dialog
            if (uiState.isGalleryViewerOpen && uiState.lastCapturedMediaUri != null) {
                GalleryViewerDialog(
                    mediaUri = uiState.lastCapturedMediaUri!!,
                    activeFilter = uiState.selectedFilter,
                    onClose = { viewModel.closeGalleryViewer() },
                    onDelete = { viewModel.onDeleteMedia(uiState.lastCapturedMediaUri!!, context) }
                )
            }
        } else {
            // Permission request screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Priestemra",
                    color = SamsungYellow,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera and microphone permissions are required to provide flagship image processing and capture.",
                    color = SamsungWhite85,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SamsungYellow)
                ) {
                    Text(
                        text = "Grant Permissions",
                        color = SamsungBlack,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
