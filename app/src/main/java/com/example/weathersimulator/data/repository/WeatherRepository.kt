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
import com.example.weathersimulator.data.remote.weather.OpenMeteoArchiveApi

class WeatherRepository @Inject constructor(
    private val api: OpenMeteoApi,
    private val archiveApi: OpenMeteoArchiveApi,
    private val csvReader: WeatherCsvReader
) {
    private val cachedHistoricalDatasets = mutableMapOf<String, WeatherCsvDataset>()
    private val cachedHistoricalForecasts = mutableMapOf<String, OpenMeteoResponse>()

    data class ArchiveCity(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val csvFileName: String?,
        val timezone: String = "auto"
    )

    val archiveCities = listOf(
        ArchiveCity(
            name = "Timișoara",
            latitude = 45.7489,
            longitude = 21.2087,
            csvFileName = "open-meteo-45.80N21.18E96m.csv",
            timezone = "Europe/Bucharest"
        ),
        ArchiveCity("București", 44.4268, 26.1025, null, "Europe/Bucharest"),
        ArchiveCity("Cluj-Napoca", 46.7712, 23.6236, null, "Europe/Bucharest"),
        ArchiveCity("Iași", 47.1585, 27.6014, null, "Europe/Bucharest"),
        ArchiveCity("Craiova", 44.3302, 23.7949, null, "Europe/Bucharest"),
        ArchiveCity("Constanța", 44.1598, 28.6348, null, "Europe/Bucharest"),
        ArchiveCity("Brașov", 45.6427, 25.5887, null, "Europe/Bucharest")
    )

    suspend fun getForecast(lat: Double, lon: Double): OpenMeteoResponse {
        return api.forecast(lat = lat, lon = lon)
    }

    fun getArchiveCity(name: String): ArchiveCity {
        return archiveCities.firstOrNull { it.name == name } ?: archiveCities.first()
    }

    fun buildDynamicArchiveCity(
        name: String,
        latitude: Double,
        longitude: Double,
        timezone: String?
    ): ArchiveCity {
        return ArchiveCity(
            name = name,
            latitude = latitude,
            longitude = longitude,
            csvFileName = null,
            timezone = timezone ?: "auto"
        )
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

    suspend fun getHistoricalForecastForCity(
        city: ArchiveCity,
        monthKey: String,
        source: String = "API"
    ): OpenMeteoResponse {
        val cacheKey = "${city.name}_${source}_$monthKey"

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

        val response = getHistoricalForecastFromApi(city, monthKey)
        cachedHistoricalForecasts[cacheKey] = response
        return response
    }

    private suspend fun getRecentPastForecastForRange(
        city: ArchiveCity,
        startDate: LocalDate,
        endDate: LocalDate
    ): OpenMeteoResponse {
        val today = LocalDate.now()

        val pastDays = java.time.temporal.ChronoUnit.DAYS
            .between(startDate, today)
            .toInt()
            .coerceIn(1, 92)

        val response = api.recentPastForecast(
            lat = city.latitude,
            lon = city.longitude,
            pastDays = pastDays,
            timezone = city.timezone
        )

        return filterHistoricalResponseByDateRange(
            response = response,
            startDate = startDate,
            endDate = endDate
        )
    }

    private fun filterHistoricalResponseByDateRange(
        response: OpenMeteoResponse,
        startDate: LocalDate,
        endDate: LocalDate
    ): OpenMeteoResponse {
        val hourly = response.hourly
        val daily = response.daily

        val hourlyIndexes = hourly?.time
            ?.mapIndexedNotNull { index, time ->
                val date = LocalDate.parse(time.take(10))
                if (date in startDate..endDate) index else null
            }
            ?: emptyList()

        val dailyIndexes = daily?.time
            ?.mapIndexedNotNull { index, time ->
                val date = LocalDate.parse(time.take(10))
                if (date in startDate..endDate) index else null
            }
            ?: emptyList()

        return response.copy(
            hourly = hourly?.copy(
                time = hourlyIndexes.map { hourly.time[it] },
                temperature = hourlyIndexes.map { hourly.temperature[it] },
                precipitation = hourlyIndexes.map { hourly.precipitation.getOrElse(it) { 0.0 } },
                rain = hourlyIndexes.map { hourly.rain.getOrElse(it) { 0.0 } },
                snowfall = hourlyIndexes.map { hourly.snowfall.getOrElse(it) { 0.0 } },
                weatherCode = hourlyIndexes.map { hourly.weatherCode[it] },
                cloudCover = hourlyIndexes.map { hourly.cloudCover[it] },
                windSpeed = hourlyIndexes.map { hourly.windSpeed.getOrElse(it) { 0.0 } },
                windGusts = hourlyIndexes.map { hourly.windGusts.getOrElse(it) { 0.0 } },
                humidity = hourlyIndexes.map { hourly.humidity[it] },
                pressure = hourlyIndexes.map { hourly.pressure[it] },
                isDay = hourlyIndexes.map { hourly.isDay[it] }
            ),
            daily = daily?.copy(
                time = dailyIndexes.map { daily.time[it] },
                weatherCode = dailyIndexes.map { daily.weatherCode[it] },
                tempMax = dailyIndexes.map { daily.tempMax[it] },
                tempMin = dailyIndexes.map { daily.tempMin[it] },
                sunrise = dailyIndexes.map { daily.sunrise[it] },
                sunset = dailyIndexes.map { daily.sunset[it] }
            )
        )
    }

    private suspend fun getHistoricalForecastFromApi(
        city: ArchiveCity,
        monthKey: String
    ): OpenMeteoResponse {
        val yearMonth = YearMonth.parse(monthKey)

        val startDate = yearMonth.atDay(1)
        val yesterday = LocalDate.now().minusDays(1)
        val latestAvailableArchiveDate = LocalDate.now().minusDays(5)

        val requestedEndDate = minOf(
            yearMonth.atEndOfMonth(),
            yesterday
        )

        return when {
            requestedEndDate <= latestAvailableArchiveDate -> {
                getHistoricalForecastFromArchiveInChunks(
                    city = city,
                    startDate = startDate,
                    endDate = requestedEndDate
                )
            }

            startDate > latestAvailableArchiveDate -> {
                getRecentPastForecastForRange(
                    city = city,
                    startDate = startDate,
                    endDate = requestedEndDate
                )
            }

            else -> {
                val archivePart = getHistoricalForecastFromArchiveInChunks(
                    city = city,
                    startDate = startDate,
                    endDate = latestAvailableArchiveDate
                )

                val recentPart = getRecentPastForecastForRange(
                    city = city,
                    startDate = latestAvailableArchiveDate.plusDays(1),
                    endDate = requestedEndDate
                )

                mergeHistoricalResponses(listOf(archivePart, recentPart))
            }
        }
    }

    private suspend fun getHistoricalForecastFromArchiveInChunks(
        city: ArchiveCity,
        startDate: LocalDate,
        endDate: LocalDate
    ): OpenMeteoResponse {
        val responses = mutableListOf<OpenMeteoResponse>()

        var chunkStart = startDate

        while (!chunkStart.isAfter(endDate)) {
            val chunkEnd = minOf(chunkStart.plusDays(6), endDate)

            val response = archiveApi.archive(
                lat = city.latitude,
                lon = city.longitude,
                startDate = chunkStart.toString(),
                endDate = chunkEnd.toString(),
                timezone = city.timezone
            )

            responses.add(response)
            chunkStart = chunkEnd.plusDays(1)
        }

        return mergeHistoricalResponses(responses)
    }

    private fun mergeHistoricalResponses(
        responses: List<OpenMeteoResponse>
    ): OpenMeteoResponse {
        val first = responses.first()

        return OpenMeteoResponse(
            latitude = first.latitude,
            longitude = first.longitude,
            timezone = first.timezone,
            current = first.current,
            hourly = HourlyDto(
                time = responses.flatMap { it.hourly?.time ?: emptyList() },
                temperature = responses.flatMap { it.hourly?.temperature ?: emptyList() },
                precipitation = responses.flatMap { it.hourly?.precipitation ?: emptyList() },
                rain = responses.flatMap { it.hourly?.rain ?: emptyList() },
                snowfall = responses.flatMap { it.hourly?.snowfall ?: emptyList() },
                weatherCode = responses.flatMap { it.hourly?.weatherCode ?: emptyList() },
                cloudCover = responses.flatMap { it.hourly?.cloudCover ?: emptyList() },
                windSpeed = responses.flatMap { it.hourly?.windSpeed ?: emptyList() },
                windGusts = responses.flatMap { it.hourly?.windGusts ?: emptyList() },
                humidity = responses.flatMap { it.hourly?.humidity ?: emptyList() },
                pressure = responses.flatMap { it.hourly?.pressure ?: emptyList() },
                isDay = responses.flatMap { it.hourly?.isDay ?: emptyList() }
            ),
            daily = DailyDto(
                time = responses.flatMap { it.daily?.time ?: emptyList() },
                weatherCode = responses.flatMap { it.daily?.weatherCode ?: emptyList() },
                tempMax = responses.flatMap { it.daily?.tempMax ?: emptyList() },
                tempMin = responses.flatMap { it.daily?.tempMin ?: emptyList() },
                sunrise = responses.flatMap { it.daily?.sunrise ?: emptyList() },
                sunset = responses.flatMap { it.daily?.sunset ?: emptyList() }
            )
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
