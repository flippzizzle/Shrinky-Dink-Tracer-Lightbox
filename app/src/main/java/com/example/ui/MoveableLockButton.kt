package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LockDarkGlass
import com.example.ui.theme.LockGlowCyan
import kotlin.math.roundToInt

@Composable
fun MoveableLockButton(
    isLocked: Boolean,
    position: Offset,
    onPositionChanged: (Offset) -> Unit,
    onToggleLock: () -> Unit,
    parentWidthPx: Float,
    parentHeightPx: Float,
    modifier: Modifier = Modifier
) {
    var buttonWidthPx by remember { mutableStateOf(0f) }
    var buttonHeightPx by remember { mutableStateOf(0f) }

    val bgColor by animateColorAsState(
        targetValue = if (isLocked) LockDarkGlass else Color(0xEE1E1B4B),
        animationSpec = tween(300),
        label = "LockButtonBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isLocked) LockGlowCyan else Color(0xFF818CF8),
        animationSpec = tween(300),
        label = "LockButtonBorder"
    )

    val iconColor = if (isLocked) LockGlowCyan else Color(0xFFE0E7FF)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        modifier = modifier
            .offset {
                IntOffset(
                    x = position.x.roundToInt(),
                    y = position.y.roundToInt()
                )
            }
            .onGloballyPositioned { coordinates ->
                buttonWidthPx = coordinates.size.width.toFloat()
                buttonHeightPx = coordinates.size.height.toFloat()
            }
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp))
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .testTag("moveable_lock_button")
            .pointerInput(isLocked, parentWidthPx, parentHeightPx, buttonWidthPx, buttonHeightPx) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var dragDistance = 0f
                    var isDragging = false
                    val touchSlop = viewConfiguration.touchSlop

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.changedToUp()) {
                            if (!isDragging) {
                                change.consume()
                                onToggleLock()
                            }
                            break
                        }
                        val drag = change.position - change.previousPosition
                        dragDistance += drag.getDistance()
                        if (!isDragging && dragDistance > touchSlop) {
                            isDragging = true
                        }
                        if (isDragging) {
                            change.consume()
                            val maxX = (parentWidthPx - buttonWidthPx).coerceAtLeast(0f)
                            val maxY = (parentHeightPx - buttonHeightPx).coerceAtLeast(0f)
                            val newX = (position.x + drag.x).coerceIn(0f, maxX)
                            val newY = (position.y + drag.y).coerceIn(0f, maxY)
                            onPositionChanged(Offset(newX, newY))
                        }
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Drag grip indicator
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = "Drag button",
                tint = iconColor.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )

            // Lock / Unlock icon
            Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = if (isLocked) "Locked - Tap to Unlock" else "Unlocked - Tap to Lock",
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = if (isLocked) "LOCKED • TAP TO UNLOCK" else "LOCK FOR TRACING",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        }
    }
}
