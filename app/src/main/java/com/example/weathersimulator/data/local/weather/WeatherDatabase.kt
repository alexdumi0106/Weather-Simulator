package com.example.weathersimulator.data.local.weather

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weathersimulator.data.local.user.UserDao
import com.example.weathersimulator.data.local.user.UserEntity
import com.example.weathersimulator.data.local.ai.AiMessageEntity
import com.example.weathersimulator.data.local.ai.AiMessageDao

@Database(
    entities = [
        UserEntity::class,
        AiMessageEntity::class
    ],
    version = 2
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun aiMessageDao(): AiMessageDao
    abstract fun userDao(): UserDao
}