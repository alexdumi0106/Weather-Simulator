package com.example.weathersimulator.domain.weather

import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse
import kotlin.math.min

data class WeatherAlert(val title: String, val message: String)

object WeatherAlertEvaluator {

    fun evaluate(resp: OpenMeteoResponse): WeatherAlert? {

        val h = resp.hourly ?: return null
        val n = min(12, h.time.size)
        if (n == 0) return null

        val precipMax = h.precipitation.take(n).maxOrNull() ?: 0.0
        val windMax = h.windSpeed.take(n).maxOrNull() ?: 0.0
        val codes = h.weatherCode.take(n)

        val storm = codes.any { code -> code in listOf(95, 96, 99) }
        val rainSoon = precipMax >= 0.2
        val windy = windMax >= 45.0

        return when {
            storm -> WeatherAlert(
                "Atenție: posibilă furtună",
                "În următoarele ore pot apărea averse și descărcări electrice."
            )
            rainSoon -> WeatherAlert(
                "Ploaie posibilă",
                "Sunt șanse de precipitații în următoarele ore."
            )
            windy -> WeatherAlert(
                "Vânt puternic",
                "Vântul poate deveni puternic în următoarele ore."
            )
            else -> null
        }
    }
}