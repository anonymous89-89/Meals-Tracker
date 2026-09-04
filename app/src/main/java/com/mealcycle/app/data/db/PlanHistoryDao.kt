package com.mealcycle.app.data.db

import androidx.room.*
import com.mealcycle.app.data.model.PlanHistory
import kotlinx.coroutines.flow.Flow

/**
 * DAO for plan_history table.
 * Records are inserted when a user ends a plan cycle.
 */
@Dao
interface PlanHistoryDao {

    /**
     * Insert a completed plan record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanHistory(planHistory: PlanHistory)

    /**
     * Get all plan history records for a user, ordered by most recent first.
     */
    @Query("SELECT * FROM plan_history WHERE userId = :userId ORDER BY endDate DESC")
    fun getPlanHistoryForUser(userId: String): Flow<List<PlanHistory>>

    /**
     * Get the total amount spent across all plans for a user.
     */
    @Query("SELECT SUM(amountSpent) FROM plan_history WHERE userId = :userId")
    fun getTotalAmountSpentForUser(userId: String): Flow<Int?>

    /**
     * Delete all history for a user (used if user account is deleted).
     */
    @Query("DELETE FROM plan_history WHERE userId = :userId")
    suspend fun deleteAllHistoryForUser(userId: String)
}
