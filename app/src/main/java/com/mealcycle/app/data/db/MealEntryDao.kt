package com.mealcycle.app.data.db

import androidx.room.*
import com.mealcycle.app.data.model.MealEntry
import kotlinx.coroutines.flow.Flow

/** Aggregated monthly meal count — used by the Usage Statistics screen */
data class MonthlyMealCount(
    val month: String,  // "yyyy-MM" e.g. "2024-04"
    val count: Int
)

/**
 * DAO for meal_entries table.
 * All queries are scoped to a specific userId to support multi-user profiles.
 *
 * FIX #8: Renamed insertMealEntry → upsertMealEntry to be explicit about REPLACE strategy.
 *         The unique index on (userId, date, mealType) ensures no duplicates on rapid tapping.
 */
@Dao
interface MealEntryDao {

    /**
     * Upsert a meal entry (insert or replace on unique constraint conflict).
     * The unique index (userId, date, mealType) ensures idempotency.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMealEntry(mealEntry: MealEntry)

    /**
     * Delete a specific meal entry by userId, date, and mealType.
     * Used when toggling a meal off.
     */
    @Query("DELETE FROM meal_entries WHERE userId = :userId AND date = :date AND mealType = :mealType")
    suspend fun deleteMealEntry(userId: String, date: String, mealType: String)

    /**
     * Get all delivered meal entries for a specific user (entire current cycle).
     * Used for cycle-level calculations (total delivered, remaining, etc.)
     */
    @Query("SELECT * FROM meal_entries WHERE userId = :userId AND isDelivered = 1")
    fun getDeliveredMealsForUser(userId: String): Flow<List<MealEntry>>

    /**
     * Get all meal entries for a specific date and user.
     * Used to populate the meal toggle buttons on the home screen.
     */
    @Query("SELECT * FROM meal_entries WHERE userId = :userId AND date = :date")
    fun getMealsForDate(userId: String, date: String): Flow<List<MealEntry>>

    /**
     * Get all meal entries for a given user (for calendar indicators).
     */
    @Query("SELECT * FROM meal_entries WHERE userId = :userId")
    fun getAllMealsForUser(userId: String): Flow<List<MealEntry>>

    /**
     * Count of delivered meals for a user — used for cycle cap enforcement.
     */
    @Query("SELECT COUNT(*) FROM meal_entries WHERE userId = :userId AND isDelivered = 1")
    fun getDeliveredMealCount(userId: String): Flow<Int>

    /**
     * Get the earliest delivered meal date for a user (plan start date).
     */
    @Query("SELECT MIN(date) FROM meal_entries WHERE userId = :userId AND isDelivered = 1")
    suspend fun getEarliestDeliveredDate(userId: String): String?

    /**
     * Group delivered meals by calendar month for the Usage Statistics screen.
     * Returns rows like {month: "2024-04", count: 60}.
     */
    @Query("""
        SELECT strftime('%Y-%m', date) AS month, COUNT(*) AS count
        FROM meal_entries
        WHERE userId = :userId AND isDelivered = 1
        GROUP BY month
        ORDER BY month ASC
    """)
    fun getMonthlyMealCounts(userId: String): Flow<List<MonthlyMealCount>>

    /**
     * Get delivered meals within a date range for export.
     */
    @Query("SELECT * FROM meal_entries WHERE userId = :userId AND isDelivered = 1 AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getMealsInDateRange(userId: String, startDate: String, endDate: String): List<MealEntry>

    /**
     * Delete all meal entries for a user (used on Reset Plan / End Plan).
     */
    @Query("DELETE FROM meal_entries WHERE userId = :userId")
    suspend fun deleteAllMealsForUser(userId: String)
}
