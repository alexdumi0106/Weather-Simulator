package com.example.weathersimulator.data.repository

import com.example.weathersimulator.data.remote.weather.OpenMeteoApi
import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: OpenMeteoApi
) {
    suspend fun getForecast(lat: Double, lon: Double): OpenMeteoResponse {
        return api.forecast(lat = lat, lon = lon)
    }
}
