package com.mealcycle.app.data.repository

import com.mealcycle.app.data.db.HolidayDao
import com.mealcycle.app.data.db.MonthlyHolidayCount
import com.mealcycle.app.data.model.HolidayEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayRepository @Inject constructor(
    private val holidayDao: HolidayDao
) {
    fun getAllHolidays(userId: String): Flow<List<HolidayEntry>> =
        holidayDao.getAllHolidaysForUser(userId)

    fun getMonthlyHolidayCounts(userId: String): Flow<List<MonthlyHolidayCount>> =
        holidayDao.getMonthlyHolidayCounts(userId)

    suspend fun markHoliday(userId: String, date: String) {
        holidayDao.markHoliday(HolidayEntry(userId = userId, date = date))
    }

    suspend fun unmarkHoliday(userId: String, date: String) {
        holidayDao.unmarkHoliday(userId, date)
    }

    suspend fun deleteAllForUser(userId: String) {
        holidayDao.deleteAllForUser(userId)
    }
}
