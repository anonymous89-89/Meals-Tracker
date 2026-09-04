package com.mealcycle.app.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mealcycle.app.R
import com.mealcycle.app.data.model.MealEntry
import com.mealcycle.app.data.model.MealType
import com.mealcycle.app.ui.theme.*

/**
 * Breakfast / Lunch / Dinner toggle buttons + Full Day + Mark as Holiday.
 * Full Day and Holiday sit side-by-side, each ~half width.
 */
@Composable
fun MealButtons(
    mealsForDate: List<MealEntry>,
    isCycleComplete: Boolean,
    isHoliday: Boolean = false,
    onToggleMeal: (MealType) -> Unit,
    onSelectFullDay: () -> Unit,
    onToggleHoliday: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Cycle complete overlay banner
        if (isCycleComplete) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DeliveredContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Delivered,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.cycle_complete),
                        style = MaterialTheme.typography.labelLarge,
                        color = Delivered,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Three meal toggle buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MealType.entries.forEach { mealType ->
                val isDelivered = mealsForDate.any { it.mealType == mealType.name && it.isDelivered }
                MealToggleButton(
                    modifier = Modifier.weight(1f),
                    mealType = mealType,
                    isDelivered = isDelivered,
                    enabled = !isCycleComplete,
                    onClick = { onToggleMeal(mealType) }
                )
            }
        }

        // Full Day + Mark as Holiday — side by side, half-width each
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Full Day button (half width)
            Button(
                onClick = onSelectFullDay,
                enabled = !isCycleComplete,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Undelivered
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = stringResource(R.string.full_day),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            // Mark as Holiday button (half width)
            val holidayBg by animateColorAsState(
                targetValue = if (isHoliday) Holiday else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(300),
                label = "holiday_bg"
            )
            val holidayContent = if (isHoliday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

            Button(
                onClick = onToggleHoliday,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = holidayBg,
                    contentColor = holidayContent
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isHoliday) 4.dp else 1.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_holiday_calendar),
                    contentDescription = "Holiday",
                    modifier = Modifier.size(16.dp),
                    tint = if (isHoliday) Color.White else Color.Unspecified
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isHoliday) "Holiday ✓" else "Holiday",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun MealToggleButton(
    modifier: Modifier = Modifier,
    mealType: MealType,
    isDelivered: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isDelivered) Delivered else SurfaceVariant,
        animationSpec = tween(durationMillis = 300),
        label = "meal_toggle_color_${mealType.name}"
    )
    val contentColor: Color = if (isDelivered) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    // Scale animation on toggle
    val scale by animateFloatAsState(
        targetValue = if (isDelivered) 1.0f else 0.97f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "meal_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(64.dp)
            .scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = contentColor,
            disabledContainerColor = bgColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isDelivered) 4.dp else 1.dp
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = if (isDelivered) Icons.Filled.Check else when (mealType) {
                    MealType.BREAKFAST -> Icons.Filled.WbSunny
                    MealType.LUNCH     -> Icons.Filled.LunchDining
                    MealType.DINNER    -> Icons.Filled.Bedtime
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            Text(
                text = mealType.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                fontSize = 11.sp
            )
        }
    }
}
