package com.mealcycle.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single meal slot for a given user, date, and meal type.
 * isDelivered = true only when the user manually toggles the meal as received.
 *
 * FIXES:
 * 1. Unique composite index on (userId, date, mealType) — prevents duplicate rows on rapid tapping.
 * 2. ForeignKey to User with CASCADE DELETE — auto-deletes meals when user is deleted.
 */
@Entity(
    tableName = "meal_entries",
    indices = [Index(value = ["userId", "date", "mealType"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MealEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val date: String,         // ISO format: "2025-04-27" — always DateTimeFormatter.ISO_LOCAL_DATE
    val mealType: String,     // "BREAKFAST" | "LUNCH" | "DINNER"
    val isDelivered: Boolean
)

/** Enum for type-safe meal references throughout the app. */
enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner")
}
