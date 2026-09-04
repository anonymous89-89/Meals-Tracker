package com.mealcycle.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealcycle.app.data.datastore.UserPreferences
import com.mealcycle.app.data.db.MonthlyHolidayCount
import com.mealcycle.app.data.db.MonthlyMealCount
import com.mealcycle.app.data.repository.HolidayRepository
import com.mealcycle.app.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class UsageStatsUiState(
    val monthlyData: List<MonthlyMealCount> = emptyList(),
    val monthlyHolidays: Map<String, Int> = emptyMap(),   // "2026-04" -> 2
    val yearlyData: Map<String, Int> = emptyMap(),         // "2024" -> total
    val totalAllTime: Int = 0,
    val totalHolidaysAllTime: Int = 0,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UsageStatsViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val holidayRepository: HolidayRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<UsageStatsUiState> = userPreferences.activeUserId
        .flatMapLatest { userId ->
            val safeId = userId ?: ""
            combine(
                mealRepository.getMonthlyMealCounts(safeId),
                holidayRepository.getMonthlyHolidayCounts(safeId)
            ) { meals, holidays -> Pair(meals, holidays) }
        }
        .map { (monthlyList, holidayList) ->
            val yearlyData = monthlyList
                .groupBy { it.month.take(4) }
                .mapValues { entry -> entry.value.sumOf { it.count } }

            val holidaysByMonth = holidayList.associate { it.month to it.count }

            UsageStatsUiState(
                monthlyData = monthlyList,
                monthlyHolidays = holidaysByMonth,
                yearlyData = yearlyData,
                totalAllTime = monthlyList.sumOf { it.count },
                totalHolidaysAllTime = holidayList.sumOf { it.count },
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UsageStatsUiState())
}
