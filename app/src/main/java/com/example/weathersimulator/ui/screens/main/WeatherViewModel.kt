package com.example.weathersimulator.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import com.example.weathersimulator.data.repository.WeatherRepository
import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse
import com.example.weathersimulator.data.local.weather.WeatherCsvDailyRow
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.util.Log
import kotlin.math.abs
import kotlin.math.roundToInt

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state

    private var lastFetchAtMs: Long = 0L
    private var lastFetchLat: Double? = null
    private var lastFetchLon: Double? = null
    private var cachedResponse: com.example.weathersimulator.data.remote.weather.OpenMeteoResponse? = null
    private var historicalCachedResponse: OpenMeteoResponse? = null
    private var historicalDailyRowsByDate: Map<String, WeatherCsvDailyRow> = emptyMap()
    private var historicalTimezone: String = "Europe/Bucharest"
    private var historicalCsvUtcOffsetSeconds: Int = 10_800
    private var historicalWarmupJob: Job? = null
    private val minRefreshIntervalMs = 60_000L
    private val minLocationDelta = 0.01

    init {
        warmUpHistoricalInBackground()
    }

    fun load(lat: Double, lon: Double) {
        if (_state.value.isHistoryMode) return
        if (_state.value.isLoading) return

        val now = System.currentTimeMillis()
        val previousLat = lastFetchLat
        val previousLon = lastFetchLon
        val closeToPreviousLocation =
            previousLat != null && previousLon != null &&
                abs(lat - previousLat) < minLocationDelta && abs(lon - previousLon) < minLocationDelta

        val recentlyFetched = (now - lastFetchAtMs) < minRefreshIntervalMs
        if (closeToPreviousLocation && recentlyFetched) {
            val cached = cachedResponse
            if (cached != null) {
                val hourlyItems = mapHourlyForecast(cached)
                val dailyItems = mapDailyForecast(cached)
                _state.update {
                    it.copy(
                        isLoading = false,
                        data = cached,
                        hourlyForecast = hourlyItems,
                        dailyForecast = dailyItems,
                        error = null
                    )
                }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val response = weatherRepository.getForecast(lat, lon)
                Log.d("WeatherDebug", "HOURLY RESPONSE = ${response.hourly}")
                Log.d("WeatherDebug", "HOURLY TIMES = ${response.hourly?.time}")
                Log.d("WeatherDebug", "HOURLY TEMPS = ${response.hourly?.temperature}")
                Log.d("WeatherDebug", "HOURLY CODES = ${response.hourly?.weatherCode}")
                applyResponse(response)
            } catch (e: Exception) {
                val friendlyMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Conexiunea cu serverul meteo a expirat. Reincerc automat cand se actualizeaza locatia."
                    else -> e.message ?: "Failed to load weather"
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = friendlyMessage
                    )
                }
            }
        }
    }

    fun loadHistorical() {
        if (_state.value.isLoading) return

        warmUpHistoricalInBackground()

        viewModelScope.launch {
            val hasHistoricalCacheReady =
                historicalCachedResponse != null && historicalDailyRowsByDate.isNotEmpty()

            if (!hasHistoricalCacheReady) {
                _state.update { it.copy(isLoading = true, error = null) }
            } else {
                _state.update { it.copy(error = null) }
            }

            try {
                historicalWarmupJob?.join()
                val response = preloadHistoricalData()

                applyHistoricalResponse(response)

                val months = buildAvailableHistoryMonthsFromResponse(response)
                val preferredMonth = _state.value.selectedHistoryMonth
                val selectedMonth = preferredMonth
                    ?.takeIf { key -> months.any { it.key == key } }
                    ?: months.firstOrNull()?.key

                _state.update {
                    it.copy(
                        isLoading = false,
                        isHistoryMode = true,
                        availableHistoryMonths = months,
                        selectedHistoryMonth = selectedMonth
                    )
                }

                if (selectedMonth != null) {
                    selectHistoryMonth(selectedMonth)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load historical weather"
                    )
                }
            }
        }
    }

    private fun warmUpHistoricalInBackground() {
        if (historicalWarmupJob?.isActive == true) return
        if (historicalCachedResponse != null && historicalDailyRowsByDate.isNotEmpty()) return

        historicalWarmupJob = viewModelScope.launch(Dispatchers.Default) {
            runCatching { preloadHistoricalData() }
                .onFailure { error ->
                    Log.w("WeatherViewModel", "Historical preload failed: ${error.message}")
                }
        }
    }

    private suspend fun preloadHistoricalData(): OpenMeteoResponse {
        val cached = historicalCachedResponse
        if (cached != null && historicalDailyRowsByDate.isNotEmpty()) {
            return cached
        }

        val historicalDataset = withContext(Dispatchers.IO) {
            weatherRepository.getHistoricalDataset()
        }

        historicalDailyRowsByDate = historicalDataset.dailyRows.associateBy { it.time }
        historicalTimezone = historicalDataset.timezone
        historicalCsvUtcOffsetSeconds = historicalDataset.utcOffsetSeconds

        val response = withContext(Dispatchers.IO) {
            weatherRepository.getHistoricalForecast(historicalDataset)
        }

        historicalCachedResponse = response
        return response
    }

    fun setHistoryMode(enabled: Boolean) {
        _state.update {
            it.copy(
                isHistoryMode = enabled,
                error = if (enabled) null else it.error
            )
        }
    }

    private fun applyResponse(response: OpenMeteoResponse) {
        val hourlyItems = mapHourlyForecast(response)
        val dailyItems = mapDailyForecast(response)

        lastFetchAtMs = System.currentTimeMillis()
        lastFetchLat = response.latitude
        lastFetchLon = response.longitude
        cachedResponse = response

        _state.update {
            it.copy(
                isLoading = false,
                data = response,
                hourlyForecast = hourlyItems,
                dailyForecast = dailyItems,
                error = null
            )
        }
    }

    private fun applyHistoricalResponse(response: OpenMeteoResponse) {
        _state.update {
            it.copy(
                isLoading = false,
                data = response,
                error = null,
                hourlyForecast = emptyList(),
                dailyForecast = emptyList()
            )
        }
    }

    fun selectHistoryMonth(monthKey: String) {
        val response = _state.value.data ?: return
        val hourly = response.hourly ?: return

        val size = minOf(
            hourly.time.size,
            hourly.temperature.size,
            hourly.humidity.size,
            hourly.pressure.size
        )

        if (size == 0) return

        val monthRows = (0 until size)
            .filter { hourly.time[it].startsWith(monthKey) }

        val days = monthRows
            .map { hourly.time[it].take(10) }
            .distinct()
            .sorted()
            .map { date ->
                HistoryDayUi(
                    key = date,
                    label = formatHistoryDayLabel(date)
                )
            }

        val monthSummary = buildHistoryMonthSummary(monthKey, monthRows, hourly)

        val firstDay = days.firstOrNull()?.key

        _state.update {
            it.copy(
                selectedHistoryMonth = monthKey,
                availableHistoryDays = days,
                selectedHistoryDay = firstDay,
                historyMonthSummary = monthSummary,
                historicalHourlyForecast = emptyList(),
                historyDaySummary = null
            )
        }

        if (firstDay != null) {
            selectHistoryDay(firstDay)
        }
    }

    fun selectHistoryDay(dayKey: String) {
        val response = _state.value.data ?: return
        val hourly = response.hourly ?: return

        val size = minOf(
            hourly.time.size,
            hourly.temperature.size,
            hourly.weatherCode.size,
            hourly.isDay.size,
            hourly.cloudCover.size,
            hourly.humidity.size,
            hourly.pressure.size
        )

        if (size == 0) return

        val dayIndexes = (0 until size)
            .filter { hourly.time[it].startsWith(dayKey) }

        val hourlyItems = dayIndexes.map { index ->
            HourlyForecastItemUi(
                time = formatHour(hourly.time[index]),
                temperature = "${hourly.temperature[index].roundToInt()}°",
                weatherCode = hourly.weatherCode[index],
                isDay = hourly.isDay[index] == 1,
                cloudCover = hourly.cloudCover[index]
            )
        }

        val daySummary = buildHistoryDaySummary(dayKey, dayIndexes, hourly)

        _state.update {
            it.copy(
                selectedHistoryDay = dayKey,
                historicalHourlyForecast = hourlyItems,
                historyDaySummary = daySummary
            )
        }
    }

    private fun buildAvailableHistoryMonthsFromResponse(
        response: OpenMeteoResponse
    ): List<HistoryMonthUi> {
        val hourlyTimes = response.hourly?.time ?: return emptyList()

        return hourlyTimes
            .asSequence()
            .map { it.take(7) }
            .filter { it.length == 7 && it[4] == '-' }
            .distinct()
            .sortedDescending()
            .map { monthKey ->
                HistoryMonthUi(
                    key = monthKey,
                    label = formatHistoryMonthLabel(monthKey)
                )
            }
            .toList()
    }

    private fun buildAvailableHistoryMonths(): List<HistoryMonthUi> {
        return buildHistoricalMonthRange(
            startMonthKey = "2012-01",
            endMonthKey = "2025-12"
        )
    }

    private fun buildHistoricalMonthRange(
        startMonthKey: String,
        endMonthKey: String
    ): List<HistoryMonthUi> {
        val startParts = startMonthKey.split("-")
        val endParts = endMonthKey.split("-")

        if (startParts.size != 2 || endParts.size != 2) return emptyList()

        val startYear = startParts[0].toIntOrNull() ?: return emptyList()
        val startMonth = startParts[1].toIntOrNull() ?: return emptyList()
        val endYear = endParts[0].toIntOrNull() ?: return emptyList()
        val endMonth = endParts[1].toIntOrNull() ?: return emptyList()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, startYear)
            set(Calendar.MONTH, startMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, endYear)
            set(Calendar.MONTH, endMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val months = mutableListOf<HistoryMonthUi>()
        while (!calendar.after(endCalendar)) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val monthKey = "%04d-%02d".format(year, month)

            months.add(
                HistoryMonthUi(
                    key = monthKey,
                    label = formatHistoryMonthLabel(monthKey)
                )
            )

            calendar.add(Calendar.MONTH, 1)
        }

        return months.reversed()
    }

    private fun formatHistoryMonthLabel(monthKey: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMMM", java.util.Locale("ro"))
            val date = inputFormat.parse(monthKey)
            val formatted = outputFormat.format(date ?: return monthKey)
            formatted.replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            monthKey
        }
    }

    private fun formatHistoryMonthName(monthKey: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMMM", java.util.Locale("ro"))
            val date = inputFormat.parse(monthKey)
            val formatted = outputFormat.format(date ?: return monthKey)
            formatted.replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            monthKey
        }
    }

    private fun formatHistoryDayLabelShort(dayKey: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("d MMMM", java.util.Locale("ro"))
            val date = inputFormat.parse(dayKey)
            outputFormat.format(date ?: return dayKey)
        } catch (e: Exception) {
            dayKey
        }
    }

    private fun formatHistoryDayLabel(dayKey: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("ro"))
            val date = inputFormat.parse(dayKey)
            outputFormat.format(date ?: return dayKey)
        } catch (e: Exception) {
            dayKey
        }
    }

    private fun buildHistoryMonthSummary(
        monthKey: String,
        indexes: List<Int>,
        hourly: com.example.weathersimulator.data.remote.weather.HourlyDto
    ): HistoryMonthSummaryUi? {
        if (indexes.isEmpty()) return null

        val temperatures = indexes.map { hourly.temperature[it] }
        val humidities = indexes.map { hourly.humidity[it] }
        val pressures = indexes.map { hourly.pressure[it] }
        val maxTemperatureValue = temperatures.maxOrNull() ?: return null
        val minTemperatureValue = temperatures.minOrNull() ?: return null
        val maxTemperatureIndex = indexes.firstOrNull { hourly.temperature[it] == maxTemperatureValue } ?: indexes.first()
        val minTemperatureIndex = indexes.firstOrNull { hourly.temperature[it] == minTemperatureValue } ?: indexes.first()

        return HistoryMonthSummaryUi(
            monthLabel = formatHistoryMonthName(monthKey),
            maxTemperature = "${maxTemperatureValue.roundToInt()}°",
            maxTemperatureDate = formatHistoryDayLabelShort(hourly.time[maxTemperatureIndex].take(10)),
            minTemperature = "${minTemperatureValue.roundToInt()}°",
            minTemperatureDate = formatHistoryDayLabelShort(hourly.time[minTemperatureIndex].take(10)),
            averageHumidity = "${humidities.average().toInt()}%",
            averagePressure = "${pressures.average().toInt()} hPa"
        )
    }

    private fun buildHistoryDaySummary(
        dayKey: String,
        indexes: List<Int>,
        hourly: com.example.weathersimulator.data.remote.weather.HourlyDto
    ): HistoryDaySummaryUi? {
        if (indexes.isEmpty()) return null

        val temperatures = indexes.map { hourly.temperature[it] }
        val humidities = indexes.map { hourly.humidity[it] }
        val pressures = indexes.map { hourly.pressure[it] }
        val dailyRow = historicalDailyRowsByDate[dayKey]

        val sunrise = dailyRow?.sunrise?.let { normalizeHistoricalSolarTime(it) }
        val sunset = dailyRow?.sunset?.let { normalizeHistoricalSolarTime(it) }

        return HistoryDaySummaryUi(
            dateLabel = formatHistoryDayLabel(dayKey),
            maxTemperature = "${temperatures.maxOrNull()?.roundToInt() ?: 0}°",
            minTemperature = "${temperatures.minOrNull()?.roundToInt() ?: 0}°",
            averageHumidity = "${humidities.average().toInt()}%",
            averagePressure = "${pressures.average().toInt()} hPa",
            sunrise = sunrise,
            sunset = sunset
        )
    }

    private fun mapHourlyForecast(response: OpenMeteoResponse): List<HourlyForecastItemUi> {
        val hourly = response.hourly ?: return emptyList()

        val times = hourly.time
        val temperatures = hourly.temperature
        val weatherCodes = hourly.weatherCode
        val isDayList = hourly.isDay
        val cloudCoverList = hourly.cloudCover

        val maxAvailable = minOf(
            times.size,
            temperatures.size,
            weatherCodes.size,
            isDayList.size,
            cloudCoverList.size
        )

        if (maxAvailable == 0) return emptyList()

        val currentHour = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:00", java.util.Locale.getDefault())
            .format(java.util.Date())

        val startIndex = times.indexOfFirst { it == currentHour }.let { index ->
            if (index >= 0) index else 0
        }

        val endIndex = minOf(startIndex + 24, maxAvailable)

        return (startIndex until endIndex).map { index ->
            HourlyForecastItemUi(
                time = formatHour(times[index]),
                temperature = "${temperatures[index].roundToInt()}°",
                weatherCode = weatherCodes[index],
                isDay = isDayList[index] == 1,
                cloudCover = cloudCoverList[index]
            )
        }
    }

    private fun mapDailyForecast(
        response: OpenMeteoResponse
    ): List<DailyForecastItemUi> {
        val daily = response.daily ?: return emptyList()

        val times = daily.time
        val maxTemps = daily.tempMax
        val minTemps = daily.tempMin
        val weatherCodes = daily.weatherCode
        val hourly = response.hourly
        val dailyCloudCoverByDate = hourly?.let { computeDailyCloudCover(it.time, it.cloudCover) } ?: emptyMap()
        val dailyDayNightStatsByDate = hourly?.let { computeDailyDayNightStats(it) } ?: emptyMap()

        val size = minOf(times.size, maxTemps.size, minTemps.size, weatherCodes.size, 10)

        return (0 until size).map { index ->
            val dateKey = times[index]
            val dayNightStats = dailyDayNightStatsByDate[dateKey]
            val dailyCloudCover = dailyCloudCoverByDate[dateKey] ?: 100

            DailyForecastItemUi(
                dateKey = dateKey,
                dayLabel = formatDayLabel(dateKey, index),
                maxTemperature = "${maxTemps[index].roundToInt()}°",
                minTemperature = "${minTemps[index].roundToInt()}°",
                weatherCode = weatherCodes[index],
                cloudCover = dailyCloudCover,
                isDay = true,
                dayWeatherCode = dayNightStats?.dayWeatherCode ?: weatherCodes[index],
                dayCloudCover = dayNightStats?.dayCloudCover ?: dailyCloudCover,
                dayMaxTemperature = "${(dayNightStats?.dayMaxTemperature ?: maxTemps[index]).roundToInt()}°",
                nightWeatherCode = dayNightStats?.nightWeatherCode ?: weatherCodes[index],
                nightCloudCover = dayNightStats?.nightCloudCover ?: dailyCloudCover,
                nightMaxTemperature = "${(dayNightStats?.nightMaxTemperature ?: minTemps[index]).roundToInt()}°"
            )
        }
    }

    private fun computeDailyDayNightStats(
        hourly: com.example.weathersimulator.data.remote.weather.HourlyDto
    ): Map<String, DayNightStats> {
        val size = minOf(
            hourly.time.size,
            hourly.temperature.size,
            hourly.weatherCode.size,
            hourly.cloudCover.size,
            hourly.isDay.size
        )
        if (size == 0) return emptyMap()

        val grouped = mutableMapOf<String, MutableDayNightStats>()
        for (index in 0 until size) {
            val dateKey = hourly.time[index].take(10)
            val bucket = grouped.getOrPut(dateKey) { MutableDayNightStats() }
            val weatherCode = hourly.weatherCode[index]
            val cloudCover = hourly.cloudCover[index]
            val temperature = hourly.temperature[index]

            if (hourly.isDay[index] == 1) {
                bucket.dayCodes.add(weatherCode)
                bucket.dayClouds.add(cloudCover)
                bucket.dayTemperatures.add(temperature)
            } else {
                bucket.nightCodes.add(weatherCode)
                bucket.nightClouds.add(cloudCover)
                bucket.nightTemperatures.add(temperature)
            }
        }

        return grouped.mapValues { (_, value) ->
            val fallbackCode = value.dayCodes.firstOrNull()
                ?: value.nightCodes.firstOrNull()
                ?: 0
            val fallbackCloud = value.dayClouds.firstOrNull()
                ?: value.nightClouds.firstOrNull()
                ?: 100
            val fallbackTemp = value.dayTemperatures.maxOrNull()
                ?: value.nightTemperatures.maxOrNull()
                ?: 0.0

            DayNightStats(
                dayWeatherCode = value.dayCodes.mostFrequentIntOr(fallbackCode),
                dayCloudCover = value.dayClouds.averageIntOr(fallbackCloud),
                dayMaxTemperature = value.dayTemperatures.maxOrNull() ?: fallbackTemp,
                nightWeatherCode = value.nightCodes.mostFrequentIntOr(fallbackCode),
                nightCloudCover = value.nightClouds.averageIntOr(fallbackCloud),
                nightMaxTemperature = value.nightTemperatures.maxOrNull() ?: fallbackTemp
            )
        }
    }

    private fun computeDailyCloudCover(
        hourlyTimes: List<String>,
        hourlyCloudCover: List<Int>
    ): Map<String, Int> {
        val size = minOf(hourlyTimes.size, hourlyCloudCover.size)
        if (size == 0) return emptyMap()

        val grouped = mutableMapOf<String, MutableList<Int>>()
        for (index in 0 until size) {
            val date = hourlyTimes[index].take(10)
            grouped.getOrPut(date) { mutableListOf() }.add(hourlyCloudCover[index])
        }

        return grouped.mapValues { (_, values) ->
            if (values.isEmpty()) 100 else values.average().toInt()
        }
    }

    private fun formatDayLabel(date: String, index: Int): String {
        return try {
            if (index == 0) return "Astăzi"
            if (index == 1) return "Mâine"

            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale("ro"))

            val parsedDate = inputFormat.parse(date)
            val day = outputFormat.format(parsedDate ?: return date)

            day.replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            date
        }
    }

    private fun formatHour(dateTime: String): String {
        return try {
            if (dateTime.length >= 16) {
                dateTime.substring(11, 16)
            } else {
                dateTime
            }
        } catch (e: Exception) {
            dateTime
        }
    }

    private fun normalizeHistoricalSolarTime(dateTime: String): String {
        return try {
            val parsed = LocalDateTime.parse(dateTime)
            val sourceOffset = ZoneOffset.ofTotalSeconds(historicalCsvUtcOffsetSeconds)
            val sourceInstant = parsed.toInstant(sourceOffset)
            val targetZone = runCatching { ZoneId.of(historicalTimezone) }
                .getOrElse { ZoneId.systemDefault() }

            sourceInstant.atZone(targetZone)
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            formatHour(dateTime)
        }
    }

    private data class MutableDayNightStats(
        val dayCodes: MutableList<Int> = mutableListOf(),
        val dayClouds: MutableList<Int> = mutableListOf(),
        val dayTemperatures: MutableList<Double> = mutableListOf(),
        val nightCodes: MutableList<Int> = mutableListOf(),
        val nightClouds: MutableList<Int> = mutableListOf(),
        val nightTemperatures: MutableList<Double> = mutableListOf()
    )

    private data class DayNightStats(
        val dayWeatherCode: Int,
        val dayCloudCover: Int,
        val dayMaxTemperature: Double,
        val nightWeatherCode: Int,
        val nightCloudCover: Int,
        val nightMaxTemperature: Double
    )

    private fun List<Int>.mostFrequentIntOr(default: Int): Int {
        if (isEmpty()) return default
        return groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: default
    }

    private fun List<Int>.averageIntOr(default: Int): Int {
        if (isEmpty()) return default
        return average().roundToInt()
    }
}