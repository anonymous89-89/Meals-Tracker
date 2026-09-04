package com.mealcycle.app.ui.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mealcycle.app.R
import com.mealcycle.app.ui.calendar.CalendarView
import com.mealcycle.app.ui.navigation.AnimatedNavigationBar
import com.mealcycle.app.ui.navigation.NavItem
import com.mealcycle.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Root Home screen composable.
 * Handles swipe-based day navigation, date picker, stats, calendar, and meal toggles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Re-evaluate today boundary every time screen resumes from background
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshTodayBoundary()
    }

    // Show errors in a Snackbar
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!)
            viewModel.clearError()
        }
    }

    // Swipe gesture: left = next day, right = previous day
    val swipeModifier = Modifier.pointerInput(Unit) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onDragEnd = {
                when {
                    totalDrag < -60f -> viewModel.goToNextDay()    // swipe left → next
                    totalDrag > 60f  -> viewModel.goToPreviousDay() // swipe right → prev
                }
                totalDrag = 0f
            }
        ) { _, dragAmount -> totalDrag += dragAmount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                },
                actions = {
                    // Settings icon — opens cycle size and price editor
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Cycle Settings", tint = Primary)
                    }
                    // Calendar / date-picker icon
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = stringResource(R.string.pick_date), tint = Primary)
                    }
                    // Profile avatar button
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = stringResource(R.string.profile), tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            AnimatedNavigationBar(
                items = listOf(
                    NavItem(Icons.Filled.Home, "Home") {},
                    NavItem(Icons.Filled.BarChart, "Stats", onNavigateToStats),
                    NavItem(Icons.Filled.History, "History", onNavigateToHistory),
                    NavItem(Icons.Filled.Person, "Profile", onNavigateToProfile)
                ),
                selectedIndex = 0
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .then(swipeModifier)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Greeting header ─────────────────────────────────────────────
            val greeting = when (LocalTime.now().hour) {
                in 5..11 -> "Good morning 👋"
                in 12..16 -> "Good afternoon ☀️"
                in 17..20 -> "Good evening 🌅"
                else -> "Good night 🌙"
            }
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // ── Date header with prev/next arrows + holiday toggle ───────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DateNavigationHeader(
                    selectedDate = uiState.selectedDate,
                    onPrevious = { viewModel.goToPreviousDay() },
                    onNext = { viewModel.goToNextDay() }
                )
                // Holiday toggle chip — tap to mark/unmark as holiday
                val isHoliday = uiState.selectedDateIsHoliday
                FilterChip(
                    selected = isHoliday,
                    onClick = { viewModel.toggleHoliday(uiState.selectedDate) },
                    label = { Text(if (isHoliday) "Holiday ✓" else "Mark as Holiday") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.BeachAccess,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // ── Stats cards row ──────────────────────────────────────────────
            StatsCardsRow(
                deliveredCount = uiState.deliveredCount,
                totalMeals = uiState.totalMeals,
                pricePerMeal = uiState.pricePerMeal
            )

            // ── Animated progress bars — GradientProgressBar wraps in GlassmorphicCard internally
            CycleProgressBar(
                deliveredCount = uiState.deliveredCount,
                totalMeals = uiState.totalMeals
            )

            RemainingDaysBar(
                deliveredCount = uiState.deliveredCount,
                totalMeals = uiState.totalMeals
            )

            // ── Monthly Calendar ─────────────────────────────────────────────
            CalendarView(
                selectedDate = uiState.selectedDate,
                allMeals = uiState.allMeals,
                holidayDates = uiState.holidayDates,
                onDateSelected = { viewModel.selectDate(it) }
            )

            // ── Meal toggles for selected date ───────────────────────────────
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MealButtons(
                        mealsForDate = uiState.mealsForDate,
                        isCycleComplete = uiState.isCycleComplete,
                        isHoliday = uiState.selectedDateIsHoliday,
                        onToggleMeal = { viewModel.toggleMeal(it) },
                        onSelectFullDay = { viewModel.selectFullDay() },
                        onToggleHoliday = { viewModel.toggleHoliday(uiState.selectedDate) }
                    )
                }
            }

            // ── End Plan button (full width) ─────────────────────────────
            Button(
                onClick = { viewModel.showEndPlanDialog() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Filled.StopCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.end_plan))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // ── Date picker dialog ───────────────────────────────────────────────────
    if (showDatePicker) {
        // selectableDates goes in rememberDatePickerState(), NOT in DatePicker()
        // This is the correct API location in Material 3 1.2.1 (BOM 2024.06.00)
        val selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val picked = LocalDate.ofEpochDay(utcTimeMillis / 86_400_000L)
                return !picked.isAfter(LocalDate.now())
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate
                .toEpochDay() * 86_400_000L,
            selectableDates = selectableDates
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = LocalDate.ofEpochDay(millis / 86_400_000L)
                        viewModel.selectDate(picked)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── End Plan bottom sheet ────────────────────────────────────────────────
    if (uiState.showEndPlanDialog) {
        EndPlanDialog(
            deliveredCount = uiState.deliveredCount,
            totalMeals = uiState.totalMeals,
            pricePerMeal = uiState.pricePerMeal,
            onDismiss = { viewModel.dismissEndPlanDialog() },
            onConfirm = { viewModel.confirmEndPlan() }
        )
    }

    // ── Reset Plan confirmation dialog ───────────────────────────────────────
    if (uiState.showResetConfirm) {
        ResetPlanDialog(
            onDismiss = { viewModel.dismissResetConfirm() },
            onConfirm = { viewModel.confirmReset() }
        )
    }

    // ── Cycle settings dialog (gear icon) ────────────────────────────────────
    if (showSettings) {
        CycleSettingsDialog(
            currentTotalMeals   = uiState.totalMeals,
            currentPricePerMeal = uiState.pricePerMeal,
            onDismiss = { showSettings = false },
            onSave = { newTotal, newPrice ->
                viewModel.updateTotalMeals(newTotal)
                viewModel.updatePricePerMeal(newPrice)
                showSettings = false
            }
        )
    }
}

/**
 * Date row with left/right arrow buttons and the current date label.
 * The right arrow is disabled when selectedDate >= today.
 */
@Composable
private fun DateNavigationHeader(
    selectedDate: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val today = LocalDate.now()
    val isAtToday = !selectedDate.isBefore(today)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day", tint = Primary)
        }
        Text(
            text = when {
                selectedDate == today -> stringResource(R.string.today) + " · " +
                        selectedDate.format(DateTimeFormatter.ofPattern("d MMM"))
                else -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onNext, enabled = !isAtToday) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Next day",
                tint = if (!isAtToday) Primary else Undelivered
            )
        }
    }
}
