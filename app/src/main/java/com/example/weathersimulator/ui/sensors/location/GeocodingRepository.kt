package com.example.weathersimulator.ui.sensors.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import java.util.Locale

class GeocodingRepository(
    private val context: Context
) {
    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Async callback API (Android 13+)
                kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1) { list ->
                        val addr = list.firstOrNull()
                        val city = addr?.locality ?: addr?.subAdminArea
                        val country = addr?.countryName
                        cont.resume(
                            listOfNotNull(city, country).joinToString(", ").ifBlank { null },
                            onCancellation = null
                        )
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(lat, lon, 1)
                val addr = list?.firstOrNull()
                val city = addr?.locality ?: addr?.subAdminArea
                val country = addr?.countryName
                listOfNotNull(city, country).joinToString(", ").ifBlank { null }
            }
        } catch (_: Exception) {
            null
        }
    }
}
