package com.mealcycle.app.ui.theme.engine

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Device performance tier — computed once at app launch.
 * Determines blur radius caps, animation complexity, and effect intensity.
 *
 * Philosophy: prioritize perceived smoothness over visual intensity.
 */
enum class DeviceTier(
    val maxBlurRadius: Float,
    val enableRenderEffect: Boolean,
    val enableShimmer: Boolean
) {
    LOW(maxBlurRadius = 6f, enableRenderEffect = false, enableShimmer = false),
    MID(maxBlurRadius = 14f, enableRenderEffect = true, enableShimmer = false),
    HIGH(maxBlurRadius = 25f, enableRenderEffect = true, enableShimmer = true);
}

@Singleton
class DeviceTierDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val tier: DeviceTier by lazy { detectTier() }

    private fun detectTier(): DeviceTier {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val totalGb = memInfo.totalMem / (1024.0 * 1024 * 1024)

        return when {
            totalGb < 4.0 -> DeviceTier.LOW
            totalGb < 8.0 -> DeviceTier.MID
            else -> DeviceTier.HIGH
        }
    }

    /**
     * Clamp a requested blur radius to the device's safe maximum.
     * During scroll, further halve the radius for smooth 60fps.
     */
    fun safeBlur(requested: Float, isScrolling: Boolean = false): Float {
        val capped = requested.coerceIn(0f, tier.maxBlurRadius)
        return if (isScrolling) (capped * 0.3f).coerceAtLeast(0f) else capped
    }
}
