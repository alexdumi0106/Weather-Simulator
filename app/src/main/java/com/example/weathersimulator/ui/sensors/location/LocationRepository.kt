package com.example.weathersimulator.ui.sensors.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*

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

    fun stop() {
        callback?.let { client.removeLocationUpdates(it) }
        callback = null
    }
}

