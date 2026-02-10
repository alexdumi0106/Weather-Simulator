package com.example.weathersimulator.workers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object WeatherWorkScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<WeatherAlertWorker>(3, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "weather_alerts_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun runNow(context: Context) {
        val req = OneTimeWorkRequestBuilder<WeatherAlertWorker>().build()
        WorkManager.getInstance(context).enqueue(req)
    }

}
