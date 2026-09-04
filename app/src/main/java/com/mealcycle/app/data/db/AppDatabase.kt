package com.mealcycle.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mealcycle.app.data.model.HolidayEntry
import com.mealcycle.app.data.model.MealEntry
import com.mealcycle.app.data.model.PlanHistory
import com.mealcycle.app.data.model.User

/**
 * Version 3: Added holidays table (HolidayEntry entity).
 * Still using fallbackToDestructiveMigration() since no live production data.
 */
@Database(
    entities = [MealEntry::class, PlanHistory::class, User::class, HolidayEntry::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun planHistoryDao(): PlanHistoryDao
    abstract fun userDao(): UserDao
    abstract fun holidayDao(): HolidayDao
}
