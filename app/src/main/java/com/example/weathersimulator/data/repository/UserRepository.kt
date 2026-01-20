package com.example.weathersimulator.data.repository

import com.example.weathersimulator.data.local.user.UserDao
import com.example.weathersimulator.data.local.user.UserEntity
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
            val userId = result.user?.uid
                ?: return Result.failure(Exception("User ID null"))

            val newUser = User(
                id = userId,
                email = email,
                name = "",
                preferences = emptyMap()
            )

            userDao.insertUser(
                UserEntity(
                    uid = newUser.id,
                    email = newUser.email,
                    name = newUser.name
                )
            )

            Result.success(newUser)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
                ?: return Result.failure(Exception("User ID null"))

            val localUser = userDao.getUserById(uid)

            if (localUser != null) {
                Result.success(
                    User(
                        id = localUser.uid,
                        email = localUser.email,
                        name = localUser.name,
                        preferences = emptyMap()
                    )
                )
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
