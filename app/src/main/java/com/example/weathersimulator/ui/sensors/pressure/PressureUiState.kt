package com.example.weathersimulator.sensors.pressure

data class PressureUiState(
    val isAvailable: Boolean = true,
    val pressureHpa: Float? = null,
    val baselineHpa: Float? = null,
    val trendHpaPerHour: Float? = null,
    val trendLabel: PressureTrend = PressureTrend.UNKNOWN
)

enum class PressureTrend {
    UNKNOWN,
    STABLE,
    FALLING,
    RAPID_FALL,
    RISING
}
