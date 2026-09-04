package com.mealcycle.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records a completed meal plan cycle.
 * Inserted when user confirms "End Plan" — then MealEntry table is cleared for current user.
 *
 * ForeignKey with CASCADE DELETE: if the user profile is deleted, all their history is deleted too.
 */
@Entity(
    tableName = "plan_history",
    indices = [Index(value = ["userId"])],
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PlanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val startDate: String,      // "2025-01-01" — ISO_LOCAL_DATE, earliest delivered meal date
    val endDate: String,        // "2025-04-27" — ISO_LOCAL_DATE, date End Plan was confirmed
    val deliveredMeals: Int,
    val amountSpent: Int,       // deliveredMeals * 50  — pure Int arithmetic, no floats
    val refundAmount: Int       // (90 - deliveredMeals) * 50
)
