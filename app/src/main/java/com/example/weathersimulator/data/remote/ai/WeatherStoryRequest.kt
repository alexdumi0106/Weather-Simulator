package com.example.weathersimulator.data.remote.ai

data class WeatherStoryRequest(
    val temperature: Double?,
    val apparentTemperature: Double?,
    val humidity: Int?,
    val windSpeed: Double?,
    val pressure: Double?,
    val weatherCode: Int?,
    val cloudCover: Int?,
    val nextHours: List<String>,
    val nextTemperatures: List<Double>,
    val nextPrecipitation: List<Double>,
    val nextWindSpeed: List<Double>,
    val nextWeatherCodes: List<Int>,
    val dailyDates: List<String>,
    val dailyMaxTemperatures: List<Double>,
    val dailyMinTemperatures: List<Double>,
    val dailyWeatherCodes: List<Int>
)
