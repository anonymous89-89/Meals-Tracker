package com.mealcycle.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mealcycle.app.R
import com.mealcycle.app.data.model.MealEntry
import com.mealcycle.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Monday-first week order — consistent across all locales. */
private val WEEK_DAYS = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY
)

/**
 * Custom monthly calendar composable — no third-party library.
 *
 * Day cell indicators:
 * - No meals      → light grey dot
 * - 1–2 meals     → amber/yellow dot
 * - All 3 meals   → filled primary circle
 * - Today         → underline ring
 * - Selected date → bold accent border + background tint
 */
@Composable
fun CalendarView(
    selectedDate: LocalDate,
    allMeals: List<MealEntry>,
    holidayDates: Set<String> = emptySet(),
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    var displayMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    // Group delivered meals by date for O(1) lookup
    val deliveredByDate: Map<String, Int> = remember(allMeals) {
        allMeals
            .filter { it.isDelivered }
            .groupBy { it.date }
            .mapValues { it.value.size }
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Month navigation header ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { displayMonth = displayMonth.minusMonths(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = Primary)
                }
                Text(
                    text = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = { displayMonth = displayMonth.plusMonths(1) }
                ) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = "Next month",
                        tint = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Day-of-week header row ───────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                // WEEK_DAYS is a Monday-first explicit list — locale-safe
                WEEK_DAYS.forEach { dow ->
                    Text(
                        text = dow.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Day cells ───────────────────────────────────────────────────
            val firstDay = displayMonth.atDay(1)
            // Offset: how many empty cells before the 1st (Mon=0, Tue=1, ...)
            val startOffset = (firstDay.dayOfWeek.value - 1) // 0 = Monday

            val totalDays = displayMonth.lengthOfMonth()
            val totalCells = startOffset + totalDays
            val rows = (totalCells + 6) / 7  // ceiling division

            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { col ->
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - startOffset + 1

                        if (dayNumber < 1 || dayNumber > totalDays) {
                            // Empty cell
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val date = displayMonth.atDay(dayNumber)
                            val isFuture = date.isAfter(today)
                            val isToday = date == today
                            val isSelected = date == selectedDate
                            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val mealCount = deliveredByDate[dateStr] ?: 0
                            val isHoliday = dateStr in holidayDates

                            DayCell(
                                modifier = Modifier.weight(1f),
                                dayNumber = dayNumber,
                                isToday = isToday,
                                isSelected = isSelected,
                                isFuture = isFuture,
                                isHoliday = isHoliday,
                                mealCount = mealCount,
                                onClick = { onDateSelected(date) }  // all dates tappable
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Legend ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                LegendItem(color = Undelivered, label = "No meals")
                LegendItem(color = Amber, label = "1–2 meals")
                LegendItem(color = Primary, label = "All 3")
                LegendItem(color = Color(0xFFFF9800), label = "Holiday")
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    isHoliday: Boolean = false,
    mealCount: Int,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> Primary.copy(alpha = 0.15f)
        else -> Color.Transparent
    }
    val borderColor = when {
        isSelected -> Primary
        else -> Color.Transparent
    }
    val textColor = when {
        isFuture -> Undelivered
        isSelected -> Primary
        isToday -> Primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val dotColor = when {
        mealCount >= 3 -> Primary
        mealCount in 1..2 -> Amber
        isFuture -> Color.Transparent
        else -> Undelivered.copy(alpha = 0.6f)
    }
    val dotFilled = mealCount >= 3
    val showHolidayDot = isHoliday

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),  // All dates clickable (for holiday marking on future dates)
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                fontSize = 13.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                lineHeight = 16.sp
            )
            // Indicator below date number
            if (!isFuture || isHoliday) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isFuture && !isHoliday) {
                        // Blue dot for 3 meals, yellow for partial, grey for none
                        Box(
                            modifier = Modifier
                                .size(if (dotFilled) 6.dp else 4.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                    if (showHolidayDot) {
                        // Holiday calendar icon from drawable
                        Icon(
                            painter = painterResource(id = R.drawable.ic_holiday_calendar),
                            contentDescription = "Holiday",
                            modifier = Modifier.size(12.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }

        // Today underline ring
        if (isToday && !isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, Primary.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
