package com.mealcycle.app.data.repository

import com.mealcycle.app.data.db.PlanHistoryDao
import com.mealcycle.app.data.model.PlanHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for reading and writing plan history records.
 */
@Singleton
class HistoryRepository @Inject constructor(
    private val planHistoryDao: PlanHistoryDao
) {
    /** All plan records for a user, newest first. */
    fun getPlanHistory(userId: String): Flow<List<PlanHistory>> =
        planHistoryDao.getPlanHistoryForUser(userId)

    /** Total rupee amount spent across all plans. */
    fun getTotalAmountSpent(userId: String): Flow<Int?> =
        planHistoryDao.getTotalAmountSpentForUser(userId)

    /** Insert a new completed plan record. */
    suspend fun insertPlanHistory(planHistory: PlanHistory) =
        planHistoryDao.insertPlanHistory(planHistory)

    /** Delete all plan history for a user (when the user profile is deleted). */
    suspend fun deleteAllHistoryForUser(userId: String) =
        planHistoryDao.deleteAllHistoryForUser(userId)
}
