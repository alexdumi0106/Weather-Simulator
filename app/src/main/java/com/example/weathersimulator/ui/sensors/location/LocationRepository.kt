package com.example.weathersimulator.ui.sensors.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import android.location.Location
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LocationRepository(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    private val request = LocationRequest.Builder(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        30_000L
    )
        .setMinUpdateIntervalMillis(10_000L)
        .build()

    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun start(onUpdate: (lat: Double, lon: Double, accuracy: Float?) -> Unit) {
        if (callback != null) return

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                onUpdate(loc.latitude, loc.longitude, loc.accuracy)
            }
        }

        client.requestLocationUpdates(request, callback!!, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    suspend fun getSingleLocation(timeoutMs: Long = 5000L): Location? = withContext(Dispatchers.IO) {
        try {
            // 1) încearcă lastLocation rapid
            val last = Tasks.await(client.lastLocation, timeoutMs, TimeUnit.MILLISECONDS)
            if (last != null) return@withContext last

            // 2) fallback: getCurrentLocation (mai sigur, dar poate dura)
            val tokenSource = com.google.android.gms.tasks.CancellationTokenSource()
            val currentTask = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
            Tasks.await(currentTask, timeoutMs, TimeUnit.MILLISECONDS)
        } catch (_: Exception) {
            null
        }
    }

    fun stop() {
        callback?.let { client.removeLocationUpdates(it) }
        callback = null
    }
}



