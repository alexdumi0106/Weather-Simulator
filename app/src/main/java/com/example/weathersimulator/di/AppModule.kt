package com.example.weathersimulator.di

import android.content.Context
import androidx.room.Room
import com.example.weathersimulator.core.data.local.WeatherDatabase
import com.example.weathersimulator.core.data.local.user.UserDao
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
    ): WeatherDatabase =
        Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_db"
        ).build()

    @Provides
    fun provideUserDao(db: WeatherDatabase): UserDao = db.userDao()

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
}
