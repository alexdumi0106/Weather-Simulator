package com.example.weathersimulator.data.remote.ai

import com.example.weathersimulator.repository.AiRepository
import javax.inject.Inject

class OllamaAiRepository @Inject constructor(
    private val api: OllamaApiService
) : AiRepository {

    override suspend fun generate(prompt: String): String {
        val res = api.generate(
            OllamaRequest(
                model = "llama3.2:1b",   // se poate schimba ulterior
                prompt = prompt,
                stream = false
            )
        )
        return res.response?.trim().orEmpty()
    }
}
