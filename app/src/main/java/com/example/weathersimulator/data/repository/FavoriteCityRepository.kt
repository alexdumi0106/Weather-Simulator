package com.example.weathersimulator.data.repository

import com.example.weathersimulator.data.local.city.FavoriteCityDao
import com.example.weathersimulator.data.local.city.FavoriteCityEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoriteCityRepository @Inject constructor(
    private val dao: FavoriteCityDao
) {
    fun observeFavorites(): Flow<List<FavoriteCityEntity>> {
        return dao.observeFavorites()
    }

    suspend fun getLastSelectedCity(): FavoriteCityEntity? {
        return dao.getLastSelectedCity()
    }

    suspend fun saveFavorite(city: FavoriteCityEntity) {
        dao.upsert(city.copy(isLastSelected = false))
    }

    suspend fun saveLastSelected(city: FavoriteCityEntity) {
        dao.clearLastSelected()
        dao.upsert(city.copy(id = "last_selected_city", isLastSelected = true))
    }

    suspend fun deleteFavorite(id: String) {
        dao.deleteFavorite(id)
    }
}