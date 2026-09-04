package com.mealcycle.app.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mealcycle.app.R
import com.mealcycle.app.ui.theme.*
import com.mealcycle.app.ui.navigation.AnimatedNavigationBar
import com.mealcycle.app.ui.navigation.NavItem
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageStatsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: UsageStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Usage Statistics", fontWeight = FontWeight.Bold, color = Primary)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            AnimatedNavigationBar(
                items = listOf(
                    NavItem(Icons.Filled.Home, "Home", onNavigateToHome),
                    NavItem(Icons.Filled.BarChart, "Stats") {},
                    NavItem(Icons.Filled.History, "History", onNavigateToHistory),
                    NavItem(Icons.Filled.Person, "Profile", onNavigateToProfile)
                ),
                selectedIndex = 1
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        if (uiState.monthlyData.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.BarChart, null, tint = Undelivered, modifier = Modifier.size(64.dp))
                    Text("No meal data yet", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Start marking meals to see statistics", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Scaffold
        }

        val maxCount = uiState.monthlyData.maxOf { it.count }.coerceAtLeast(1)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── All-time summary card ─────────────────────────────────────────
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatSummaryCell(emoji = "🍽️", value = "${uiState.totalAllTime}", label = "Total Meals")
                        Box(Modifier.width(1.dp).height(48.dp).background(Undelivered))
                        StatSummaryCell(emoji = "📅", value = "${uiState.yearlyData.size}", label = "Years Active")
                        Box(Modifier.width(1.dp).height(48.dp).background(Undelivered))
                        StatSummaryCell(
                            emoji = "📈",
                            value = if (uiState.monthlyData.isNotEmpty())
                                "${uiState.totalAllTime / uiState.monthlyData.size}"
                            else "–",
                            label = "Avg / Month"
                        )
                    }
                }
            }

            // ── Holiday summary card (only if holidays exist) ────────────────
            if (uiState.totalHolidaysAllTime > 0) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Holiday.copy(alpha = 0.15f),
                                            Holiday.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Holiday.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_holiday_calendar),
                                    contentDescription = "Holiday",
                                    modifier = Modifier.size(22.dp),
                                    tint = Color.Unspecified
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Total Holidays",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${uiState.totalHolidaysAllTime} days",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Holiday
                                )
                            }
                        }
                    }
                }
            }

            // ── Yearly sections ───────────────────────────────────────────────
            uiState.yearlyData.entries.sortedByDescending { it.key }.forEach { (year, yearTotal) ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, color = Primary
                        )
                        Text(
                            "$yearTotal meals delivered in $year",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val monthsInYear = uiState.monthlyData.filter { it.month.startsWith(year) }
                items(monthsInYear) { entry ->
                    val holidayCount = uiState.monthlyHolidays[entry.month] ?: 0
                    MonthBarRow(entry = entry, maxCount = maxCount, holidayCount = holidayCount)
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun StatSummaryCell(emoji: String, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.widthIn(min = 80.dp)
    ) {
        Text(emoji, fontSize = 22.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MonthBarRow(
    entry: com.mealcycle.app.data.db.MonthlyMealCount,
    maxCount: Int,
    holidayCount: Int = 0
) {
    val fraction = entry.count.toFloat() / maxCount.toFloat()
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(600),
        label = "bar_anim"
    )

    val monthName = try {
        val parts = entry.month.split("-")
        Month.of(parts[1].toInt()).getDisplayName(TextStyle.FULL, Locale.getDefault())
    } catch (e: Exception) { entry.month }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(monthName, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Holiday badge (only shown if holidays exist for this month)
                    if (holidayCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Holiday.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_holiday_calendar),
                                    contentDescription = "Holiday",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.Unspecified
                                )
                                Text(
                                    "$holidayCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Holiday
                                )
                            }
                        }
                    }
                    Text("${entry.count} meals", style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold, color = Primary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Undelivered)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction).fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Primary, Delivered)))
                )
            }
        }
    }
}
