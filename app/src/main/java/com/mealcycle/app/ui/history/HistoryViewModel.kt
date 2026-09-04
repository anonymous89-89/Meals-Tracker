package com.mealcycle.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealcycle.app.data.datastore.UserPreferences
import com.mealcycle.app.data.model.PlanHistory
import com.mealcycle.app.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HistoryUiState(
    val activeUserId: String = "",
    val planHistory: List<PlanHistory> = emptyList(),
    val totalAmountSpent: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = userPreferences.activeUserId
        .flatMapLatest { userId ->
            val safeId = userId ?: ""
            combine(
                historyRepository.getPlanHistory(safeId),
                historyRepository.getTotalAmountSpent(safeId)
            ) { history, total ->
                HistoryUiState(
                    activeUserId = safeId,
                    planHistory = history,
                    totalAmountSpent = total ?: 0
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = HistoryUiState()
        )
}
