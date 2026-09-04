package com.mealcycle.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mealcycle.app.R
import com.mealcycle.app.ui.components.GlassmorphicCard
import com.mealcycle.app.ui.theme.*
import com.mealcycle.app.utils.MealCalculations
import kotlinx.coroutines.delay

/**
 * Row of stats cards: Delivered, Remaining, Time, Spent, Refund.
 * Premium glassmorphic design with smooth animations and interactive elements.
 */
@Composable
fun StatsCardsRow(
    deliveredCount: Int,
    totalMeals: Int,
    pricePerMeal: Int,
    modifier: Modifier = Modifier
) {
    val remaining = MealCalculations.remainingMeals(deliveredCount, totalMeals)
    val spent = MealCalculations.amountSpent(deliveredCount, pricePerMeal)
    val refund = MealCalculations.refundAmount(remaining, pricePerMeal)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Row 1: Delivered + Remaining
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCardPremium(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.CheckCircle,
                iconTint = Delivered,
                iconBgColor = DeliveredContainer,
                label = stringResource(R.string.delivered_meals),
                value = "$deliveredCount / $totalMeals",
                animationDelayMs = 0
            )
            StatCardPremium(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.HourglassEmpty,
                iconTint = Primary,
                iconBgColor = PrimaryContainer,
                label = stringResource(R.string.remaining_meals),
                value = "$remaining ${stringResource(R.string.meals)}",
                animationDelayMs = 80
            )
        }

        // Row 2: Spent + Refund
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCardPremium(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.CurrencyRupee,
                iconTint = Amber,
                iconBgColor = AmberContainer,
                label = stringResource(R.string.amount_spent),
                value = "₹$spent",
                animationDelayMs = 160
            )
            StatCardPremium(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Replay,
                iconTint = Delivered,
                iconBgColor = DeliveredContainer,
                label = stringResource(R.string.refund_amount),
                value = "₹$refund",
                animationDelayMs = 240
            )
        }
    }
}

/**
 * Premium stat card with glassmorphism and staggered entrance animation.
 */
@Composable
private fun StatCardPremium(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    label: String,
    value: String,
    animationDelayMs: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (animationDelayMs > 0) delay(animationDelayMs.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "card_alpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else 18f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_offset"
    )

    GlassmorphicCard(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            translationY = translateY
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(iconBgColor, iconBgColor.copy(alpha = 0.7f))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TimeStatCard(modifier: Modifier = Modifier, text: String) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PrimaryContainer, PrimaryContainer.copy(alpha = 0.5f))
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.remaining_time),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnPrimaryContainer
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}
