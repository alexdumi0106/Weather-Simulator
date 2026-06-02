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

    override suspend fun generateLocal(prompt: String, serverUrl: String): String {
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

        return api.generateLocal(OllamaRequest(prompt)).response
    }

    override suspend fun generateSkyObservation(
        request: SkyObservationRequest,
        serverUrl: String
    ): String {
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

        return api.generateSkyObservation(request).response
    }

    override suspend fun generateOutfitRecommendation(
        request: OutfitRecommendationRequest,
        serverUrl: String
    ): String {
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

        return api.generateOutfitRecommendation(request).response
    }

    override suspend fun generateWeatherSimulation(
        request: WeatherSimulationRequest,
        serverUrl: String
    ): String {
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

        return api.generateWeatherSimulation(request).response
    }

    override suspend fun generateWeatherStory(
        request: WeatherStoryRequest,
        serverUrl: String
    ): String {
        val client = buildClient()
        val api = buildApi(serverUrl, client)
        return api.generateWeatherStory(request).response
    }

    override suspend fun generateHistoricalDayDescription(
        request: HistoricalDayDescriptionRequest,
        serverUrl: String
    ): String {
        val client = buildClient()
        val api = buildApi(serverUrl, client)
        return api.generateHistoricalDayDescription(request).response
    }

    override suspend fun generateClimateComparison(
        request: ClimateComparisonRequest,
        serverUrl: String
    ): String {
        val client = buildClient()
        val api = buildApi(serverUrl, client)
        return api.generateClimateComparison(request).response
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(1000, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(1000, TimeUnit.SECONDS)
            .build()
    }

    private fun buildApi(serverUrl: String, client: OkHttpClient): OllamaApiService {
        return Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)
    }
}
