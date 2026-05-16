package com.example.weathersimulator.data.remote.city

import com.google.gson.annotations.SerializedName

data class CitySearchResponse(
    val results: List<CityResultDto>?
)

data class CityResultDto(
    val id: Long?,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?,
    val timezone: String?
) {
    fun displayName(): String {
        return listOfNotNull(name, admin1, country)
            .distinct()
            .joinToString(", ")
    }
}