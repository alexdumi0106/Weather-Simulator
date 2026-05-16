package com.example.weathersimulator.data.local.city

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_cities")
data class FavoriteCityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val timezone: String?,
    val isLastSelected: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)