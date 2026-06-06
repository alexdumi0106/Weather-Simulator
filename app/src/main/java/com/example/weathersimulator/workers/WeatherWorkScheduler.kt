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
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )

        runNow(context)
    }

    fun runNow(context: Context) {
        val req = OneTimeWorkRequestBuilder<WeatherAlertWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "weather_alerts_check_now",
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

}
