package com.mealcycle.app.ui.theme.engine

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.mealcycle.app.data.datastore.UserPreferences
import com.mealcycle.app.ui.theme.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Theme source selection.
 */
enum class ThemeSource { DEFAULT, WALLPAPER, CUSTOM_IMAGE }

/**
 * Glass/blur configuration derived from device tier and user settings.
 */
data class GlassConfig(
    val blurRadius: Float = 12f,
    val tintAlpha: Float = 0.15f,
    val tintColor: Color = Color.White,
    val borderAlpha: Float = 0.2f
)

/**
 * Central theme engine — single source of truth for all dynamic theming.
 *
 * Generates:
 * - Light + Dark ColorSchemes from extracted palette
 * - Gradient color stops
 * - Glass blur configuration
 *
 * Philosophy: perceived smoothness > visual intensity.
 */
@Singleton
class ThemeEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val paletteExtractor: PaletteExtractor,
    private val deviceTierDetector: DeviceTierDetector,
    private val userPreferences: UserPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ── Derived state from palette ──

    val lightColorScheme: StateFlow<ColorScheme> = paletteExtractor.palette.map { p ->
        buildLightScheme(p)
    }.stateIn(scope, SharingStarted.Eagerly, buildLightScheme(ExtractedPalette()))

    val darkColorScheme: StateFlow<ColorScheme> = paletteExtractor.palette.map { p ->
        buildDarkScheme(p)
    }.stateIn(scope, SharingStarted.Eagerly, buildDarkScheme(ExtractedPalette()))

    val gradientColors: StateFlow<List<Color>> = paletteExtractor.palette.map { p ->
        listOf(
            p.dominant,
            p.vibrant,
            p.muted,
            p.lightVibrant
        )
    }.stateIn(scope, SharingStarted.Eagerly, GradientPrimary + listOf(Primary))

    val glassConfig: StateFlow<GlassConfig> = paletteExtractor.palette.map { p ->
        val tier = deviceTierDetector.tier
        GlassConfig(
            blurRadius = tier.maxBlurRadius * 0.6f,  // default 60% of max
            tintAlpha = 0.12f,
            tintColor = p.lightMuted,
            borderAlpha = 0.15f
        )
    }.stateIn(scope, SharingStarted.Eagerly, GlassConfig())

    // ── User-adjustable intensities ──

    val blurIntensity: StateFlow<Float> = userPreferences.blurIntensity
        .stateIn(scope, SharingStarted.Eagerly, 0.6f)

    val gradientIntensity: StateFlow<Float> = userPreferences.gradientIntensity
        .stateIn(scope, SharingStarted.Eagerly, 0.7f)

    val themeSource: StateFlow<String> = userPreferences.themeSource
        .stateIn(scope, SharingStarted.Eagerly, "default")

    // ── Actions ──

    /**
     * Extract palette from the device wallpaper.
     */
    fun extractFromWallpaper() {
        scope.launch {
            try {
                val wm = WallpaperManager.getInstance(context)
                val drawable = wm.drawable ?: return@launch
                val bitmap = if (drawable is BitmapDrawable) {
                    drawable.bitmap
                } else {
                    val bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bmp)
                    drawable.setBounds(0, 0, 200, 200)
                    drawable.draw(canvas)
                    bmp
                }
                paletteExtractor.extractFrom(bitmap)
                userPreferences.setThemeSource("wallpaper")
            } catch (_: SecurityException) {
                // Wallpaper permission denied — stay on default
            }
        }
    }

    /**
     * Extract palette from a user-selected image URI.
     */
    fun extractFromImage(uri: Uri) {
        scope.launch {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .size(200, 200)
                    .allowHardware(false)
                    .build()
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as BitmapDrawable).bitmap
                    paletteExtractor.extractFrom(bitmap)
                    userPreferences.setThemeSource("custom")
                    userPreferences.setCustomImageUri(uri.toString())
                }
            } catch (_: Exception) {
                // Failed to load image — stay on current
            }
        }
    }

    /**
     * Reset to default brand palette.
     */
    fun resetToDefault() {
        paletteExtractor.resetToDefault()
        scope.launch {
            userPreferences.setThemeSource("default")
            userPreferences.setCustomImageUri("")
        }
    }

    fun setBlurIntensity(value: Float) {
        scope.launch { userPreferences.setBlurIntensity(value.coerceIn(0f, 1f)) }
    }

    fun setGradientIntensity(value: Float) {
        scope.launch { userPreferences.setGradientIntensity(value.coerceIn(0f, 1f)) }
    }

    // ── Color scheme builders ──

    private fun buildLightScheme(p: ExtractedPalette) = lightColorScheme(
        primary = p.dominant,
        onPrimary = Color.White,
        primaryContainer = p.lightVibrant.copy(alpha = 0.3f),
        onPrimaryContainer = p.darkVibrant,
        secondary = p.muted,
        onSecondary = Color.White,
        secondaryContainer = p.lightMuted,
        background = Color(0xFFF8F9FB),
        surface = Color.White,
        surfaceVariant = p.lightMuted.copy(alpha = 0.5f),
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        onSurfaceVariant = Color(0xFF49454F),
        error = Error,
        onError = OnError
    )

    private fun buildDarkScheme(p: ExtractedPalette) = darkColorScheme(
        primary = p.vibrant,
        onPrimary = Color.White,
        primaryContainer = p.darkVibrant.copy(alpha = 0.4f),
        onPrimaryContainer = p.lightVibrant,
        secondary = p.muted,
        onSecondary = Color.White,
        secondaryContainer = p.darkMuted.copy(alpha = 0.6f),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFF2C2C2C),
        onBackground = Color(0xFFE1E1E1),
        onSurface = Color(0xFFE1E1E1),
        onSurfaceVariant = Color(0xFFA0A0A0),
        error = Error,
        onError = OnError
    )
}
