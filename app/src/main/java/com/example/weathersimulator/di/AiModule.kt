package com.example.weathersimulator.di

import com.example.weathersimulator.data.remote.ai.OllamaAiRepository
import com.example.weathersimulator.data.remote.ai.OllamaApiService
import com.example.weathersimulator.repository.AiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    /**
     * IMPORTANT:
     * - Emulator: http://10.0.2.2:11434/
     * - Telefon: http://IP-ul-PC-ului:11434/
     */
    @Provides
    @Singleton
    fun provideOllamaApiService(): OllamaApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://localhost:11434/") // pentru emulator
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAiRepository(api: OllamaApiService): AiRepository =
        OllamaAiRepository(api)
}
