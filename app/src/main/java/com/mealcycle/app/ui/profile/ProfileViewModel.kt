package com.mealcycle.app.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealcycle.app.data.backup.DataBackupManager
import com.mealcycle.app.data.datastore.UserPreferences
import com.mealcycle.app.data.export.ExportManager
import com.mealcycle.app.data.export.PdfExporter
import com.mealcycle.app.data.model.User
import com.mealcycle.app.data.repository.HistoryRepository
import com.mealcycle.app.data.repository.HolidayRepository
import com.mealcycle.app.data.repository.MealRepository
import com.mealcycle.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class ProfileUiState(
    val users: List<User> = emptyList(),
    val activeUserId: String = "",
    val showAddUserDialog: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val mealRepository: MealRepository,
    private val historyRepository: HistoryRepository,
    private val holidayRepository: HolidayRepository,
    private val userPreferences: UserPreferences,
    private val dataBackupManager: DataBackupManager,
    private val exportManager: ExportManager,
    private val pdfExporter: PdfExporter
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        userRepository.getAllUsers(),
        userPreferences.activeUserId
    ) { users, activeId ->
        ProfileUiState(
            users = users,
            activeUserId = activeId ?: "",
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ProfileUiState()
    )

    private val _showAddUser = MutableStateFlow(false)
    val showAddUserDialog: StateFlow<Boolean> = _showAddUser

    // ─── Snackbar Messages ───────────────────────────────────────────────────
    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()
    fun clearSnack() { _snackMessage.value = null }

    // ─── Theme Mode ─────────────────────────────────────────────────────────
    val themeMode: StateFlow<String> = userPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "auto")

    suspend fun setThemeMode(mode: String) {
        userPreferences.setThemeMode(mode)
    }

    // ─── Font Color Preset (dark mode) ──────────────────────────────────────
    val fontColorPreset: StateFlow<String> = userPreferences.fontColorPreset
        .stateIn(viewModelScope, SharingStarted.Eagerly, "default")

    fun setFontColorPreset(preset: String) {
        viewModelScope.launch { userPreferences.setFontColorPreset(preset) }
    }

    fun showAddUserDialog() { _showAddUser.value = true }
    fun dismissAddUserDialog() { _showAddUser.value = false }

    /** Create a new user with the given name and optional photo URI, then switch to it. */
    fun addUser(name: String, photoUri: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newUser = User(
                userId = UUID.randomUUID().toString(),
                name = name.trim(),
                photoUri = photoUri
            )
            userRepository.insertUser(newUser)
            // Auto-switch to new user if this is the first user being created
            if (uiState.value.activeUserId.isEmpty()) {
                userPreferences.setActiveUserId(newUser.userId)
            }
            _showAddUser.value = false
        }
    }

    /** Switch the active user. */
    fun switchUser(userId: String) {
        viewModelScope.launch {
            userPreferences.setActiveUserId(userId)
        }
    }

    /** Update an existing user's name. */
    fun updateName(userId: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val existing = userRepository.getUserById(userId) ?: return@launch
            userRepository.insertUser(existing.copy(name = newName.trim()))
            _snackMessage.value = "Name updated"
        }
    }

    /** Update an existing user's photo URI. */
    fun updatePhoto(userId: String, photoUri: String) {
        viewModelScope.launch {
            val existing = userRepository.getUserById(userId) ?: return@launch
            userRepository.insertUser(existing.copy(photoUri = photoUri))
        }
    }

    /** Update both name and photo in one call (from edit dialog). */
    fun updateUser(userId: String, newName: String, newPhotoUri: String?) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val existing = userRepository.getUserById(userId) ?: return@launch
            userRepository.insertUser(existing.copy(
                name = newName.trim(),
                photoUri = newPhotoUri ?: existing.photoUri
            ))
            _snackMessage.value = "Profile updated"
        }
    }

    /** Delete a user and all their data, then fall back to another user if available. */
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            mealRepository.deleteAllMealsForUser(userId)
            historyRepository.deleteAllHistoryForUser(userId)
            userRepository.deleteUser(userId)

            // If the deleted user was active, switch to another or clear
            if (uiState.value.activeUserId == userId) {
                val remaining = uiState.value.users.filter { it.userId != userId }
                if (remaining.isNotEmpty()) {
                    userPreferences.setActiveUserId(remaining.first().userId)
                } else {
                    userPreferences.clearActiveUserId()
                }
            }
        }
    }

    // ─── Data Backup ─────────────────────────────────────────────────────────

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            try {
                val count = dataBackupManager.exportToUri(uri)
                _snackMessage.value = "Exported $count records successfully"
            } catch (e: Exception) {
                _snackMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            try {
                val count = dataBackupManager.importFromUri(uri)
                _snackMessage.value = "Imported $count records successfully"
            } catch (e: Exception) {
                _snackMessage.value = "Import failed: ${e.message}"
            }
        }
    }

    // ─── Meal Export (PDF / JSON) ────────────────────────────────────────────

    fun exportToJson(uri: Uri, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            try {
                val state = uiState.value
                val userName = state.users.firstOrNull { it.userId == state.activeUserId }?.name ?: "User"
                val count = exportManager.exportToJson(uri, state.activeUserId, userName, startDate, endDate)
                _snackMessage.value = "Exported $count day(s) as JSON"
            } catch (e: Exception) {
                _snackMessage.value = "JSON export failed: ${e.message}"
            }
        }
    }

    fun exportToPdf(uri: Uri, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            try {
                val state = uiState.value
                val userName = state.users.firstOrNull { it.userId == state.activeUserId }?.name ?: "User"
                val grouped = exportManager.getMealsGroupedByDate(state.activeUserId, startDate, endDate)
                val count = pdfExporter.exportToPdf(uri, userName, startDate, endDate, grouped)
                _snackMessage.value = "Exported $count day(s) as PDF"
            } catch (e: Exception) {
                _snackMessage.value = "PDF export failed: ${e.message}"
            }
        }
    }

    // ─── Reset Plan ─────────────────────────────────────────────────────────
    fun resetPlan() {
        val state = uiState.value
        if (state.activeUserId.isBlank()) return
        viewModelScope.launch {
            try {
                mealRepository.deleteAllMealsForUser(state.activeUserId)
                holidayRepository.deleteAllForUser(state.activeUserId)
                _snackMessage.value = "Plan reset successfully"
            } catch (e: Exception) {
                _snackMessage.value = "Failed to reset plan: ${e.message}"
            }
        }
    }

    // ─── Clear History ──────────────────────────────────────────────────────
    fun clearHistory() {
        val state = uiState.value
        if (state.activeUserId.isBlank()) return
        viewModelScope.launch {
            try {
                historyRepository.deleteAllHistoryForUser(state.activeUserId)
                _snackMessage.value = "History cleared successfully"
            } catch (e: Exception) {
                _snackMessage.value = "Failed to clear history: ${e.message}"
            }
        }
    }
}
