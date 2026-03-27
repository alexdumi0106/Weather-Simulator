package com.example.weathersimulator.ui.screens.main

import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse

data class WeatherUiState(
    val isLoading: Boolean = false,
    val data: OpenMeteoResponse? = null,
    val hourlyForecast: List<HourlyForecastItemUi> = emptyList(),
    val dailyForecast: List<DailyForecastItemUi> = emptyList(),
    val error: String? = null
)

data class DailyForecastItemUi(
    val dayLabel: String,
    val maxTemperature: String,
    val minTemperature: String,
    val weatherCode: Int,
    val cloudCover: Int,
    val isDay: Boolean = true
)

data class HourlyForecastItemUi(
    val time: String,
    val temperature: String,
    val weatherCode: Int,
    val isDay: Boolean,
    val cloudCover: Int
)