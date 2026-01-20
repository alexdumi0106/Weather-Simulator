package com.example.weathersimulator.di

import android.content.Context
import com.example.weathersimulator.ui.sensors.location.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.weathersimulator.ui.sensors.location.GeocodingRepository


@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationRepository(@ApplicationContext context: Context): LocationRepository {
        return LocationRepository(context)
    }

    @Provides
    @Singleton
    fun provideGeocodingRepository(@ApplicationContext context: Context): GeocodingRepository {
        return GeocodingRepository(context)
    }
}


