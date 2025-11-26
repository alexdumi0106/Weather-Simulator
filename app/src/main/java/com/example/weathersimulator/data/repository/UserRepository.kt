package com.example.weathersimulator.data.repository

import com.example.weathersimulator.core.data.local.user.UserDao
import com.example.weathersimulator.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val userDao: UserDao
) {

    suspend fun registerUser(email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("User ID null"))

            val newUser = User(
                id = userId,
                email = email,
                name = "",
                preferences = emptyMap()
            )

            userDao.insertUser(UserEntity.fromDomain(newUser))

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("User ID null"))

            val localUser = userDao.getUser(uid)

            if (localUser != null) {
                Result.success(localUser.toDomain())
            } else {
                Result.success(
                    User(
                        id = uid,
                        email = email,
                        name = "",
                        preferences = emptyMap()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
