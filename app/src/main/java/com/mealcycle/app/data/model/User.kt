package com.mealcycle.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user profile. Multiple users can share the same device.
 * userId is a UUID string generated on user creation.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,  // UUID
    val name: String,
    val photoUri: String?            // URI from photo picker; null if no photo chosen
)
