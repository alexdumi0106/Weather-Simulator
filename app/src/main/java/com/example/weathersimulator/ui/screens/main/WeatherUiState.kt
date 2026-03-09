package com.example.weathersimulator.ui.screens.main

import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse

data class WeatherUiState(
    val isLoading: Boolean = false,
    val data: OpenMeteoResponse? = null,
    val error: String? = null
)