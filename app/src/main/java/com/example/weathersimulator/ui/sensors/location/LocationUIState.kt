package com.example.weathersimulator.ui.sensors.location

data class LocationUiState(
    val hasPermission: Boolean = false,
    val placeName: String? = null,
    val isGpsEnabled: Boolean = true,
    val lat: Double? = null,
    val lon: Double? = null,
    val accuracyMeters: Float? = null,
    val lastUpdatedEpochMs: Long? = null,
    val error: String? = null
)