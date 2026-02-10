package com.example.weathersimulator.data.remote.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "precipitation,weathercode,windspeed_10m",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}
