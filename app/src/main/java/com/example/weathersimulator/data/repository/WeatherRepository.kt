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
import java.time.YearMonth
import java.time.LocalDate

class WeatherRepository @Inject constructor(
    private val api: OpenMeteoApi,
    private val csvReader: WeatherCsvReader
) {
    private val cachedHistoricalDatasets = mutableMapOf<String, WeatherCsvDataset>()
    private val cachedHistoricalForecasts = mutableMapOf<String, OpenMeteoResponse>()

    data class ArchiveCity(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val csvFileName: String?
    )

    val archiveCities = listOf(
        ArchiveCity(
            name = "Timișoara",
            latitude = 45.7489,
            longitude = 21.2087,
            csvFileName = "open-meteo-45.80N21.18E96m.csv"
        ),
        ArchiveCity("București", 44.4268, 26.1025, null),
        ArchiveCity("Cluj-Napoca", 46.7712, 23.6236, null),
        ArchiveCity("Iași", 47.1585, 27.6014, null),
        ArchiveCity("Craiova", 44.3302, 23.7949, null),
        ArchiveCity("Constanța", 44.1598, 28.6348, null),
        ArchiveCity("Brașov", 45.6427, 25.5887, null)
    )

    suspend fun getForecast(lat: Double, lon: Double): OpenMeteoResponse {
        return api.forecast(lat = lat, lon = lon)
    }

    fun getArchiveCity(name: String): ArchiveCity {
        return archiveCities.firstOrNull { it.name == name } ?: archiveCities.first()
    }

    fun getHistoricalDataset(city: ArchiveCity): WeatherCsvDataset? {
        val fileName = city.csvFileName ?: return null

        val cached = cachedHistoricalDatasets[fileName]
        if (cached != null) return cached

        if (!csvReader.exists(fileName)) return null

        val parsed = csvReader.read(fileName)
        cachedHistoricalDatasets[fileName] = parsed
        return parsed
    }

    suspend fun getHistoricalForecast(
        cityName: String,
        monthKey: String? = null,
        source: String = "CSV"
    ): OpenMeteoResponse {
        val city = getArchiveCity(cityName)
        val cacheKey = "${city.name}_${source}_${monthKey ?: "csv"}"

        cachedHistoricalForecasts[cacheKey]?.let { return it }

        val shouldUseCsv =
            city.csvFileName != null &&
            source == "CSV"

        if (shouldUseCsv) {
            val csvDataset = getHistoricalDataset(city)

            if (csvDataset != null) {
                val response = csvDataset.toOpenMeteoResponse()
                cachedHistoricalForecasts[cacheKey] = response
                return response
            }
        }

        val safeMonthKey = monthKey ?: "2025-01"
        val response = getHistoricalForecastFromApi(city, safeMonthKey)
        cachedHistoricalForecasts[cacheKey] = response
        return response
    }

    private suspend fun getHistoricalForecastFromApi(
        city: ArchiveCity,
        monthKey: String
    ): OpenMeteoResponse {
        val yearMonth = YearMonth.parse(monthKey)

        val startDate = yearMonth.atDay(1)
        val yesterday = LocalDate.now().minusDays(1)

        val endDate = minOf(
            yearMonth.atEndOfMonth(),
            yesterday
        )

        return api.archive(
            lat = city.latitude,
            lon = city.longitude,
            startDate = startDate.toString(),
            endDate = endDate.toString()
        )
    }

    private fun WeatherCsvDataset.toOpenMeteoResponse(): OpenMeteoResponse {
        val hourlyRows = rows
        val hourlyDto = HourlyDto(
            time = hourlyRows.map { it.time },
            temperature = hourlyRows.map { it.temperatureC },
            precipitation = hourlyRows.map { it.precipitationMm },
            rain = hourlyRows.map { it.rainMm },
            snowfall = hourlyRows.map { it.snowfallCm },
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
            tempMin = parsedDailyRows.map { it.temperatureMinC },
            sunrise = parsedDailyRows.map { it.sunrise },
            sunset = parsedDailyRows.map { it.sunset }
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
