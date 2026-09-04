package com.mealcycle.app.ui.theme.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.mealcycle.app.ui.theme.LocalThemeEngine
import com.mealcycle.app.ui.theme.GradientPrimary

/**
 * Animated linear gradient background that adapts to the extracted palette.
 *
 * Performance:
 * - Uses animateColorAsState (not manual recomposition)
 * - Gradient angle shifts subtly via InfiniteTransition (only on HIGH tier)
 * - Falls back to static gradient on LOW/MID devices
 */
@Composable
fun DynamicGradientBackground(
    modifier: Modifier = Modifier,
    intensityOverride: Float? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val engine = LocalThemeEngine.current
    val gradientColors by engine?.gradientColors?.collectAsState()
        ?: remember { mutableStateOf(GradientPrimary + listOf(Color(0xFF4F46E5))) }
    val intensity by engine?.gradientIntensity?.collectAsState(initial = 0.7f)
        ?: remember { mutableStateOf(0.7f) }

    val effectiveIntensity = intensityOverride ?: intensity

    // Animate individual colors for smooth transitions
    val animSpec = spring<Color>(stiffness = Spring.StiffnessLow)
    val c0 by animateColorAsState(
        gradientColors.getOrElse(0) { Color(0xFF4F46E5) }.copy(alpha = effectiveIntensity),
        animSpec, label = "gc0"
    )
    val c1 by animateColorAsState(
        gradientColors.getOrElse(1) { Color(0xFF6366F1) }.copy(alpha = effectiveIntensity),
        animSpec, label = "gc1"
    )
    val c2 by animateColorAsState(
        gradientColors.getOrElse(2) { Color(0xFF6B7280) }.copy(alpha = effectiveIntensity * 0.6f),
        animSpec, label = "gc2"
    )

    // Subtle angle animation — only on flagship devices
    val tier = engine?.let {
        remember {
            try {
                val field = it::class.java.getDeclaredField("deviceTierDetector")
                field.isAccessible = true
                val detector = field.get(it) as? com.mealcycle.app.ui.theme.engine.DeviceTierDetector
                detector?.tier
            } catch (_: Exception) { null }
        }
    }

    val enableShimmer = tier?.enableShimmer == true
    val angleOffset by if (enableShimmer) {
        val transition = rememberInfiniteTransition(label = "gradientShimmer")
        transition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "angle"
        )
    } else {
        remember { mutableFloatStateOf(135f) }
    }

    val brush = Brush.linearGradient(
        colors = listOf(c0, c1, c2),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f) // static angle for consistency
    )

    Box(modifier = modifier.background(brush), content = content)
}

/**
 * Radial glow accent — centered soft glow using palette vibrant color.
 * Lightweight: just a radial gradient, no blur.
 */
@Composable
fun RadialGlowBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val engine = LocalThemeEngine.current
    val gradientColors by engine?.gradientColors?.collectAsState()
        ?: remember { mutableStateOf(GradientPrimary + listOf(Color(0xFF4F46E5))) }

    val centerColor by animateColorAsState(
        gradientColors.getOrElse(0) { Color(0xFF4F46E5) }.copy(alpha = 0.15f),
        spring(stiffness = Spring.StiffnessLow), label = "radialCenter"
    )

    val brush = Brush.radialGradient(
        colors = listOf(centerColor, Color.Transparent),
        radius = 600f
    )

    Box(modifier = modifier.background(brush), content = content)
}

/**
 * Sweep gradient overlay for premium card headers.
 * Very lightweight — single sweep brush, no animation.
 */
@Composable
fun SweepGradientOverlay(
    modifier: Modifier = Modifier,
    alpha: Float = 0.08f,
    content: @Composable BoxScope.() -> Unit
) {
    val engine = LocalThemeEngine.current
    val gradientColors by engine?.gradientColors?.collectAsState()
        ?: remember { mutableStateOf(GradientPrimary + listOf(Color(0xFF4F46E5))) }

    val brush = Brush.sweepGradient(
        colors = gradientColors.map { it.copy(alpha = alpha) } + listOf(
            gradientColors.first().copy(alpha = alpha)
        )
    )

    Box(modifier = modifier.background(brush), content = content)
}
