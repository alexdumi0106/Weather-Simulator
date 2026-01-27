package com.example.weathersimulator.di

import android.content.Context
import com.example.weathersimulator.sensors.pressure.PressureSensorDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PressureModule {

    @Provides
    @Singleton
    fun providePressureSensorDataSource(
        @ApplicationContext context: Context
    ): PressureSensorDataSource {
        return PressureSensorDataSource(context)
    }
}
