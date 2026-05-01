package com.example.weathersimulator.data.remote.weather

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,

    val current: CurrentDto? = null,
    val hourly: HourlyDto? = null,
    val daily: DailyDto? = null
)

data class CurrentDto(
    val time: String? = null,

    @SerializedName("temperature_2m")
    val temperature: Double? = null,

    @SerializedName("apparent_temperature")
    val apparentTemperature: Double? = null,

    @SerializedName("relative_humidity_2m")
    val humidity: Int? = null,

    @SerializedName("weather_code")
    val weatherCode: Int? = null,

    @SerializedName("cloud_cover")
    val cloudCover: Int? = null,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double? = null,

    @SerializedName("pressure_msl")
    val pressure: Double? = null,

    @SerializedName("is_day")
    val isDay: Int?
)

data class HourlyDto(
    val time: List<String> = emptyList(),

    @SerializedName("temperature_2m")
    val temperature: List<Double> = emptyList(),

    val precipitation: List<Double> = emptyList(),

    val rain: List<Double> = emptyList(),

    @SerializedName("weather_code")
    val weatherCode: List<Int> = emptyList(),

    @SerializedName("cloud_cover")
    val cloudCover: List<Int> = emptyList(),

    @SerializedName("wind_speed_10m")
    val windSpeed: List<Double> = emptyList(),

    @SerializedName("wind_gusts_10m")
    val windGusts: List<Double> = emptyList(),

    @SerializedName("relative_humidity_2m")
    val humidity: List<Int> = emptyList(),

    @SerializedName("pressure_msl")
    val pressure: List<Double> = emptyList(),

    @SerializedName("is_day")
    val isDay: List<Int> = emptyList()
)

data class DailyDto(
    @SerializedName("time")
    val time: List<String> = emptyList(),

    @SerializedName("weather_code")
    val weatherCode: List<Int> = emptyList(),

    @SerializedName("temperature_2m_max")
    val tempMax: List<Double> = emptyList(),

    @SerializedName("temperature_2m_min")
    val tempMin: List<Double> = emptyList()
)