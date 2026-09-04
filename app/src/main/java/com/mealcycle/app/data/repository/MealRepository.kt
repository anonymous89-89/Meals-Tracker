package com.mealcycle.app.data.repository

import com.mealcycle.app.data.db.MealEntryDao
import com.mealcycle.app.data.db.MonthlyMealCount
import com.mealcycle.app.data.model.MealEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val mealEntryDao: MealEntryDao
) {
    fun getDeliveredMeals(userId: String): Flow<List<MealEntry>> =
        mealEntryDao.getDeliveredMealsForUser(userId)

    fun getMealsForDate(userId: String, date: String): Flow<List<MealEntry>> =
        mealEntryDao.getMealsForDate(userId, date)

    fun getAllMealsForUser(userId: String): Flow<List<MealEntry>> =
        mealEntryDao.getAllMealsForUser(userId)

    fun getDeliveredMealCount(userId: String): Flow<Int> =
        mealEntryDao.getDeliveredMealCount(userId)

    fun getMonthlyMealCounts(userId: String): Flow<List<MonthlyMealCount>> =
        mealEntryDao.getMonthlyMealCounts(userId)

    suspend fun toggleMeal(
        userId: String,
        date: String,
        mealType: String,
        isCurrentlyDelivered: Boolean,
        currentDeliveredCount: Int,
        totalMeals: Int = 90
    ) {
        if (!isCurrentlyDelivered) {
            if (currentDeliveredCount < totalMeals) {
                mealEntryDao.upsertMealEntry(
                    MealEntry(userId = userId, date = date, mealType = mealType, isDelivered = true)
                )
            }
        } else {
            mealEntryDao.deleteMealEntry(userId, date, mealType)
        }
    }

    suspend fun getEarliestDeliveredDate(userId: String): String? =
        mealEntryDao.getEarliestDeliveredDate(userId)

    suspend fun deleteAllMealsForUser(userId: String) =
        mealEntryDao.deleteAllMealsForUser(userId)
}
