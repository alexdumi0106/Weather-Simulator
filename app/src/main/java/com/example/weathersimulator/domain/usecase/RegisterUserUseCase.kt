package com.example.weathersimulator.domain.usecase

import javax.inject.Inject
import com.example.weathersimulator.data.repository.UserRepository
import com.example.weathersimulator.domain.model.User


class RegisterUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<User> =
        repository.registerUser(email, password, firstName, lastName)
}

