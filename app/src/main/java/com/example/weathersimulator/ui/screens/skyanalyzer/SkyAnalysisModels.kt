package com.example.weathersimulator.ui.screens.skyanalyzer

import android.graphics.Bitmap

data class SkyAnalyzerUiState(
    val photo: Bitmap? = null,
    val photoSource: SkyPhotoSource? = null,
    val result: SkyAnalysisResult? = null,
    val isAnalyzing: Boolean = false,
    val isGeneratingAiObservation: Boolean = false,
    val aiObservationError: String? = null,
    val permissionMessage: String? = null
)

enum class SkyPhotoSource {
    Camera,
    Gallery
}

data class SkyAnalysisResult(
    val cloudType: String,
    val rainProbability: Int,
    val stormProbability: Int,
    val photographyScore: Int,
    val bestMoment: String,
    val sunsetScore: Int,
    val sunriseScore: Int,
    val stormScore: Int,
    val dramaticCloudsScore: Int,
    val fogScore: Int,
    val shortAdvice: String,
    val aiObservation: String = "",
    val skyStory: String = "",
    val metrics: SkyMetrics
)

data class SkyMetrics(
    val skyRatio: Float,
    val cloudRatio: Float,
    val darkCloudRatio: Float,
    val warmLightRatio: Float,
    val averageBrightness: Float,
    val averageSaturation: Float,
    val contrast: Float
)
