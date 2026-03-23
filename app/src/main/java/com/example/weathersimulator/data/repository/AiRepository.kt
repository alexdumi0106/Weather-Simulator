package com.example.weathersimulator.repository

interface AiRepository {
    suspend fun generate(prompt: String, serverUrl: String): String
}
