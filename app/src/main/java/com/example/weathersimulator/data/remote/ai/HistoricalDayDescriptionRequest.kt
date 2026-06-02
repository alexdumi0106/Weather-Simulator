package com.example.weathersimulator.data.remote.ai

data class HistoricalDayDescriptionRequest(
    val dateLabel: String,
    val maxTemperature: String,
    val minTemperature: String,
    val averageHumidity: String,
    val averagePressure: String,
    val sunrise: String?,
    val sunset: String?,
    val hourlySnapshots: List<HistoricalHourlySnapshot>
)

data class HistoricalHourlySnapshot(
    val time: String,
    val temperature: String,
    val weatherCode: Int,
    val cloudCover: Int
)
