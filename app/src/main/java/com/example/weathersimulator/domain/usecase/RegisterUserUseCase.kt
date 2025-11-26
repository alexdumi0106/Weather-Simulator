package com.example.weathersimulator.domain.usecase

import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        repository.registerUser(email, password)
}
