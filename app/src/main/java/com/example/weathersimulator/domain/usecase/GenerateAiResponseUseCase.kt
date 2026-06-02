package com.example.weathersimulator.domain.usecase

import com.example.weathersimulator.data.remote.ai.SkyObservationRequest
import com.example.weathersimulator.repository.AiRepository
import com.example.weathersimulator.data.remote.ai.OutfitRecommendationRequest
import com.example.weathersimulator.data.remote.ai.WeatherSimulationRequest
import com.example.weathersimulator.data.remote.ai.WeatherStoryRequest
import com.example.weathersimulator.data.remote.ai.HistoricalDayDescriptionRequest
import com.example.weathersimulator.data.remote.ai.ClimateComparisonRequest
import javax.inject.Inject

class GenerateAiResponseUseCase @Inject constructor(
    private val repo: AiRepository
) {
    suspend operator fun invoke(prompt: String, serverUrl: String): String =
        repo.generate(prompt, serverUrl)

    suspend fun local(prompt: String, serverUrl: String): String =
        repo.generateLocal(prompt, serverUrl)

    suspend fun skyObservation(request: SkyObservationRequest, serverUrl: String): String =
        repo.generateSkyObservation(request, serverUrl)

    suspend fun outfitRecommendation(request: OutfitRecommendationRequest, serverUrl: String): String =
        repo.generateOutfitRecommendation(request, serverUrl)

    suspend fun weatherSimulation(request: WeatherSimulationRequest, serverUrl: String): String =
        repo.generateWeatherSimulation(request, serverUrl)

    suspend fun weatherStory(request: WeatherStoryRequest, serverUrl: String): String =
        repo.generateWeatherStory(request, serverUrl)

    suspend fun historicalDayDescription(request: HistoricalDayDescriptionRequest, serverUrl: String): String =
        repo.generateHistoricalDayDescription(request, serverUrl)

    suspend fun climateComparison(request: ClimateComparisonRequest, serverUrl: String): String =
        repo.generateClimateComparison(request, serverUrl)
}
