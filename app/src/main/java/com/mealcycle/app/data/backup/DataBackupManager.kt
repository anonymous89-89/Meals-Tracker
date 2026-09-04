package com.mealcycle.app.data.backup

import android.content.Context
import android.net.Uri
import com.mealcycle.app.data.db.AppDatabase
import com.mealcycle.app.data.model.HolidayEntry
import com.mealcycle.app.data.model.MealEntry
import com.mealcycle.app.data.model.PlanHistory
import com.mealcycle.app.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles exporting & importing all app data as a JSON file.
 * Used for backup/restore so users never lose meal tracking data.
 *
 * JSON structure:
 * {
 *   "version": 1,
 *   "exportDate": "2025-05-08",
 *   "users": [...],
 *   "meals": [...],
 *   "holidays": [...],
 *   "planHistory": [...]
 * }
 */
@Singleton
class DataBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {
    companion object {
        private const val BACKUP_VERSION = 1
        private const val KEY_VERSION = "version"
        private const val KEY_EXPORT_DATE = "exportDate"
        private const val KEY_USERS = "users"
        private const val KEY_MEALS = "meals"
        private const val KEY_HOLIDAYS = "holidays"
        private const val KEY_PLAN_HISTORY = "planHistory"
    }

    // ─── Export ───────────────────────────────────────────────────────────────

    /**
     * Export all data to the given URI (user picks a file via SAF).
     * Returns the number of total records exported.
     */
    suspend fun exportToUri(uri: Uri): Int = withContext(Dispatchers.IO) {
        val users = db.userDao().getAllUsers().first()
        val json = JSONObject().apply {
            put(KEY_VERSION, BACKUP_VERSION)
            put(KEY_EXPORT_DATE, java.time.LocalDate.now().toString())
            put(KEY_USERS, usersToJson(users))
        }

        var totalRecords = users.size

        // Collect all meals, holidays, and plan history across all users
        val allMeals = JSONArray()
        val allHolidays = JSONArray()
        val allHistory = JSONArray()

        for (user in users) {
            val meals = db.mealEntryDao().getAllMealsForUser(user.userId).first()
            meals.forEach { meal ->
                allMeals.put(mealToJson(meal))
                totalRecords++
            }

            val holidays = db.holidayDao().getAllHolidaysForUser(user.userId).first()
            holidays.forEach { holiday ->
                allHolidays.put(holidayToJson(holiday))
                totalRecords++
            }

            val history = db.planHistoryDao().getPlanHistoryForUser(user.userId).first()
            history.forEach { plan ->
                allHistory.put(planHistoryToJson(plan))
                totalRecords++
            }
        }

        json.put(KEY_MEALS, allMeals)
        json.put(KEY_HOLIDAYS, allHolidays)
        json.put(KEY_PLAN_HISTORY, allHistory)

        // Write to file
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.toString(2).toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("Cannot open output stream for URI: $uri")

        totalRecords
    }

    // ─── Import ──────────────────────────────────────────────────────────────

    /**
     * Import data from the given URI. Merges with existing data
     * (uses REPLACE strategy so duplicates are overwritten).
     * Returns the number of records imported.
     */
    suspend fun importFromUri(uri: Uri): Int = withContext(Dispatchers.IO) {
        val jsonStr = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: throw IllegalStateException("Cannot open input stream for URI: $uri")

        val json = JSONObject(jsonStr)
        val version = json.optInt(KEY_VERSION, 1)
        if (version > BACKUP_VERSION) {
            throw IllegalArgumentException("Backup version $version is newer than supported ($BACKUP_VERSION)")
        }

        var importedCount = 0

        // Import users
        val usersArray = json.optJSONArray(KEY_USERS) ?: JSONArray()
        for (i in 0 until usersArray.length()) {
            val user = jsonToUser(usersArray.getJSONObject(i))
            db.userDao().insertUser(user)
            importedCount++
        }

        // Import meals
        val mealsArray = json.optJSONArray(KEY_MEALS) ?: JSONArray()
        for (i in 0 until mealsArray.length()) {
            val meal = jsonToMeal(mealsArray.getJSONObject(i))
            db.mealEntryDao().upsertMealEntry(meal)
            importedCount++
        }

        // Import holidays
        val holidaysArray = json.optJSONArray(KEY_HOLIDAYS) ?: JSONArray()
        for (i in 0 until holidaysArray.length()) {
            val holiday = jsonToHoliday(holidaysArray.getJSONObject(i))
            db.holidayDao().markHoliday(holiday)
            importedCount++
        }

        // Import plan history
        val historyArray = json.optJSONArray(KEY_PLAN_HISTORY) ?: JSONArray()
        for (i in 0 until historyArray.length()) {
            val plan = jsonToPlanHistory(historyArray.getJSONObject(i))
            db.planHistoryDao().insertPlanHistory(plan)
            importedCount++
        }

        importedCount
    }

    // ─── JSON Serializers ────────────────────────────────────────────────────

    private fun usersToJson(users: List<User>): JSONArray {
        val arr = JSONArray()
        users.forEach { arr.put(userToJson(it)) }
        return arr
    }

    private fun userToJson(user: User) = JSONObject().apply {
        put("userId", user.userId)
        put("name", user.name)
        put("photoUri", user.photoUri ?: JSONObject.NULL)
    }

    private fun jsonToUser(obj: JSONObject) = User(
        userId = obj.getString("userId"),
        name = obj.getString("name"),
        photoUri = if (obj.isNull("photoUri")) null else obj.getString("photoUri")
    )

    private fun mealToJson(meal: MealEntry) = JSONObject().apply {
        put("userId", meal.userId)
        put("date", meal.date)
        put("mealType", meal.mealType)
        put("isDelivered", meal.isDelivered)
    }

    private fun jsonToMeal(obj: JSONObject) = MealEntry(
        userId = obj.getString("userId"),
        date = obj.getString("date"),
        mealType = obj.getString("mealType"),
        isDelivered = obj.getBoolean("isDelivered")
    )

    private fun holidayToJson(holiday: HolidayEntry) = JSONObject().apply {
        put("userId", holiday.userId)
        put("date", holiday.date)
    }

    private fun jsonToHoliday(obj: JSONObject) = HolidayEntry(
        userId = obj.getString("userId"),
        date = obj.getString("date")
    )

    private fun planHistoryToJson(plan: PlanHistory) = JSONObject().apply {
        put("userId", plan.userId)
        put("startDate", plan.startDate)
        put("endDate", plan.endDate)
        put("deliveredMeals", plan.deliveredMeals)
        put("amountSpent", plan.amountSpent)
        put("refundAmount", plan.refundAmount)
    }

    private fun jsonToPlanHistory(obj: JSONObject) = PlanHistory(
        userId = obj.getString("userId"),
        startDate = obj.getString("startDate"),
        endDate = obj.getString("endDate"),
        deliveredMeals = obj.getInt("deliveredMeals"),
        amountSpent = obj.getInt("amountSpent"),
        refundAmount = obj.getInt("refundAmount")
    )
}
