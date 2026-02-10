package com.example.weathersimulator.data.remote.weather

data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val hourly: Hourly?
)

data class Hourly(
    val time: List<String> = emptyList(),
    val precipitation: List<Double> = emptyList(),
    val weathercode: List<Int> = emptyList(),
    val windspeed_10m: List<Double> = emptyList()
)
