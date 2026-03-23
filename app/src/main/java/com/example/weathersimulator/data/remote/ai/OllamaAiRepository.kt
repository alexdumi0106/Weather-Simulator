package com.example.weathersimulator.data.remote.ai

import com.example.weathersimulator.repository.AiRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OllamaAiRepository @Inject constructor() : AiRepository {

    override suspend fun generate(prompt: String, serverUrl: String): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(1000, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(1000, TimeUnit.SECONDS)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)

        return api.generate(OllamaRequest(prompt)).response
    }
}
