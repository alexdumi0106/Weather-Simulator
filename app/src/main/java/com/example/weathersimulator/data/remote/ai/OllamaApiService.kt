package com.example.weathersimulator.data.remote.ai

import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaApiService {
    @POST("generate")
    suspend fun generate(
        @Body body: OllamaRequest
    ): OllamaResponse

    @POST("generate-local")
    suspend fun generateLocal(
        @Body body: OllamaRequest
    ): OllamaResponse

    @POST("ai/sky-observation")
    suspend fun generateSkyObservation(
        @Body body: SkyObservationRequest
    ): OllamaResponse

    @POST("ai/outfit-recommendation")
    suspend fun generateOutfitRecommendation(
        @Body body: OutfitRecommendationRequest
    ): OllamaResponse

    @POST("ai/weather-simulation")
    suspend fun generateWeatherSimulation(
        @Body body: WeatherSimulationRequest
    ): OllamaResponse

    @POST("ai/weather-story")
    suspend fun generateWeatherStory(
        @Body body: WeatherStoryRequest
    ): OllamaResponse

    @POST("ai/historical-day-description")
    suspend fun generateHistoricalDayDescription(
        @Body body: HistoricalDayDescriptionRequest
    ): OllamaResponse

    @POST("ai/climate-comparison")
    suspend fun generateClimateComparison(
        @Body body: ClimateComparisonRequest
    ): OllamaResponse
}
