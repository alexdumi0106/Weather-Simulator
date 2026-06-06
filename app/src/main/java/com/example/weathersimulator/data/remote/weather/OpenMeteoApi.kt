package com.example.weathersimulator.data.remote.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,

        
        @Query("current") current: String =
            "temperature_2m,apparent_temperature,weather_code,is_day,cloud_cover,wind_speed_10m,relative_humidity_2m,pressure_msl,uv_index",

        @Query("hourly") hourly: String =
            "temperature_2m,weather_code,is_day,cloud_cover,wind_speed_10m,wind_gusts_10m,relative_humidity_2m,pressure_msl,precipitation,rain,snowfall,uv_index",

        
        @Query("daily") daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset",

        @Query("forecast_days") forecastDays: Int = 10,

        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse

    @GET("v1/forecast")
    suspend fun recentPastForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,

        @Query("hourly") hourly: String =
            "temperature_2m,relative_humidity_2m,pressure_msl,cloud_cover,wind_speed_10m,wind_gusts_10m,precipitation,rain,snowfall,weather_code,is_day",

        @Query("daily") daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset",

        @Query("past_days") pastDays: Int,

        @Query("forecast_days") forecastDays: Int = 1,

        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse

    @GET("https://archive-api.open-meteo.com/v1/archive")
    suspend fun archive(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,

        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,

        @Query("hourly") hourly: String =
            "temperature_2m,relative_humidity_2m,surface_pressure,cloud_cover,wind_speed_10m,wind_gusts_10m,precipitation,rain,snowfall,weather_code,is_day",

        @Query("daily") daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset",

        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}
