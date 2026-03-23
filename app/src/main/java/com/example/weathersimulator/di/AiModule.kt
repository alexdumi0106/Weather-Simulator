package com.example.weathersimulator.di

import com.example.weathersimulator.data.remote.ai.OllamaAiRepository
import com.example.weathersimulator.repository.AiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiRepository(): AiRepository = OllamaAiRepository()
}
