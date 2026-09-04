package com.mealcycle.app.data.db

import androidx.room.*
import com.mealcycle.app.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the users table.
 */
@Dao
interface UserDao {

    /**
     * Insert or update a user profile.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * Get all users (shown in profile picker / switcher).
     */
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    /**
     * Get a single user by their userId.
     */
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): User?

    /**
     * Delete a user profile.
     */
    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
}
