package com.mealcycle.app.ui.theme.composables

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mealcycle.app.ui.theme.LocalThemeEngine

/**
 * Glassmorphism card with device-tier optimized blur.
 *
 * Performance strategy (prioritize perceived smoothness over visual intensity):
 * - LOW:  translucent overlay only, no RenderEffect (0–6dp equivalent)
 * - MID:  localized card blur 8–14dp, cached with Offscreen compositing
 * - HIGH: full RenderEffect blur 16–25dp
 *
 * Rules:
 * 1. Localized only — blur this card, never fullscreen
 * 2. Skip during scroll — caller should pass isScrolling flag
 * 3. Cache blurred layers — CompositingStrategy.Offscreen
 * 4. Translucent fallback — on API < 31 or low-end
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isScrolling: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 0.5.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val engine = LocalThemeEngine.current
    val glassConfig by engine?.glassConfig?.collectAsState()
        ?: remember { mutableStateOf(com.mealcycle.app.ui.theme.engine.GlassConfig()) }
    val blurIntensity by engine?.blurIntensity?.collectAsState(initial = 0.6f)
        ?: remember { mutableStateOf(0.6f) }

    // Animate tint color for smooth palette transitions
    val animatedTint by animateColorAsState(
        glassConfig.tintColor.copy(alpha = glassConfig.tintAlpha),
        spring(stiffness = Spring.StiffnessLow),
        label = "glassTint"
    )

    // Determine effective blur radius based on device tier
    val effectiveBlur = remember(glassConfig.blurRadius, blurIntensity, isScrolling) {
        val base = glassConfig.blurRadius * blurIntensity
        if (isScrolling) (base * 0.3f).coerceAtLeast(0f) else base
    }

    val supportsRenderEffect = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Surface(
        modifier = modifier
            .then(
                if (supportsRenderEffect && effectiveBlur > 0f && !isScrolling) {
                    Modifier.graphicsLayer {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                effectiveBlur, effectiveBlur,
                                android.graphics.Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        }
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                } else {
                    Modifier
                }
            ),
        shape = shape,
        color = animatedTint,
        border = BorderStroke(
            borderWidth,
            Color.White.copy(alpha = glassConfig.borderAlpha)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Lightweight translucent card — no blur, just alpha overlay.
 * Always safe on any device/API level.
 * Use this for secondary content or during scrolling.
 */
@Composable
fun TranslucentCard(
    modifier: Modifier = Modifier,
    alpha: Float = 0.12f,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier,
        shape = shape,
        color = surfaceColor.copy(alpha = alpha),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
