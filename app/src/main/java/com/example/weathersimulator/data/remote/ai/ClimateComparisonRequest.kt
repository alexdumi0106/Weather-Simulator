package com.example.weathersimulator.data.remote.ai

data class ClimateComparisonRequest(
    val selectedDay: ClimateDaySummaryRequest,
    val comparisonDay: ClimateDaySummaryRequest
)

data class ClimateDaySummaryRequest(
    val dateLabel: String,
    val maxTemperature: String,
    val minTemperature: String,
    val averageHumidity: String,
    val averagePressure: String
)
