package com.example.model

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.example.R

sealed interface ImageSource {
    data class DevicePhoto(val uri: Uri) : ImageSource
    data class BuiltInPreset(
        @param:DrawableRes val resId: Int,
        val title: String,
        val subtitle: String
    ) : ImageSource
}

val DefaultPresets = listOf(
    ImageSource.BuiltInPreset(
        resId = R.drawable.sample_charm_dino,
        title = "Dino Charm",
        subtitle = "Chibi dinosaur keychain"
    ),
    ImageSource.BuiltInPreset(
        resId = R.drawable.sample_charm_cat,
        title = "Floral Kitten",
        subtitle = "Cute kitty with flower crown"
    )
)

enum class FilterMode(val label: String) {
    ORIGINAL("Normal"),
    HIGH_CONTRAST("High Contrast"),
    INVERTED("Invert"),
    GRAYSCALE("B&W Outline");

    fun toColorFilter(): ColorFilter? {
        return when (this) {
            ORIGINAL -> null
            GRAYSCALE -> {
                val matrix = ColorMatrix().apply { setToSaturation(0f) }
                ColorFilter.colorMatrix(matrix)
            }
            INVERTED -> {
                val matrix = ColorMatrix(
                    floatArrayOf(
                        -1f,  0f,  0f, 0f, 255f,
                         0f, -1f,  0f, 0f, 255f,
                         0f,  0f, -1f, 0f, 255f,
                         0f,  0f,  0f, 1f,   0f
                    )
                )
                ColorFilter.colorMatrix(matrix)
            }
            HIGH_CONTRAST -> {
                // High contrast color matrix
                val matrix = ColorMatrix(
                    floatArrayOf(
                        2.0f,  0f,    0f,   0f, -120f,
                        0f,    2.0f,  0f,   0f, -120f,
                        0f,    0f,    2.0f, 0f, -120f,
                        0f,    0f,    0f,   1f,    0f
                    )
                )
                ColorFilter.colorMatrix(matrix)
            }
        }
    }
}

enum class CanvasBg(val label: String, val color: Color) {
    WHITE("White Backlight", Color(0xFFFFFFFF)),
    SOFT_AMBER("Warm Glow", Color(0xFFFFFBEB)),
    PURE_BLACK("OLED Black", Color(0xFF000000)),
    LIGHT_GRAY("Light Studio", Color(0xFFF3F4F6))
}
