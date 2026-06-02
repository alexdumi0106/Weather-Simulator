package com.example.weathersimulator.data.remote.ai

data class OutfitRecommendationRequest(
    val cityName: String,
    val temperature: Double?,
    val apparentTemperature: Double?,
    val humidity: Int?,
    val windSpeed: Double?,
    val precipitationNextHours: List<Double>,
    val uvIndex: Double?,
    val momentOfDay: String,
    val nextHours: List<String>,
    val nextTemperatures: List<Double>
)