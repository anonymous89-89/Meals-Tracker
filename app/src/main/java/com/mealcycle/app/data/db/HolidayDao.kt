package com.mealcycle.app.data.db

import androidx.room.*
import com.mealcycle.app.data.model.HolidayEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markHoliday(entry: HolidayEntry)

    @Query("DELETE FROM holidays WHERE userId = :userId AND date = :date")
    suspend fun unmarkHoliday(userId: String, date: String)

    @Query("SELECT * FROM holidays WHERE userId = :userId ORDER BY date ASC")
    fun getAllHolidaysForUser(userId: String): Flow<List<HolidayEntry>>

    @Query("SELECT COUNT(*) > 0 FROM holidays WHERE userId = :userId AND date = :date")
    suspend fun isHoliday(userId: String, date: String): Boolean

    @Query("DELETE FROM holidays WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    /**
     * Group holidays by calendar month for the Usage Statistics screen.
     * Returns rows like {month: "2026-04", count: 2}.
     */
    @Query("""
        SELECT strftime('%Y-%m', date) AS month, COUNT(*) AS count
        FROM holidays
        WHERE userId = :userId
        GROUP BY month
        ORDER BY month ASC
    """)
    fun getMonthlyHolidayCounts(userId: String): Flow<List<MonthlyHolidayCount>>
}

/** Aggregated monthly holiday count — used by the Usage Statistics screen */
data class MonthlyHolidayCount(
    val month: String,  // "yyyy-MM" e.g. "2026-04"
    val count: Int
)
