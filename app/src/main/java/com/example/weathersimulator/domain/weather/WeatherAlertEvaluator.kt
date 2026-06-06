package com.example.weathersimulator.domain.weather

import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse
import kotlin.math.roundToInt

data class WeatherAlert(
    val id: String,
    val title: String,
    val message: String,
    val priority: Int,
    val urgent: Boolean = false,
    val cooldownMs: Long = 6 * 60 * 60 * 1000L
)

object WeatherAlertEvaluator {

    fun evaluate(resp: OpenMeteoResponse): WeatherAlert? {
        return evaluateMessages(resp).firstOrNull()
    }

    fun evaluateMessages(resp: OpenMeteoResponse): List<WeatherAlert> {
        val hourly = resp.hourly ?: return emptyList()
        val current = resp.current

        val currentIndex = current?.time
            ?.let { time -> hourly.time.indexOf(time) }
            ?.takeIf { it >= 0 }
            ?: 0

        val next3 = forecastWindow(hourly.time.size, currentIndex, 3)
        val next6 = forecastWindow(hourly.time.size, currentIndex, 6)
        val next12 = forecastWindow(hourly.time.size, currentIndex, 12)
        val today = forecastWindow(hourly.time.size, currentIndex, 24)

        if (next12.isEmpty()) return emptyList()

        val next3Codes = hourly.weatherCode.intValuesAt(next3)
        val next6Codes = hourly.weatherCode.intValuesAt(next6)
        val next12Codes = hourly.weatherCode.intValuesAt(next12)
        val todayCodes = hourly.weatherCode.intValuesAt(today)

        val next6Precip = hourly.precipitation.doubleValuesAt(next6)
        val next12Precip = hourly.precipitation.doubleValuesAt(next12)
        val todayPrecip = hourly.precipitation.doubleValuesAt(today)
        val todayTemps = hourly.temperature.doubleValuesAt(today)
        val todayUv = hourly.uvIndex.doubleValuesAt(today)
        val todayWind = hourly.windSpeed.doubleValuesAt(today)
        val next12Wind = hourly.windSpeed.doubleValuesAt(next12)
        val next12Gusts = hourly.windGusts.doubleValuesAt(next12)
        val todaySnow = hourly.snowfall.doubleValuesAt(today)
        val todayHumidity = hourly.humidity.intValuesAt(today)

        val maxNext6Precip = next6Precip.maxOrNull() ?: 0.0
        val maxNext12Precip = next12Precip.maxOrNull() ?: 0.0
        val totalTodayPrecip = todayPrecip.sum()
        val maxTodayTemp = maxOf(
            current?.apparentTemperature ?: Double.NEGATIVE_INFINITY,
            current?.temperature ?: Double.NEGATIVE_INFINITY,
            todayTemps.maxOrNull() ?: Double.NEGATIVE_INFINITY,
            resp.daily?.tempMax?.firstOrNull() ?: Double.NEGATIVE_INFINITY
        )
        val minTodayTemp = minOf(
            current?.apparentTemperature ?: Double.POSITIVE_INFINITY,
            current?.temperature ?: Double.POSITIVE_INFINITY,
            todayTemps.minOrNull() ?: Double.POSITIVE_INFINITY,
            resp.daily?.tempMin?.firstOrNull() ?: Double.POSITIVE_INFINITY
        )
        val maxUv = maxOf(
            current?.uvIndex ?: 0.0,
            todayUv.maxOrNull() ?: 0.0
        )
        val maxWind = maxOf(
            current?.windSpeed ?: 0.0,
            todayWind.maxOrNull() ?: 0.0,
            next12Wind.maxOrNull() ?: 0.0
        )
        val maxGusts = next12Gusts.maxOrNull() ?: 0.0

        val stormCodes = setOf(95, 96, 99, 996, 997, 998)
        val rainCodes = setOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82)
        val snowCodes = setOf(71, 73, 75, 77, 85, 86)
        val fogCodes = setOf(45, 48)

        val messages = mutableListOf<WeatherAlert>()

        val stormSoon = next6Codes.any { it in stormCodes } ||
            (next3Codes.any { it in rainCodes } && (maxGusts >= 60.0 || maxNext6Precip >= 5.0))

        if (stormSoon) {
            messages += WeatherAlert(
                id = "storm_warning",
                title = "Avertizare de furtuna",
                message = "Cauta un adapost cat mai curand. In urmatoarele ore pot aparea averse puternice, fulgere sau rafale intense.",
                priority = 100,
                urgent = true,
                cooldownMs = 2 * 60 * 60 * 1000L
            )
        }

        val heavyRainSoon = maxNext12Precip >= 3.0 ||
            next12Codes.any { it in setOf(63, 65, 80, 81, 82) }
        val rainToday = maxNext12Precip >= 0.4 ||
            totalTodayPrecip >= 1.0 ||
            next12Codes.any { it in rainCodes }

        if (!stormSoon && heavyRainSoon) {
            messages += WeatherAlert(
                id = "heavy_rain",
                title = "Ploaie puternica in apropiere",
                message = "Nu uita umbrela sau o geaca impermeabila. Sunt asteptate precipitatii mai serioase in urmatoarele ore.",
                priority = 85
            )
        } else if (!stormSoon && rainToday) {
            messages += WeatherAlert(
                id = "umbrella_reminder",
                title = "Ia umbrela cu tine",
                message = "Sunt asteptate precipitatii astazi. O umbrela sau o haina impermeabila iti pot prinde bine.",
                priority = 70
            )
        }

        if (maxTodayTemp >= 34.0) {
            messages += WeatherAlert(
                id = "extreme_heat",
                title = "Zi caniculara",
                message = "Nu uita sa te hidratezi si evita expunerea lunga la soare. Temperatura resimtita poate urca spre ${maxTodayTemp.roundForMessage()} grade.",
                priority = 82
            )
        } else if (maxTodayTemp >= 30.0) {
            messages += WeatherAlert(
                id = "hydration_reminder",
                title = "Hidratare recomandata",
                message = "Te asteapta o zi calda. Bea apa mai des si ia pauze la umbra daca stai mult afara.",
                priority = 62
            )
        }

        if (maxUv >= 7.0 && current?.isDay == 1) {
            messages += WeatherAlert(
                id = "uv_warning",
                title = "Indice UV ridicat",
                message = "Soarele poate fi puternic astazi. Foloseste protectie solara si evita orele de varf daca poti.",
                priority = 58
            )
        }

        if (maxGusts >= 65.0 || maxWind >= 45.0) {
            messages += WeatherAlert(
                id = "strong_wind",
                title = "Vant puternic",
                message = "Rafalele pot deveni intense in urmatoarele ore. Ai grija la copaci, obiecte usoare si deplasari.",
                priority = 78
            )
        } else if (maxGusts >= 45.0 || maxWind >= 32.0) {
            messages += WeatherAlert(
                id = "windy_day",
                title = "Vant in intensificare",
                message = "Vantul poate deveni neplacut astazi. Prinde bine sa iti ajustezi planurile in aer liber.",
                priority = 48
            )
        }

        val snowToday = todayCodes.any { it in snowCodes } || todaySnow.any { it > 0.0 }
        if (snowToday) {
            messages += WeatherAlert(
                id = "snow_warning",
                title = "Ninsoare posibila",
                message = "In urmatoarele ore poate ninge. Ia in calcul drumuri mai lente si imbracaminte mai groasa.",
                priority = 74
            )
        }

        val icyRisk = minTodayTemp <= 1.0 &&
            (todayPrecip.any { it > 0.0 } || todayCodes.any { it in rainCodes || it in snowCodes })

        if (icyRisk) {
            messages += WeatherAlert(
                id = "ice_risk",
                title = "Risc de polei",
                message = "Temperaturile joase si umezeala pot face suprafetele alunecoase. Mergi cu prudenta.",
                priority = 80
            )
        } else if (minTodayTemp <= -5.0) {
            messages += WeatherAlert(
                id = "cold_warning",
                title = "Frig accentuat",
                message = "Se anunta temperaturi scazute. Imbraca-te in straturi si protejeaza mainile si fata.",
                priority = 55
            )
        }

        val fogLikely = next12Codes.any { it in fogCodes } ||
            (todayHumidity.any { it >= 94 } && maxWind <= 12.0)

        if (fogLikely) {
            messages += WeatherAlert(
                id = "fog_warning",
                title = "Ceata posibila",
                message = "Vizibilitatea poate scadea in urmatoarele ore. Condu prudent si pastreaza distanta.",
                priority = 52
            )
        }

        return messages
            .distinctBy { it.id }
            .sortedByDescending { it.priority }
            .take(MaxMessagesPerCheck)
    }

    private fun forecastWindow(
        hourlySize: Int,
        start: Int,
        hours: Int
    ): IntRange {
        if (hourlySize <= 0) return IntRange.EMPTY
        val safeStart = start.coerceIn(0, hourlySize - 1)
        val endExclusive = (safeStart + hours).coerceAtMost(hourlySize)
        if (safeStart >= endExclusive) return IntRange.EMPTY
        return safeStart until endExclusive
    }

    private fun List<Double>.doubleValuesAt(indexes: IntRange): List<Double> {
        return indexes.mapNotNull { index -> getOrNull(index) }
    }

    private fun List<Int>.intValuesAt(indexes: IntRange): List<Int> {
        return indexes.mapNotNull { index -> getOrNull(index) }
    }

    private fun Double.roundForMessage(): Int {
        return this.roundToInt()
    }

    private const val MaxMessagesPerCheck = 2
}
