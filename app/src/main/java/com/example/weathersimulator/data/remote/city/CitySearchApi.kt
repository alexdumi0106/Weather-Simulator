package com.example.weathersimulator.data.remote.city

import retrofit2.http.GET
import retrofit2.http.Query

interface CitySearchApi {

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 8,
        @Query("language") language: String = "ro",
        @Query("format") format: String = "json"
    ): CitySearchResponse
}