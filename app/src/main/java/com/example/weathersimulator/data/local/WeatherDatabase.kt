package com.example.weathersimulator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weathersimulator.data.local.user.UserDao
import com.example.weathersimulator.data.local.user.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
