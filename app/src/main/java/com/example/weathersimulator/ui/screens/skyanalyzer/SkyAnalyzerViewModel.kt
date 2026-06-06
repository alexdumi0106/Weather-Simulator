package com.example.weathersimulator.ui.screens.skyanalyzer

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersimulator.data.local.ai.AiSettingsStore
import com.example.weathersimulator.data.remote.ai.SkyObservationRequest
import com.example.weathersimulator.domain.usecase.GenerateAiResponseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SkyAnalyzerViewModel @Inject constructor(
    private val generateAiResponse: GenerateAiResponseUseCase,
    private val aiSettingsStore: AiSettingsStore
) : ViewModel() {
    private val _state = MutableStateFlow(SkyAnalyzerUiState())
    val state: StateFlow<SkyAnalyzerUiState> = _state.asStateFlow()

    fun analyze(
        photo: Bitmap,
        source: SkyPhotoSource
    ) {
        _state.update {
            it.copy(
                photo = photo,
                photoSource = source,
                result = null,
                isAnalyzing = true,
                isGeneratingAiObservation = false,
                aiObservationError = null,
                permissionMessage = null
            )
        }

        viewModelScope.launch {
            val localResult = withContext(Dispatchers.Default) {
                SkyAnalyzer.analyze(photo)
            }

            _state.update {
                it.copy(
                    result = localResult,
                    isAnalyzing = false,
                    isGeneratingAiObservation = true
                )
            }

            try {
                val aiText = withContext(Dispatchers.IO) {
                    generateAiResponse.skyObservation(
                        request = localResult.toSkyObservationRequest(),
                        serverUrl = aiSettingsStore.getServerUrl()
                    )
                }.cleanAiObservation(localResult)

                _state.update {
                    it.copy(
                        result = localResult.copy(aiObservation = aiText),
                        isGeneratingAiObservation = false,
                        aiObservationError = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        result = localResult.copy(
                            aiObservation = fallbackObservation(localResult)
                        ),
                        isGeneratingAiObservation = false,
                        aiObservationError = "Nu am putut contacta serviciul AI. Afisez observatia generata din analiza imaginii."
                    )
                }
            }
        }
    }

    fun showPermissionMessage() {
        _state.update {
            it.copy(permissionMessage = "Permisiunea pentru camera este necesara ca sa analizam cerul.")
        }
    }

    fun showImageLoadError() {
        _state.update {
            it.copy(permissionMessage = "Nu am putut incarca imaginea din galerie. Incearca o alta poza.")
        }
    }

    private fun SkyAnalysisResult.toSkyObservationRequest() =
        SkyObservationRequest(
            cloudType = cloudType,
            rainProbability = rainProbability,
            stormProbability = stormProbability,
            photographyScore = photographyScore,
            bestMoment = bestMoment,
            sunsetScore = sunsetScore,
            sunriseScore = sunriseScore,
            stormScore = stormScore,
            dramaticCloudsScore = dramaticCloudsScore,
            fogScore = fogScore,
            skyRatio = metrics.skyRatio,
            cloudRatio = metrics.cloudRatio,
            darkCloudRatio = metrics.darkCloudRatio,
            warmLightRatio = metrics.warmLightRatio,
            averageBrightness = metrics.averageBrightness,
            averageSaturation = metrics.averageSaturation,
            contrast = metrics.contrast
        )

    private fun fallbackObservation(result: SkyAnalysisResult): String {
        val riskText = when {
            result.stormProbability >= 55 -> "Norii intunecati si contrastul ridicat sugereaza instabilitate; fotografia are un caracter amenintator, nu calm."
            result.rainProbability >= 45 -> "Cerul pare incarcat, iar indiciile vizuale sugereaza sanse moderate de ploaie."
            else -> "Riscul vizual de ploaie si furtuna ramane redus in aceasta analiza."
        }

        return "Imaginea indica ${result.cloudType.lowercase()} cu un scor foto de ${result.photographyScore}/100 pentru ${result.bestMoment.lowercase()}. $riskText ${result.shortAdvice}"
    }

    private fun String.cleanAiObservation(result: SkyAnalysisResult): String {
        val cleaned = trim()
            .removePrefix("Raspuns:")
            .removePrefix("Răspuns:")
            .trim()
            .ifBlank { "Serviciul AI nu a returnat o descriere clara pentru aceasta imagine." }
        if (cleaned.looksLikeAiBackendError()) {
            return fallbackObservation(result)
        }

        val highRiskVisual = result.stormProbability >= 55 ||
            result.rainProbability >= 55 ||
            result.metrics.darkCloudRatio >= 0.14f ||
            result.cloudType.contains("furtuna", ignoreCase = true) ||
            result.cloudType.contains("ploaie", ignoreCase = true)

        val tooCalm = listOf(
            "calm",
            "linistit",
            "linistita",
            "risc redus",
            "sanse reduse",
            "senin"
        ).any { token ->
            cleaned.contains(token, ignoreCase = true)
        }

        return if (highRiskVisual && tooCalm) {
            fallbackObservation(result)
        } else {
            cleaned
        }
    }

    private fun String.looksLikeAiBackendError(): Boolean {
        val lower = lowercase()

        return lower.startsWith("backend error:") ||
            lower.startsWith("backend local error:") ||
            lower.contains("llama runner process has terminated") ||
            lower.contains("unable to allocate cpu buffer") ||
            lower.contains("status code: 500") ||
            lower.contains("panic:")
    }
}
