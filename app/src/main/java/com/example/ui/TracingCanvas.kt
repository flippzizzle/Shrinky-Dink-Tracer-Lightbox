package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.CanvasBg
import com.example.model.FilterMode
import com.example.model.ImageSource

@Composable
fun TracingCanvas(
    imageSource: ImageSource?,
    isLocked: Boolean,
    scale: Float,
    pan: Offset,
    rotation: Float,
    isFlippedHorizontally: Boolean,
    isFlippedVertically: Boolean,
    filterMode: FilterMode,
    opacity: Float,
    canvasBg: CanvasBg,
    onTransform: (zoomChange: Float, panChange: Offset) -> Unit,
    onPickImageRequested: () -> Unit,
    onChooseSampleRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(canvasBg.color)
            .testTag("tracing_canvas")
            // When locked: block and consume ALL pointer events so tracing hand doesn't touch the image
            .pointerInput(isLocked) {
                if (isLocked) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                            if (event.changes.all { it.changedToUp() }) break
                        }
                    }
                }
            }
            // When unlocked: detect zoom and pan
            .pointerInput(isLocked) {
                if (!isLocked) {
                    detectTransformGestures { _, panChange, zoomChange, _ ->
                        onTransform(zoomChange, panChange)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageSource != null) {
            val imageModel = when (imageSource) {
                is ImageSource.DevicePhoto -> imageSource.uri
                is ImageSource.BuiltInPreset -> imageSource.resId
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageModel)
                    .crossfade(true)
                    .build(),
                contentDescription = "Tracing Image",
                contentScale = ContentScale.Fit,
                colorFilter = filterMode.toColorFilter(),
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(opacity)
                    .graphicsLayer {
                        scaleX = scale * (if (isFlippedHorizontally) -1f else 1f)
                        scaleY = scale * (if (isFlippedVertically) -1f else 1f)
                        translationX = pan.x
                        translationY = pan.y
                        rotationZ = rotation
                    }
                    .testTag("tracing_image")
            )
        } else {
            // Empty State
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(32.dp)
                    .testTag("empty_state_view")
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome to Shrinky Trace",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Upload any photo or design to trace onto shrink plastic. Lock the screen at maximum brightness for a clean, non-slip tracing light table.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onPickImageRequested,
                    modifier = Modifier.testTag("upload_photo_button")
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Select Photo to Trace")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onChooseSampleRequested,
                    modifier = Modifier.testTag("use_sample_button")
                ) {
                    Text("Or Try Sample Keychain Charm")
                }
            }
        }
    }
}
