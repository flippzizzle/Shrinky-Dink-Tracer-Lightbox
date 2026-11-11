package com.example

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.model.CanvasBg
import com.example.model.DefaultPresets
import com.example.model.FilterMode
import com.example.model.ImageSource
import com.example.ui.MoveableLockButton
import com.example.ui.PresetsDialog
import com.example.ui.ShrinkyDinksGuideDialog
import com.example.ui.TracingBottomControlBar
import com.example.ui.TracingCanvas
import com.example.ui.TracingTopAppBar
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ShrinkyTraceApp()
            }
        }
    }
}

@Composable
fun ShrinkyTraceApp() {
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Image & Transform State
    var imageSource by remember { mutableStateOf<ImageSource?>(DefaultPresets.first()) }
    var isLocked by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1.0f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var isFlippedHorizontally by remember { mutableStateOf(false) }
    var isFlippedVertically by remember { mutableStateOf(false) }
    var filterMode by remember { mutableStateOf(FilterMode.ORIGINAL) }
    var opacity by remember { mutableFloatStateOf(1.0f) }
    var canvasBg by remember { mutableStateOf(CanvasBg.WHITE) }

    // Floating hint when locked (auto-fades after 3 seconds)
    var showLockedHint by remember { mutableStateOf(false) }

    // Dialogs
    var showGuideDialog by remember { mutableStateOf(false) }
    var showPresetsDialog by remember { mutableStateOf(false) }

    // Moveable lock button position
    var buttonPosition by remember { mutableStateOf<Offset?>(null) }

    val currentTitle = when (val src = imageSource) {
        is ImageSource.BuiltInPreset -> src.title
        is ImageSource.DevicePhoto -> "Custom Photo"
        null -> "No Photo"
    }

    // Gallery photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageSource = ImageSource.DevicePhoto(uri)
            scale = 1.0f
            pan = Offset.Zero
            rotation = 0f
            scope.launch {
                snackbarHostState.showSnackbar("Photo loaded! Adjust or tap Lock to trace.")
            }
        }
    }

    // Apply brightness and immersive mode when lock state changes
    DisposableEffect(isLocked) {
        window?.let { win ->
            val insetsController = WindowCompat.getInsetsController(win, win.decorView)
            val layoutParams = win.attributes

            if (isLocked) {
                // Maximize brightness to 100%
                layoutParams.screenBrightness = 1.0f
                win.attributes = layoutParams
                win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                // Hide system bars for an unobstructed light table
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                // Revert to system brightness
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                win.attributes = layoutParams
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                // Restore system bars
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        onDispose {
            window?.let { win ->
                val layoutParams = win.attributes
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                win.attributes = layoutParams
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // Auto-dismiss the lock hint after 3 seconds
    LaunchedEffect(isLocked) {
        if (isLocked) {
            showLockedHint = true
            delay(3000)
            showLockedHint = false
        } else {
            showLockedHint = false
        }
    }

    // Handle back press
    BackHandler(enabled = isLocked || showGuideDialog || showPresetsDialog) {
        when {
            showGuideDialog -> showGuideDialog = false
            showPresetsDialog -> showPresetsDialog = false
            isLocked -> {
                isLocked = false
                scope.launch {
                    snackbarHostState.showSnackbar("Screen unlocked.")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            if (!isLocked) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        },
        topBar = {
            AnimatedVisibility(
                visible = !isLocked,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it }
            ) {
                TracingTopAppBar(
                    hasImage = imageSource != null,
                    currentTitle = currentTitle,
                    onPickImageRequested = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onOpenPresetsRequested = { showPresetsDialog = true },
                    onOpenGuideRequested = { showGuideDialog = true }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !isLocked,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                TracingBottomControlBar(
                    scale = scale,
                    rotation = rotation,
                    isFlippedHorizontally = isFlippedHorizontally,
                    filterMode = filterMode,
                    canvasBg = canvasBg,
                    onToggleMirror = {
                        isFlippedHorizontally = !isFlippedHorizontally
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isFlippedHorizontally) "Mirrored (Ready for frosted side)" else "Mirror Off"
                            )
                        }
                    },
                    onRotate90 = {
                        rotation = (rotation + 90f) % 360f
                        scope.launch {
                            snackbarHostState.showSnackbar("Rotated to ${rotation.roundToInt()}°")
                        }
                    },
                    onResetTransform = {
                        scale = 1.0f
                        pan = Offset.Zero
                        rotation = 0f
                        scope.launch {
                            snackbarHostState.showSnackbar("Fit & Centered to Screen")
                        }
                    },
                    onZoomIn = {
                        scale = (scale * 1.25f).coerceAtMost(15.0f)
                    },
                    onZoomOut = {
                        scale = (scale / 1.25f).coerceAtLeast(0.1f)
                    },
                    onResetZoom = {
                        scale = 1.0f
                    },
                    onSelectFilter = { mode ->
                        filterMode = mode
                        scope.launch {
                            snackbarHostState.showSnackbar("Filter: ${mode.label}")
                        }
                    },
                    onSelectCanvasBg = { bg ->
                        canvasBg = bg
                    },
                    onLockRequested = {
                        isLocked = true
                    }
                )
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isLocked) PaddingValues(0.dp) else innerPadding)
        ) {
            val parentWidthPx = constraints.maxWidth.toFloat()
            val parentHeightPx = constraints.maxHeight.toFloat()

            // Initialize button position if not set yet (top right corner, comfortably inset)
            val currentPos = buttonPosition ?: remember(parentWidthPx) {
                val initialX = (parentWidthPx - with(density) { 240.dp.toPx() }).coerceAtLeast(16f)
                val initialY = with(density) { 20.dp.toPx() }
                Offset(initialX, initialY).also { buttonPosition = it }
            }

            // The main tracing canvas
            TracingCanvas(
                imageSource = imageSource,
                isLocked = isLocked,
                scale = scale,
                pan = pan,
                rotation = rotation,
                isFlippedHorizontally = isFlippedHorizontally,
                isFlippedVertically = isFlippedVertically,
                filterMode = filterMode,
                opacity = opacity,
                canvasBg = canvasBg,
                onTransform = { zoomChange, panChange ->
                    scale = (scale * zoomChange).coerceIn(0.1f, 15.0f)
                    pan += panChange
                },
                onPickImageRequested = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onChooseSampleRequested = { showPresetsDialog = true },
                modifier = Modifier.fillMaxSize()
            )

            // The moveable Lock/Unlock button (Visible in BOTH locked and unlocked modes, draggable anywhere)
            MoveableLockButton(
                isLocked = isLocked,
                position = currentPos,
                onPositionChanged = { newPos -> buttonPosition = newPos },
                onToggleLock = {
                    val willLock = !isLocked
                    isLocked = willLock
                },
                parentWidthPx = parentWidthPx,
                parentHeightPx = parentHeightPx
            )

            // Brief, subtle locked indicator that fades away after 3 seconds
            AnimatedVisibility(
                visible = isLocked && showLockedHint,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xDD0F172A))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Brightness • Touches Locked • Trace Away!",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Shrinky Dinks tips dialog
    if (showGuideDialog) {
        ShrinkyDinksGuideDialog(
            onDismiss = { showGuideDialog = false }
        )
    }

    // Ready-made preset picker dialog
    if (showPresetsDialog) {
        PresetsDialog(
            onSelectPreset = { preset ->
                imageSource = preset
                scale = 1.0f
                pan = Offset.Zero
                rotation = 0f
            },
            onPickDevicePhoto = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismiss = { showPresetsDialog = false }
        )
    }
}
