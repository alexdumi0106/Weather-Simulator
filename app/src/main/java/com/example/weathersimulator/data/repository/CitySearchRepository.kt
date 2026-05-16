package com.example.weathersimulator.data.repository

import com.example.weathersimulator.data.remote.city.CityResultDto
import com.example.weathersimulator.data.remote.city.CitySearchApi
import javax.inject.Inject

class CitySearchRepository @Inject constructor(
    private val api: CitySearchApi
) {
    suspend fun searchCity(query: String): List<CityResultDto> {
        if (query.isBlank()) return emptyList()

        return api.searchCity(name = query.trim())
            .results
            .orEmpty()
    }
}