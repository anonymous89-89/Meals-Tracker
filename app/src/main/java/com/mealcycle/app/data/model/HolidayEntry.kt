package com.mealcycle.app.data.model

import androidx.room.*

/**
 * Represents a date marked as a holiday for a given user.
 * Holidays are purely a visual marker — meal toggles still work on holiday dates.
 * Supports future dates so users can plan ahead (e.g. mark a vacation week).
 */
@Entity(
    tableName = "holidays",
    indices = [Index(value = ["userId", "date"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class HolidayEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val date: String   // ISO format yyyy-MM-dd, may be future
)
