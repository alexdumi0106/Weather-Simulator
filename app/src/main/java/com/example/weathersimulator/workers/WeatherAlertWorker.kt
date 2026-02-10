package com.example.weathersimulator.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weathersimulator.data.repository.WeatherRepository
import com.example.weathersimulator.domain.weather.WeatherAlertEvaluator
import com.example.weathersimulator.notifications.WeatherNotifier
import com.example.weathersimulator.ui.sensors.location.LocationRepository
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

class WeatherAlertWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun locationRepository(): LocationRepository
        fun weatherRepository(): WeatherRepository
    }

    override suspend fun doWork(): Result {
        return try {
            val ep = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)

            val locRepo = ep.locationRepository()
            val weatherRepo = ep.weatherRepository()

            val loc = locRepo.getSingleLocation() ?: return Result.success()
            val forecast = weatherRepo.getForecast(loc.latitude, loc.longitude)

            val alert = WeatherAlertEvaluator.evaluate(forecast)
            if (alert != null) {
                WeatherNotifier.show(applicationContext, alert.title, alert.message)
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
