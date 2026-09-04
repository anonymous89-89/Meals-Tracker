package com.mealcycle.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealcycle.app.data.datastore.UserPreferences
import com.mealcycle.app.data.model.MealEntry
import com.mealcycle.app.data.model.MealType
import com.mealcycle.app.data.model.PlanHistory
import com.mealcycle.app.data.repository.HistoryRepository
import com.mealcycle.app.data.repository.HolidayRepository
import com.mealcycle.app.data.repository.MealRepository
import com.mealcycle.app.utils.MealCalculations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** UI state emitted by HomeViewModel */
data class HomeUiState(
    val activeUserId: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val mealsForDate: List<MealEntry> = emptyList(),
    val allMeals: List<MealEntry> = emptyList(),
    val deliveredCount: Int = 0,
    val isCycleComplete: Boolean = false,
    val showEndPlanDialog: Boolean = false,
    val showResetConfirm: Boolean = false,
    val isLoading: Boolean = true,
    val totalMeals: Int = MealCalculations.DEFAULT_TOTAL_MEALS,
    val pricePerMeal: Int = MealCalculations.DEFAULT_PRICE_PER_MEAL,
    val holidayDates: Set<String> = emptySet(),
    val selectedDateIsHoliday: Boolean = false
)

// Internal data class to bundle settings cleanly — avoids fragile array casting
private data class CycleSettings(
    val userId: String,
    val totalMeals: Int,
    val pricePerMeal: Int,
    val selectedDate: LocalDate,
    val showEndPlanDialog: Boolean,
    val showResetConfirm: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val historyRepository: HistoryRepository,
    private val holidayRepository: HolidayRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _showEndPlanDialog = MutableStateFlow(false)
    private val _showResetConfirm = MutableStateFlow(false)

    // Step 1 — combine only the 3 DataStore prefs (all have same-ish type group)
    // Using nested combine so every lambda has concrete, compiler-checked types.
    private val _prefs = combine(
        userPreferences.activeUserId,
        userPreferences.totalMeals,
        userPreferences.pricePerMeal
    ) { userId, totalMeals, pricePerMeal ->
        Triple(userId ?: "", totalMeals, pricePerMeal)
    }

    // Step 2 — combine the prefs triple with the 3 UI state flows
    private val _settings: Flow<CycleSettings> = combine(
        _prefs,
        _selectedDate,
        _showEndPlanDialog,
        _showResetConfirm
    ) { prefs, date, endDialog, resetDialog ->
        CycleSettings(
            userId            = prefs.first,
            totalMeals        = prefs.second,
            pricePerMeal      = prefs.third,
            selectedDate      = date,
            showEndPlanDialog = endDialog,
            showResetConfirm  = resetDialog
        )
    }

    val uiState: StateFlow<HomeUiState> = _settings.flatMapLatest { settings ->
        combine(
            mealRepository.getMealsForDate(settings.userId, settings.selectedDate.format(DATE_FORMAT)),
            mealRepository.getAllMealsForUser(settings.userId),
            mealRepository.getDeliveredMealCount(settings.userId),
            holidayRepository.getAllHolidays(settings.userId)
        ) { mealsForDate, allMeals, deliveredCount, holidays ->
            val holidayDates = holidays.map { it.date }.toSet()
            HomeUiState(
                activeUserId      = settings.userId,
                selectedDate      = settings.selectedDate,
                mealsForDate      = mealsForDate,
                allMeals          = allMeals,
                deliveredCount    = deliveredCount,
                isCycleComplete   = MealCalculations.isCycleComplete(deliveredCount, settings.totalMeals),
                showEndPlanDialog = settings.showEndPlanDialog,
                showResetConfirm  = settings.showResetConfirm,
                isLoading         = false,
                totalMeals        = settings.totalMeals,
                pricePerMeal      = settings.pricePerMeal,
                holidayDates      = holidayDates,
                selectedDateIsHoliday = settings.selectedDate.format(DATE_FORMAT) in holidayDates
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    // ─── Settings ─────────────────────────────────────────────────────────────

    fun updateTotalMeals(value: Int) {
        viewModelScope.launch { userPreferences.setTotalMeals(value) }
    }

    fun updatePricePerMeal(value: Int) {
        viewModelScope.launch { userPreferences.setPricePerMeal(value) }
    }

    // ─── Holiday ──────────────────────────────────────────────────────────────

    fun toggleHoliday(date: LocalDate) {
        val state = uiState.value
        // Guard: never insert with empty userId — FK constraint violation in Room 2.6.1
        if (state.activeUserId.isBlank()) return
        val dateStr = date.format(DATE_FORMAT)
        viewModelScope.launch {
            try {
                if (dateStr in state.holidayDates) {
                    holidayRepository.unmarkHoliday(state.activeUserId, dateStr)
                } else {
                    holidayRepository.markHoliday(state.activeUserId, dateStr)
                }
            } catch (e: Exception) {
                // FK constraint or other DB error — silently ignore
            }
        }
    }

    // ─── Date Navigation ──────────────────────────────────────────────────────

    fun goToPreviousDay() { _selectedDate.update { it.minusDays(1) } }

    fun goToNextDay(): Boolean {
        // Allow navigating to future dates (for holiday marking)
        _selectedDate.update { it.plusDays(1) }
        return true
    }

    fun selectDate(date: LocalDate) { _selectedDate.value = date }

    fun refreshTodayBoundary() {
        val today = LocalDate.now()
        // Snap back only if we're more than 31 days in the future (unreasonable)
        if (_selectedDate.value.isAfter(today.plusDays(31))) {
            _selectedDate.value = today
        }
    }

    // ─── Error state ──────────────────────────────────────────────────────────

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    fun clearError() { _errorMessage.value = null }

    // ─── Meal Toggles ─────────────────────────────────────────────────────────

    fun toggleMeal(mealType: MealType) {
        val state = uiState.value
        if (state.activeUserId.isBlank()) return
        val dateStr = state.selectedDate.format(DATE_FORMAT)
        val isCurrentlyDelivered = state.mealsForDate
            .any { it.mealType == mealType.name && it.isDelivered }

        viewModelScope.launch {
            try {
                mealRepository.toggleMeal(
                    userId = state.activeUserId,
                    date = dateStr,
                    mealType = mealType.name,
                    isCurrentlyDelivered = isCurrentlyDelivered,
                    currentDeliveredCount = state.deliveredCount,
                    totalMeals = state.totalMeals
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to toggle meal. Please try again."
            }
        }
    }

    fun selectFullDay() {
        val state = uiState.value
        if (state.activeUserId.isBlank() || state.isCycleComplete) return
        viewModelScope.launch {
            try {
                var remaining = MealCalculations.remainingMeals(state.deliveredCount, state.totalMeals)
                MealType.entries.forEach { mealType ->
                    if (remaining <= 0) return@forEach
                    val isDelivered = state.mealsForDate
                        .any { it.mealType == mealType.name && it.isDelivered }
                    if (!isDelivered) {
                        mealRepository.toggleMeal(
                            userId = state.activeUserId,
                            date = state.selectedDate.format(DATE_FORMAT),
                            mealType = mealType.name,
                            isCurrentlyDelivered = false,
                            currentDeliveredCount = state.totalMeals - remaining,
                            totalMeals = state.totalMeals
                        )
                        remaining--
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save meals. Please try again."
            }
        }
    }

    // ─── End Plan / Reset ─────────────────────────────────────────────────────

    fun showEndPlanDialog() { _showEndPlanDialog.value = true }
    fun dismissEndPlanDialog() { _showEndPlanDialog.value = false }
    fun showResetConfirm() { _showResetConfirm.value = true }
    fun dismissResetConfirm() { _showResetConfirm.value = false }

    fun confirmEndPlan() {
        val state = uiState.value
        if (state.activeUserId.isBlank()) return
        viewModelScope.launch {
            try {
                val startDate = mealRepository.getEarliestDeliveredDate(state.activeUserId)
                    ?: state.selectedDate.format(DATE_FORMAT)
                val delivered = state.deliveredCount
                val remaining = MealCalculations.remainingMeals(delivered, state.totalMeals)
                historyRepository.insertPlanHistory(
                    PlanHistory(
                        userId       = state.activeUserId,
                        startDate    = startDate,
                        endDate      = LocalDate.now().format(DATE_FORMAT),
                        deliveredMeals = delivered,
                        amountSpent  = MealCalculations.amountSpent(delivered, state.pricePerMeal),
                        refundAmount = MealCalculations.refundAmount(remaining, state.pricePerMeal)
                    )
                )
                mealRepository.deleteAllMealsForUser(state.activeUserId)
                holidayRepository.deleteAllForUser(state.activeUserId)
                _showEndPlanDialog.value = false
                _selectedDate.value = LocalDate.now()
            } catch (e: Exception) {
                _showEndPlanDialog.value = false
                _errorMessage.value = "Failed to end plan. Please try again."
            }
        }
    }

    fun confirmReset() {
        val state = uiState.value
        if (state.activeUserId.isBlank()) return
        viewModelScope.launch {
            try {
                mealRepository.deleteAllMealsForUser(state.activeUserId)
                holidayRepository.deleteAllForUser(state.activeUserId)
                _showResetConfirm.value = false
                _selectedDate.value = LocalDate.now()
            } catch (e: Exception) {
                _showResetConfirm.value = false
                _errorMessage.value = "Failed to reset plan. Please try again."
            }
        }
    }
}
