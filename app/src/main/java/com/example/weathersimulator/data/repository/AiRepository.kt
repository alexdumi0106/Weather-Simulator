package com.example.weathersimulator.repository

import com.example.weathersimulator.data.remote.ai.SkyObservationRequest
import com.example.weathersimulator.data.remote.ai.OutfitRecommendationRequest
import com.example.weathersimulator.data.remote.ai.WeatherSimulationRequest
import com.example.weathersimulator.data.remote.ai.WeatherStoryRequest
import com.example.weathersimulator.data.remote.ai.HistoricalDayDescriptionRequest
import com.example.weathersimulator.data.remote.ai.ClimateComparisonRequest

interface AiRepository {
    suspend fun generate(prompt: String, serverUrl: String): String

    suspend fun generateLocal(prompt: String, serverUrl: String): String
    
    suspend fun generateSkyObservation(request: SkyObservationRequest, serverUrl: String): String

    suspend fun generateOutfitRecommendation(request: OutfitRecommendationRequest, serverUrl: String): String

    suspend fun generateWeatherSimulation(request: WeatherSimulationRequest, serverUrl: String): String

    suspend fun generateWeatherStory(request: WeatherStoryRequest, serverUrl: String): String

    suspend fun generateHistoricalDayDescription(request: HistoricalDayDescriptionRequest, serverUrl: String): String

    suspend fun generateClimateComparison(request: ClimateComparisonRequest, serverUrl: String): String
}
