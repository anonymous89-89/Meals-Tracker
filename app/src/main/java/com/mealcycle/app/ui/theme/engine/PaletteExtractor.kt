package com.mealcycle.app.ui.theme.engine

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracted color palette from a wallpaper or user image.
 * All colors are nullable — some swatches may not exist in every image.
 */
data class ExtractedPalette(
    val dominant: Color = Color(0xFF4F46E5),   // fallback to brand indigo
    val vibrant: Color = Color(0xFF6366F1),
    val muted: Color = Color(0xFF6B7280),
    val darkVibrant: Color = Color(0xFF3730A3),
    val darkMuted: Color = Color(0xFF1E1E1E),
    val lightVibrant: Color = Color(0xFFE0DEFF),
    val lightMuted: Color = Color(0xFFF1F5F9),
    val isDefault: Boolean = true
)

/**
 * Extracts color palettes from bitmaps using AndroidX Palette API.
 *
 * Performance:
 * - Scales input to 200×200 to minimize allocation
 * - Runs on Dispatchers.Default (CPU-bound)
 * - Caches result in StateFlow — only recomputes on new image
 */
@Singleton
class PaletteExtractor @Inject constructor() {

    private val _palette = MutableStateFlow(ExtractedPalette())
    val palette: StateFlow<ExtractedPalette> = _palette.asStateFlow()

    /**
     * Extract palette from bitmap. Call from coroutine scope.
     * Bitmap is scaled down internally — caller doesn't need to worry about size.
     */
    suspend fun extractFrom(bitmap: Bitmap) = withContext(Dispatchers.Default) {
        // Scale to 200×200 to avoid expensive allocation
        val scaled = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
        val androidPalette = Palette.from(scaled).maximumColorCount(16).generate()

        val extracted = ExtractedPalette(
            dominant = androidPalette.dominantSwatch?.let { Color(it.rgb) }
                ?: ExtractedPalette().dominant,
            vibrant = androidPalette.vibrantSwatch?.let { Color(it.rgb) }
                ?: ExtractedPalette().vibrant,
            muted = androidPalette.mutedSwatch?.let { Color(it.rgb) }
                ?: ExtractedPalette().muted,
            darkVibrant = androidPalette.darkVibrantSwatch?.let { Color(it.rgb) }
                ?: ExtractedPalette().darkVibrant,
            darkMuted = androidPalette.darkMutedSwatch?.let { Color(it.rgb) }
                ?: ExtractedPalette().darkMuted,
            lightVibrant = androidPalette.lightVibrantSwatch?.let { Color(it.rgb) }
                ?: ExtractedPalette().lightVibrant,
            lightMuted = androidPalette.lightMutedSwatch?.let { Color(it.rgb) }
                ?: ExtractedPalette().lightMuted,
            isDefault = false
        )

        _palette.value = extracted

        // Recycle scaled bitmap if it's a new one
        if (scaled !== bitmap) scaled.recycle()
    }

    /** Reset to default brand palette */
    fun resetToDefault() {
        _palette.value = ExtractedPalette()
    }
}
