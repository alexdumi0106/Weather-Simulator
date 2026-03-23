package com.example.weathersimulator.ui.viewmodel

data class AiUiState(
    val prompt: String = "",
    val serverUrl: String = "",
    val answer: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
