package com.mealcycle.app.data.export

import android.content.Context
import android.net.Uri
import com.mealcycle.app.data.db.MealEntryDao
import com.mealcycle.app.data.model.MealEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports meal data as JSON or PDF with date range filtering.
 */
@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mealEntryDao: MealEntryDao
) {
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Query meals in the given date range, grouped by date.
     */
    suspend fun getMealsGroupedByDate(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<String, List<MealEntry>> {
        val meals = mealEntryDao.getMealsInDateRange(
            userId,
            startDate.format(dateFormat),
            endDate.format(dateFormat)
        )
        return meals.groupBy { it.date }
    }

    /**
     * Export to JSON and write to the given URI.
     * Returns the number of days exported.
     *
     * Schema:
     * {
     *   "exportDate": "2026-05-12",
     *   "user": "Me",
     *   "dateRange": { "from": "2026-01-01", "to": "2026-05-12" },
     *   "meals": [
     *     { "date": "...", "particulars": "...", "mealCount": 3, "mealTypes": [...] }
     *   ]
     * }
     */
    suspend fun exportToJson(
        uri: Uri,
        userId: String,
        userName: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Int {
        val grouped = getMealsGroupedByDate(userId, startDate, endDate)

        val root = JSONObject().apply {
            put("exportDate", LocalDate.now().format(dateFormat))
            put("user", userName)
            put("dateRange", JSONObject().apply {
                put("from", startDate.format(dateFormat))
                put("to", endDate.format(dateFormat))
            })

            val mealsArray = JSONArray()
            grouped.entries.sortedBy { it.key }.forEach { (date, entries) ->
                mealsArray.put(JSONObject().apply {
                    put("date", date)
                    put("particulars", if (entries.size == 3) "Full Day" else "Partial Day")
                    put("mealCount", entries.size)
                    put("mealTypes", JSONArray(entries.map { it.mealType }))
                })
            }
            put("meals", mealsArray)
        }

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(root.toString(2).toByteArray())
        }

        return grouped.size
    }
}
