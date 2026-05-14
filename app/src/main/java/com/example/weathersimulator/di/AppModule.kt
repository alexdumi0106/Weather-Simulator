package com.example.weathersimulator.di

import android.content.Context
import androidx.room.Room
import com.example.weathersimulator.data.local.user.UserDao
import com.example.weathersimulator.data.local.weather.WeatherDatabase
import com.example.weathersimulator.data.local.weather.WeatherCsvReader
import com.example.weathersimulator.data.repository.UserRepository
import com.example.weathersimulator.domain.usecase.LoginUserUseCase
import com.example.weathersimulator.domain.usecase.RegisterUserUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.weathersimulator.data.local.ai.AiMessageDao
import com.example.weathersimulator.data.local.ai.AiConversationDao
import com.example.weathersimulator.data.local.weather.MIGRATION_3_4


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_db"
        )
        .addMigrations(MIGRATION_3_4)
        .build()
    }

    @Provides
    fun provideUserDao(db: WeatherDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideWeatherCsvReader(
        @ApplicationContext context: Context
    ): WeatherCsvReader = WeatherCsvReader(context)

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        userDao: UserDao
    ): UserRepository = UserRepository(auth, userDao)

    @Provides
    fun provideLoginUserUseCase(
        repo: UserRepository
    ): LoginUserUseCase = LoginUserUseCase(repo)

    @Provides
    fun provideRegisterUserUseCase(
        repo: UserRepository
    ): RegisterUserUseCase = RegisterUserUseCase(repo)

    @Provides
    fun provideAiMessageDao(db: WeatherDatabase): AiMessageDao {
        return db.aiMessageDao()
    }

    @Provides
    fun provideAiConversationDao(db: WeatherDatabase): AiConversationDao {
        return db.aiConversationDao()
    }
}