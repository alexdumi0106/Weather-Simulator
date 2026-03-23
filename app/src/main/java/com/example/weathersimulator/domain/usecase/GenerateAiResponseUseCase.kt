package com.example.weathersimulator.domain.usecase

import com.example.weathersimulator.repository.AiRepository
import javax.inject.Inject

class GenerateAiResponseUseCase @Inject constructor(
    private val repo: AiRepository
) {
    suspend operator fun invoke(prompt: String, serverUrl: String): String =
        repo.generate(prompt, serverUrl)
}
