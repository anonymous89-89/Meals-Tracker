package com.mealcycle.app.ui.utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Animation specifications for premium feel
 */
object PremiumAnimationSpecs {
    val FAST_SPRING = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    val MEDIUM_SPRING = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val SLOW_SPRING = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val FAST_TWEEN = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
    val MEDIUM_TWEEN = tween<Float>(durationMillis = 400, easing = FastOutSlowInEasing)
    val SLOW_TWEEN = tween<Float>(durationMillis = 600, easing = FastOutSlowInEasing)
}

/**
 * Animates icon entrance with scale and rotation
 */
@Composable
fun AnimatedIconEntrance(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    durationMillis: Int = 600
) {
    // One-shot entrance: scale 0f -> 1f, rotation -180f -> 0f, then settle.
    val scale = remember { Animatable(0f) }
    val rotation = remember { Animatable(-180f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f))
            )
        }
        rotation.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .scale(scale.value)
            .graphicsLayer {
                rotationZ = rotation.value
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Bounce animation effect
 */
@Composable
fun rememberBounceAnimation(
    initialValue: Float = 0f,
    targetValue: Float = 1f,
    bounces: Int = 3
): Float {
    val transition = rememberInfiniteTransition(label = "bounce")
    
    return transition.animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    ).value
}

/**
 * Floating animation for UI elements
 */
@Composable
fun rememberFloatingAnimation(
    range: Float = 8f,
    durationMillis: Int = 2000
): Float {
    val transition = rememberInfiniteTransition(label = "floating")
    
    return transition.animateFloat(
        initialValue = -range,
        targetValue = range,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    ).value
}

/**
 * Wiggle animation (side-to-side)
 */
@Composable
fun rememberWiggleAnimation(
    degrees: Float = 5f,
    durationMillis: Int = 400
): Float {
    val transition = rememberInfiniteTransition(label = "wiggle")
    
    return transition.animateFloat(
        initialValue = -degrees,
        targetValue = degrees,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggle"
    ).value
}

/**
 * Attention shake animation
 */
@Composable
fun rememberShakeAnimation(
    intensity: Float = 10f
): Float {
    val transition = rememberInfiniteTransition(label = "shake")
    
    return transition.animateFloat(
        initialValue = -intensity,
        targetValue = intensity,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    ).value
}

/**
 * Fade in/out animation state
 */
@Composable
fun rememberFadeAnimation(
    visible: Boolean,
    durationMillis: Int = 300
): Float {
    return animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "fade"
    ).value
}
