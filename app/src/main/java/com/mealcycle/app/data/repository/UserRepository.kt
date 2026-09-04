package com.mealcycle.app.data.repository

import com.mealcycle.app.data.db.UserDao
import com.mealcycle.app.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user profile management.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    /** Stream of all user profiles sorted alphabetically. */
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    /** One-shot fetch of a single user by ID. */
    suspend fun getUserById(userId: String): User? = userDao.getUserById(userId)

    /** Create or update a user profile. */
    suspend fun insertUser(user: User) = userDao.insertUser(user)

    /** Remove a user profile permanently. */
    suspend fun deleteUser(userId: String) = userDao.deleteUser(userId)
}
