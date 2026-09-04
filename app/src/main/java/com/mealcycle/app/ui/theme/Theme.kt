package com.mealcycle.app.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mealcycle.app.ui.theme.engine.ThemeEngine

/**
 * CompositionLocal to provide ThemeEngine to the entire tree.
 * Null means no engine available — fallback to static scheme.
 */
val LocalThemeEngine = staticCompositionLocalOf<ThemeEngine?> { null }

/**
 * Animates every color in a ColorScheme for smooth crossfade transitions.
 * Uses spring animation with low stiffness for premium feel.
 */
@Composable
private fun ColorScheme.animated(): ColorScheme {
    val spec = spring<Color>(stiffness = Spring.StiffnessLow)
    return copy(
        primary = animateColorAsState(primary, spec, label = "primary").value,
        onPrimary = animateColorAsState(onPrimary, spec, label = "onPrimary").value,
        primaryContainer = animateColorAsState(primaryContainer, spec, label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(onPrimaryContainer, spec, label = "onPrimaryContainer").value,
        secondary = animateColorAsState(secondary, spec, label = "secondary").value,
        onSecondary = animateColorAsState(onSecondary, spec, label = "onSecondary").value,
        secondaryContainer = animateColorAsState(secondaryContainer, spec, label = "secondaryContainer").value,
        background = animateColorAsState(background, spec, label = "background").value,
        surface = animateColorAsState(surface, spec, label = "surface").value,
        surfaceVariant = animateColorAsState(surfaceVariant, spec, label = "surfaceVariant").value,
        onBackground = animateColorAsState(onBackground, spec, label = "onBackground").value,
        onSurface = animateColorAsState(onSurface, spec, label = "onSurface").value,
        onSurfaceVariant = animateColorAsState(onSurfaceVariant, spec, label = "onSurfaceVariant").value,
        error = animateColorAsState(error, spec, label = "error").value,
        onError = animateColorAsState(onError, spec, label = "onError").value
    )
}

/**
 * Fallback light/dark schemes (brand Indigo) when no ThemeEngine is available.
 */
private val FallbackLightScheme = lightColorScheme(
    primary = Primary, onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer, onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary, onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    background = Background, surface = Surface, surfaceVariant = SurfaceVariant,
    onBackground = OnBackground, onSurface = OnSurface, onSurfaceVariant = OnSurfaceVariant,
    error = Error, onError = OnError
)

private val FallbackDarkScheme = darkColorScheme(
    primary = Primary, onPrimary = OnPrimary,
    primaryContainer = DarkPrimaryContainer, onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = Secondary, onSecondary = OnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    background = DarkBackground, surface = DarkSurface, surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground, onSurface = DarkOnSurface, onSurfaceVariant = DarkOnSurfaceVariant,
    error = Error, onError = OnError
)

/**
 * App theme wrapper.
 *
 * @param themeMode "auto" (follow system), "light", or "dark".
 * @param themeEngine optional ThemeEngine for dynamic palette-driven colors.
 */
@Composable
fun MealCycleTheme(
    themeMode: String = "auto",
    themeEngine: ThemeEngine? = null,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    // Resolve color scheme: ThemeEngine → Material You → Static fallback
    val colorScheme = when {
        // Priority 1: ThemeEngine dynamic palette
        themeEngine != null -> {
            val themeSource by themeEngine.themeSource.collectAsState(initial = "default")
            if (themeSource != "default") {
                val light by themeEngine.lightColorScheme.collectAsState()
                val dark by themeEngine.darkColorScheme.collectAsState()
                if (isDark) dark else light
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Priority 2: Android 12+ Material You
                val context = LocalContext.current
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) FallbackDarkScheme else FallbackLightScheme
            }
        }
        // Priority 2: Android 12+ Material You (no engine)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Priority 3: Static fallback
        else -> if (isDark) FallbackDarkScheme else FallbackLightScheme
    }

    CompositionLocalProvider(LocalThemeEngine provides themeEngine) {
        MaterialTheme(
            colorScheme = colorScheme.animated(),
            typography = AppTypography,
            content = content
        )
    }
}
