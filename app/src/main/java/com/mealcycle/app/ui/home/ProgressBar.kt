package com.mealcycle.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mealcycle.app.R
import com.mealcycle.app.ui.components.GlassmorphicCard
import com.mealcycle.app.ui.theme.*
import com.mealcycle.app.utils.MealCalculations

/**
 * Premium glassmorphic progress bar with gradient fill and animations.
 */
@Composable
private fun GradientProgressBar(
    label: String,
    badgeText: String,
    badgeBgColor: Color,
    badgeTextColor: Color,
    fraction: Float,
    footerLeft: String,
    footerRight: String? = null,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    // Smooth fill animation with physics-based spring
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "progress_fill"
    )

    GlassmorphicCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ── Header: label + animated badge ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Animated badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeBgColor,
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // ── Gradient progress bar with enhanced visual ──────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Main fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(brush = Brush.horizontalGradient(colors = gradientColors))
                )
                
                // Shimmer/shine effect overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f * animatedFraction),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // ── Footer ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = footerLeft,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                if (footerRight != null) {
                    Text(
                        text = footerRight,
                        style = MaterialTheme.typography.labelSmall,
                        color = Delivered,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ─── Public API ──────────────────────────────────────────────────────────────

/**
 * Cycle Progress – delivered / total meals.
 */
@Composable
fun CycleProgressBar(
    deliveredCount: Int,
    totalMeals: Int = MealCalculations.DEFAULT_TOTAL_MEALS,
    modifier: Modifier = Modifier
) {
    val percent = MealCalculations.progressPercent(deliveredCount, totalMeals)
    val fraction = MealCalculations.progressFraction(deliveredCount, totalMeals)

    GradientProgressBar(
        label = stringResource(R.string.cycle_progress),
        badgeText = "$percent%",
        badgeBgColor = if (percent >= 100) DeliveredContainer else PrimaryContainer,
        badgeTextColor = if (percent >= 100) Delivered else Primary,
        fraction = fraction,
        footerLeft = "$deliveredCount / $totalMeals ${stringResource(R.string.meals)}",
        footerRight = if (percent >= 100) "🎉 Complete!" else null,
        gradientColors = if (percent >= 100)
            listOf(Delivered, DeliveredDark)
        else
            listOf(Primary, Color(0xFF818CF8), Delivered),
        modifier = modifier
    )
}

/**
 * Remaining Days – shows remaining days with amber gradient.
 */
@Composable
fun RemainingDaysBar(
    deliveredCount: Int,
    totalMeals: Int = MealCalculations.DEFAULT_TOTAL_MEALS,
    modifier: Modifier = Modifier
) {
    val remaining = MealCalculations.remainingMeals(deliveredCount, totalMeals)
    val days = MealCalculations.remainingDays(remaining)
    val extra = MealCalculations.remainingExtraMeals(remaining)
    val totalDays = totalMeals / 3
    val fraction = (days.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)

    GradientProgressBar(
        label = stringResource(R.string.remaining_time),
        badgeText = "$days days",
        badgeBgColor = if (days == 0) DeliveredContainer else AmberContainer,
        badgeTextColor = if (days == 0) Delivered else Amber,
        fraction = fraction,
        footerLeft = "$days days and $extra meals remaining",
        footerRight = if (days == 0) "Plan ended" else null,
        gradientColors = if (days <= 5)
            listOf(Color(0xFFEF4444), Amber)
        else
            listOf(Amber, AmberDark, Color(0xFFF97316)),
        modifier = modifier
    )
}
