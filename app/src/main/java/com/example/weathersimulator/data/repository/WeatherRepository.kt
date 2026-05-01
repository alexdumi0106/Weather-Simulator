package com.example.weathersimulator.data.repository

import com.example.weathersimulator.data.local.weather.WeatherCsvDataset
import com.example.weathersimulator.data.local.weather.WeatherCsvReader
import com.example.weathersimulator.data.local.weather.WeatherCsvRow
import com.example.weathersimulator.data.remote.weather.CurrentDto
import com.example.weathersimulator.data.remote.weather.OpenMeteoApi
import com.example.weathersimulator.data.remote.weather.DailyDto
import com.example.weathersimulator.data.remote.weather.HourlyDto
import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: OpenMeteoApi,
    private val csvReader: WeatherCsvReader
) {
    private var cachedHistoricalDataset: WeatherCsvDataset? = null
    private var cachedHistoricalForecast: OpenMeteoResponse? = null

    suspend fun getForecast(lat: Double, lon: Double): OpenMeteoResponse {
        return api.forecast(lat = lat, lon = lon)
    }

    fun getHistoricalDataset(): WeatherCsvDataset {
        val cached = cachedHistoricalDataset
        if (cached != null) return cached

        val parsed = csvReader.read()
        cachedHistoricalDataset = parsed
        return parsed
    }

    fun getHistoricalForecast(): OpenMeteoResponse {
        val cached = cachedHistoricalForecast
        if (cached != null) return cached

        val dataset = getHistoricalDataset()
        val response = getHistoricalForecast(dataset)
        cachedHistoricalForecast = response
        return response
    }

    fun getHistoricalForecast(dataset: WeatherCsvDataset): OpenMeteoResponse {
        val cached = cachedHistoricalForecast
        if (cached != null) return cached

        val response = dataset.toOpenMeteoResponse()
        cachedHistoricalDataset = dataset
        cachedHistoricalForecast = response
        return response
    }

    private fun WeatherCsvDataset.toOpenMeteoResponse(): OpenMeteoResponse {
        val hourlyRows = rows
        val hourlyDto = HourlyDto(
            time = hourlyRows.map { it.time },
            temperature = hourlyRows.map { it.temperatureC },
            precipitation = hourlyRows.map { it.precipitationMm },
            rain = hourlyRows.map { it.rainMm },
            weatherCode = hourlyRows.map { it.weatherCode },
            cloudCover = hourlyRows.map { it.cloudCoverPercent },
            windSpeed = hourlyRows.map { it.windSpeed10mKmh },
            windGusts = hourlyRows.map { it.windGusts10mKmh },
            humidity = hourlyRows.map { it.relativeHumidityPercent },
            pressure = hourlyRows.map { it.surfacePressureHpa },
            isDay = hourlyRows.map { if (it.isDayTime()) 1 else 0 }
        )

        val parsedDailyRows = dailyRows.sortedBy { it.time }
        val dailyDates = parsedDailyRows.map { it.time }
        val weatherCodeCountsByDate = mutableMapOf<String, MutableMap<Int, Int>>()
        for (row in hourlyRows) {
            val date = row.time.take(10)
            val countsForDate = weatherCodeCountsByDate.getOrPut(date) { mutableMapOf() }
            countsForDate[row.weatherCode] = (countsForDate[row.weatherCode] ?: 0) + 1
        }

        val dailyDto = DailyDto(
            time = dailyDates,
            weatherCode = dailyDates.map { date ->
                weatherCodeCountsByDate[date].mostFrequentWeatherCode()
            },
            tempMax = parsedDailyRows.map { it.temperatureMaxC },
            tempMin = parsedDailyRows.map { it.temperatureMinC }
        )

        val latestRow = hourlyRows.lastOrNull()
        val currentDto = latestRow?.toCurrentDto()

        return OpenMeteoResponse(
            latitude = latitude,
            longitude = longitude,
            timezone = timezone,
            current = currentDto,
            hourly = hourlyDto,
            daily = dailyDto
        )
    }

    private fun WeatherCsvRow.toCurrentDto(): CurrentDto {
        return CurrentDto(
            time = time,
            temperature = temperatureC,
            apparentTemperature = temperatureC,
            humidity = relativeHumidityPercent,
            weatherCode = weatherCode,
            cloudCover = cloudCoverPercent,
            windSpeed = windSpeed10mKmh,
            pressure = surfacePressureHpa,
            isDay = if (isDayTime()) 1 else 0
        )
    }

    private fun WeatherCsvRow.isDayTime(): Boolean {
        val hour = time.substringAfter('T', missingDelimiterValue = "00:00").take(2).toIntOrNull() ?: 0
        return hour in 6..18
    }

    private fun Map<Int, Int>?.mostFrequentWeatherCode(): Int {
        val counts = this ?: return 0
        if (counts.isEmpty()) return 0

        return counts.maxByOrNull { it.value }?.key ?: 0
    }

}
