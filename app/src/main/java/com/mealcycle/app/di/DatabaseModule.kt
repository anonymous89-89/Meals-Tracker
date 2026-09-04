package com.mealcycle.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mealcycle.app.data.db.AppDatabase
import com.mealcycle.app.data.db.HolidayDao
import com.mealcycle.app.data.db.MealEntryDao
import com.mealcycle.app.data.db.PlanHistoryDao
import com.mealcycle.app.data.db.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Migration 2 → 3: Add the holidays table.
     * This preserves all existing meal, user, and plan history data.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `holidays` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `date` TEXT NOT NULL,
                    FOREIGN KEY(`userId`) REFERENCES `users`(`userId`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_holidays_userId_date` ON `holidays` (`userId`, `date`)")
        }
    }

    /**
     * Migration 1 → 3: For fresh installs that skipped version 2.
     * Creates all tables from v1 schema + adds holidays table.
     */
    private val MIGRATION_1_3 = object : Migration(1, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add holidays table (other tables already exist from v1)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `holidays` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `date` TEXT NOT NULL,
                    FOREIGN KEY(`userId`) REFERENCES `users`(`userId`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_holidays_userId_date` ON `holidays` (`userId`, `date`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "meal_cycle_db"
        )
            .addMigrations(MIGRATION_1_3, MIGRATION_2_3)
            // ⚠️ NO fallbackToDestructiveMigration() — data must never be silently wiped
            .enableMultiInstanceInvalidation()
            .build()

    @Provides fun provideMealEntryDao(db: AppDatabase): MealEntryDao = db.mealEntryDao()

    @Provides fun providePlanHistoryDao(db: AppDatabase): PlanHistoryDao = db.planHistoryDao()

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides fun provideHolidayDao(db: AppDatabase): HolidayDao = db.holidayDao()
}
