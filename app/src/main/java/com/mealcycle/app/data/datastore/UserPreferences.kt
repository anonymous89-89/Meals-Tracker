package com.mealcycle.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mealcycle.app.utils.MealCalculations
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** DataStore delegate — created once per application. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Manages persistent user preferences in Jetpack DataStore.
 * Injected as a Singleton via Hilt.
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ACTIVE_USER_ID_KEY  = stringPreferencesKey("active_user_id")
        private val TOTAL_MEALS_KEY     = intPreferencesKey("total_meals")
        private val PRICE_PER_MEAL_KEY  = intPreferencesKey("price_per_meal")
        private val THEME_MODE_KEY      = stringPreferencesKey("theme_mode")
        private val FONT_COLOR_KEY      = stringPreferencesKey("font_color_preset")
        private val THEME_SOURCE_KEY    = stringPreferencesKey("theme_source")
        private val CUSTOM_IMAGE_URI_KEY = stringPreferencesKey("custom_image_uri")
        private val BLUR_INTENSITY_KEY  = floatPreferencesKey("blur_intensity")
        private val GRADIENT_INTENSITY_KEY = floatPreferencesKey("gradient_intensity")
    }

    // ─── Active user ───────────────────────────────────────────────────────────

    /** Emits the currently active userId, or null if none is set. */
    val activeUserId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACTIVE_USER_ID_KEY]
    }

    suspend fun setActiveUserId(userId: String) {
        context.dataStore.edit { prefs -> prefs[ACTIVE_USER_ID_KEY] = userId }
    }

    suspend fun clearActiveUserId() {
        context.dataStore.edit { prefs -> prefs.remove(ACTIVE_USER_ID_KEY) }
    }

    // ─── Cycle settings ────────────────────────────────────────────────────────

    /** Total meals in one cycle (default 90, min 3, max 999). */
    val totalMeals: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_MEALS_KEY] ?: MealCalculations.DEFAULT_TOTAL_MEALS
    }

    suspend fun setTotalMeals(value: Int) {
        val clamped = value.coerceIn(3, 999)
        context.dataStore.edit { prefs -> prefs[TOTAL_MEALS_KEY] = clamped }
    }

    /** Price per meal in rupees (default ₹50, min ₹1, max ₹9999). */
    val pricePerMeal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PRICE_PER_MEAL_KEY] ?: MealCalculations.DEFAULT_PRICE_PER_MEAL
    }

    suspend fun setPricePerMeal(value: Int) {
        val clamped = value.coerceIn(1, 9999)
        context.dataStore.edit { prefs -> prefs[PRICE_PER_MEAL_KEY] = clamped }
    }

    // ─── Theme mode ──────────────────────────────────────────────────────────

    /**
     * Theme mode: "auto" (follow system), "light", or "dark".
     * Default is "auto" — auto-detects device dark mode setting.
     * When user manually switches, their choice persists until changed again.
     */
    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY] ?: "auto"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[THEME_MODE_KEY] = mode }
    }

    // ─── Font color preset (dark mode) ─────────────────────────────────────────

    /**
     * Font color preset for dark mode: "default", "soft_white", "light_gray", "light_blue", "light_cyan".
     */
    val fontColorPreset: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FONT_COLOR_KEY] ?: "default"
    }

    suspend fun setFontColorPreset(preset: String) {
        context.dataStore.edit { prefs -> prefs[FONT_COLOR_KEY] = preset }
    }

    // ─── Adaptive theming ──────────────────────────────────────────────────────

    val themeSource: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_SOURCE_KEY] ?: "default"
    }
    suspend fun setThemeSource(source: String) {
        context.dataStore.edit { prefs -> prefs[THEME_SOURCE_KEY] = source }
    }

    val customImageUri: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_IMAGE_URI_KEY] ?: ""
    }
    suspend fun setCustomImageUri(uri: String) {
        context.dataStore.edit { prefs -> prefs[CUSTOM_IMAGE_URI_KEY] = uri }
    }

    val blurIntensity: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[BLUR_INTENSITY_KEY] ?: 0.6f
    }
    suspend fun setBlurIntensity(value: Float) {
        context.dataStore.edit { prefs -> prefs[BLUR_INTENSITY_KEY] = value }
    }

    val gradientIntensity: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[GRADIENT_INTENSITY_KEY] ?: 0.7f
    }
    suspend fun setGradientIntensity(value: Float) {
        context.dataStore.edit { prefs -> prefs[GRADIENT_INTENSITY_KEY] = value }
    }
}
