package com.example.weathersimulator.data.remote.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,

        // Current weather (cardul de sus)
        @Query("current") current: String =
            "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,pressure_msl",

        // Hourly forecast (scroll orizontal)
        @Query("hourly") hourly: String =
            "temperature_2m,precipitation,weather_code,wind_speed_10m,relative_humidity_2m,pressure_msl",

        // Daily forecast (7 zile)
        @Query("daily") daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min",

        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}