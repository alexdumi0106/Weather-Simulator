package com.example.weathersimulator.domain.usecase

import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        repository.loginUser(email, password)
}

