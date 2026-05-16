package com.example.weathersimulator.data.local.city

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCityDao {

    @Query("SELECT * FROM favorite_cities WHERE isLastSelected = 0 ORDER BY savedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteCityEntity>>

    @Query("SELECT * FROM favorite_cities WHERE isLastSelected = 1 LIMIT 1")
    suspend fun getLastSelectedCity(): FavoriteCityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(city: FavoriteCityEntity)

    @Query("DELETE FROM favorite_cities WHERE id = :id AND isLastSelected = 0")
    suspend fun deleteFavorite(id: String)

    @Query("UPDATE favorite_cities SET isLastSelected = 0 WHERE isLastSelected = 1")
    suspend fun clearLastSelected()
}