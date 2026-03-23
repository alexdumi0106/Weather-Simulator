package com.example.weathersimulator.data.remote.ai

import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaApiService {
    @POST("generate")
    suspend fun generate(
        @Body body: OllamaRequest
    ): OllamaResponse
}
