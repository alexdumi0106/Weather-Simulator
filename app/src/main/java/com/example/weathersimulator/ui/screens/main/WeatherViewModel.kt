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
import java.time.LocalDate
import com.example.weathersimulator.data.remote.city.CityResultDto
import com.example.weathersimulator.data.local.city.FavoriteCityEntity
import com.example.weathersimulator.data.repository.CitySearchRepository
import com.example.weathersimulator.data.repository.FavoriteCityRepository
import java.time.ZonedDateTime
import com.example.weathersimulator.domain.weather.WeatherAlertEvaluator
import com.example.weathersimulator.notifications.WeatherNotifier
import android.app.Application
import com.example.weathersimulator.data.remote.weather.CurrentDto
import com.example.weathersimulator.data.remote.weather.HourlyDto
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan
import com.example.weathersimulator.data.local.ai.AiSettingsStore
import com.example.weathersimulator.domain.usecase.GenerateAiResponseUseCase
import com.example.weathersimulator.data.remote.ai.ClimateComparisonRequest
import com.example.weathersimulator.data.remote.ai.ClimateDaySummaryRequest
import com.example.weathersimulator.data.remote.ai.HistoricalDayDescriptionRequest
import com.example.weathersimulator.data.remote.ai.HistoricalHourlySnapshot
import com.example.weathersimulator.data.remote.ai.OutfitRecommendationRequest
import com.example.weathersimulator.data.remote.ai.WeatherStoryRequest

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val application: Application,
    private val weatherRepository: WeatherRepository,
    private val citySearchRepository: CitySearchRepository,
    private val favoriteCityRepository: FavoriteCityRepository,
    private val generateAiResponse: GenerateAiResponseUseCase,
    private val aiSettingsStore: AiSettingsStore
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state
    val archiveCities = weatherRepository.archiveCities

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
        observeFavorites()
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
                val response = applyStormDetection(weatherRepository.getForecast(lat, lon))
                lastFetchAtMs = System.currentTimeMillis()
                lastFetchLat = lat
                lastFetchLon = lon
                cachedResponse = response
                Log.d("WeatherDebug", "HOURLY RESPONSE = ${response.hourly}")
                Log.d("WeatherDebug", "HOURLY TIMES = ${response.hourly?.time}")
                Log.d("WeatherDebug", "HOURLY TEMPS = ${response.hourly?.temperature}")
                Log.d("WeatherDebug", "HOURLY CODES = ${response.hourly?.weatherCode}")
                applyResponse(response)
            } catch (e: Exception) {
                val friendlyMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Conexiunea cu serverul meteo a expirat. Reîncerc automat când se actualizează locația."
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

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val selectedCityName = _state.value.selectedArchiveCity
                val city = weatherRepository.getArchiveCity(selectedCityName)

                val selectedMonth = _state.value.selectedHistoryMonth ?: "2025-01"

                val response = preloadHistoricalData(selectedMonth)
                applyHistoricalResponse(response)

                val isUsingCsv =
                    city.csvFileName != null &&
                    _state.value.selectedArchiveSource == "CSV"

                val months = if (isUsingCsv) {
                    buildAvailableHistoryMonthsFromResponse(response)
                } else {
                    buildAvailableHistoryMonths()
                }

                val finalSelectedMonth = selectedMonth
                    .takeIf { key -> months.any { it.key == key } }
                    ?: months.firstOrNull()?.key

                _state.update {
                    it.copy(
                        isLoading = false,
                        isHistoryMode = true,
                        availableHistoryMonths = months,
                        selectedHistoryMonth = finalSelectedMonth
                    )
                }

                val shouldAutoSelectMonth =
                    city.csvFileName != null &&
                        _state.value.selectedArchiveSource == "CSV"

                if (shouldAutoSelectMonth && finalSelectedMonth != null) {
                    selectHistoryMonth(finalSelectedMonth)
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
        // Dezactivat pentru varianta mixtă CSV + API.
    }

    private suspend fun preloadHistoricalData(monthKey: String): OpenMeteoResponse {
        val selectedCity = _state.value.selectedArchiveCity

        val city = weatherRepository.archiveCities.firstOrNull { it.name == selectedCity }
            ?: weatherRepository.buildDynamicArchiveCity(
                name = selectedCity,
                latitude = _state.value.selectedArchiveLatitude,
                longitude = _state.value.selectedArchiveLongitude,
                timezone = _state.value.selectedArchiveTimezone
            )

        val cacheStillValid =
            historicalCachedResponse != null &&
            _state.value.selectedHistoryMonth == monthKey

        if (cacheStillValid) {
            return historicalCachedResponse!!
        }

        val shouldUseCsv =
            city.csvFileName != null &&
            _state.value.selectedArchiveSource == "CSV"

        if (shouldUseCsv) {
            val historicalDataset = withContext(Dispatchers.IO) {
                weatherRepository.getHistoricalDataset(city)
            }

            if (historicalDataset != null) {
                historicalDailyRowsByDate = historicalDataset.dailyRows.associateBy { it.time }
                historicalTimezone = historicalDataset.timezone
                historicalCsvUtcOffsetSeconds = historicalDataset.utcOffsetSeconds
            }
        } else {
            historicalDailyRowsByDate = emptyMap()
            historicalTimezone = "Europe/Bucharest"
            historicalCsvUtcOffsetSeconds = 10_800
        }

        val response = withContext(Dispatchers.IO) {
            weatherRepository.getHistoricalForecastForCity(
                city = city,
                monthKey = monthKey,
                source = _state.value.selectedArchiveSource
            )
        }

        val detectedResponse = applyStormDetection(response)

        historicalTimezone = response.timezone ?: city.timezone
        historicalCsvUtcOffsetSeconds = response.utcOffsetSeconds ?: historicalCsvUtcOffsetSeconds

        historicalCachedResponse = detectedResponse
        return detectedResponse
    }

    fun selectArchiveCity(cityName: String) {
        historicalCachedResponse = null
        historicalDailyRowsByDate = emptyMap()
        historicalWarmupJob?.cancel()
        historicalWarmupJob = null

        _state.update {
            it.copy(
                selectedArchiveCity = cityName,
                availableHistoryMonths = emptyList(),
                availableHistoryDays = emptyList(),
                selectedHistoryMonth = null,
                selectedHistoryDay = null,
                historicalHourlyForecast = emptyList(),
                historyMonthSummary = null,
                historyDaySummary = null,
                historicalDayAiDescription = null,
                historicalDayAiDescriptionError = null,
                climateComparison = null,
                climateComparisonError = null,
                archiveCitySearchQuery = "",
                archiveCitySearchResults = emptyList(),
                archiveCitySearchError = null,
                data = null,
                error = null
            )
        }

        val selectedCity = weatherRepository.getArchiveCity(cityName)

        if (selectedCity.csvFileName != null && _state.value.selectedArchiveSource == "CSV") {
            loadHistorical()
        } else {
            _state.update {
                it.copy(
                    availableHistoryMonths = buildAvailableHistoryMonths()
                )
            }
        }
    }

    fun selectArchiveSource(source: String) {
        historicalCachedResponse = null
        historicalDailyRowsByDate = emptyMap()

        _state.update {
            it.copy(
                selectedArchiveSource = source,
                selectedHistoryMonth = null,
                selectedHistoryDay = null,
                availableHistoryMonths = if (source == "API") buildAvailableHistoryMonths() else emptyList(),
                availableHistoryDays = emptyList(),
                historicalHourlyForecast = emptyList(),
                historyMonthSummary = null,
                historyDaySummary = null,
                historicalDayAiDescription = null,
                historicalDayAiDescriptionError = null,
                climateComparison = null,
                climateComparisonError = null,
                data = null,
                error = null
            )
        }

        if (source == "CSV") {
            loadHistorical()
        }
    }

    fun setHistoryMode(enabled: Boolean) {
        _state.update {
            it.copy(
                isHistoryMode = enabled,
                error = if (enabled) null else it.error
            )
        }
    }

    fun exitHistoryMode() {
        historicalCachedResponse = null
        historicalDailyRowsByDate = emptyMap()

        _state.update {
            it.copy(
                isHistoryMode = false,
                selectedHistoryMonth = null,
                selectedHistoryDay = null,
                historicalHourlyForecast = emptyList(),
                historyMonthSummary = null,
                historyDaySummary = null,
                availableHistoryDays = emptyList(),
                availableHistoryMonths = emptyList(),
                historicalDayAiDescription = null,
                historicalDayAiDescriptionError = null,
                climateComparison = null,
                climateComparisonError = null,
                error = null
            )
        }

        val currentWeather = cachedResponse
        val lat = lastFetchLat
        val lon = lastFetchLon

        if (currentWeather != null) {
            applyResponse(currentWeather)
        } else if (lat != null && lon != null) {
            load(lat, lon)
        }
    }

    private fun generateWeatherStory(response: OpenMeteoResponse) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    weatherStory = null,
                    isWeatherStoryLoading = true,
                    weatherStoryError = null
                )
            }

            try {
                val story = generateAiResponse.weatherStory(
                    request = buildWeatherStoryRequest(response),
                    serverUrl = aiSettingsStore.getServerUrl()
                )

                _state.update {
                    it.copy(
                        weatherStory = story.trim(),
                        isWeatherStoryLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("WeatherStory", "Failed to generate weather story", e)

                _state.update {
                    it.copy(
                        isWeatherStoryLoading = false,
                        weatherStoryError = "Nu am putut genera povestea vremii: ${e.localizedMessage ?: "eroare necunoscuta"}"
                    )
                }
            }
        }
    }

    private fun buildWeatherStoryRequest(response: OpenMeteoResponse): WeatherStoryRequest {
        val current = response.current
        val hourly = response.hourly
        val daily = response.daily

        return WeatherStoryRequest(
            temperature = current?.temperature,
            apparentTemperature = current?.apparentTemperature,
            humidity = current?.humidity,
            windSpeed = current?.windSpeed,
            pressure = current?.pressure,
            weatherCode = current?.weatherCode,
            cloudCover = current?.cloudCover,
            nextHours = hourly?.time?.take(8) ?: emptyList(),
            nextTemperatures = hourly?.temperature?.take(8) ?: emptyList(),
            nextPrecipitation = hourly?.precipitation?.take(8) ?: emptyList(),
            nextWindSpeed = hourly?.windSpeed?.take(8) ?: emptyList(),
            nextWeatherCodes = hourly?.weatherCode?.take(8) ?: emptyList(),
            dailyDates = daily?.time?.take(3) ?: emptyList(),
            dailyMaxTemperatures = daily?.tempMax?.take(3) ?: emptyList(),
            dailyMinTemperatures = daily?.tempMin?.take(3) ?: emptyList(),
            dailyWeatherCodes = daily?.weatherCode?.take(3) ?: emptyList()
        )
    }

    private fun applyResponse(response: OpenMeteoResponse) {
        val hourlyItems = mapHourlyForecast(response)
        val dailyItems = mapDailyForecast(response)
        generateWeatherStory(response)

        lastFetchAtMs = System.currentTimeMillis()
        lastFetchLat = response.latitude
        lastFetchLon = response.longitude
        cachedResponse = response

        val alerts = WeatherAlertEvaluator.evaluateMessages(response)
        WeatherNotifier.showWeatherMessages(application.applicationContext, alerts)

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
        val city = weatherRepository.getArchiveCity(_state.value.selectedArchiveCity)

        val isUsingApi =
            city.csvFileName == null ||
            _state.value.selectedArchiveSource == "API"

        if (isUsingApi) {
            historicalCachedResponse = null

            viewModelScope.launch {
                _state.update {
                    it.copy(
                        isLoading = true,
                        selectedHistoryMonth = monthKey,
                        selectedHistoryDay = null,
                        availableHistoryDays = emptyList(),
                        historicalHourlyForecast = emptyList(),
                        historyMonthSummary = null,
                        historyDaySummary = null,
                        historicalDayAiDescription = null,
                        historicalDayAiDescriptionError = null,
                        climateComparison = null,
                        climateComparisonError = null,
                        error = null
                    )
                }

                try {
                    val response = preloadHistoricalData(monthKey)
                    applyHistoricalResponse(response)
                    applySelectedMonthFromLoadedResponse(monthKey)
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load selected historical month"
                        )
                    }
                }
            }

            return
        }

        applySelectedMonthFromLoadedResponse(monthKey)
    }

    private fun applySelectedMonthFromLoadedResponse(monthKey: String) {
        val response = _state.value.data ?: return
        val hourly = response.hourly ?: return

        val size = minOf(
            hourly.time.size,
            hourly.temperature.size,
            hourly.humidity.size,
            hourly.pressure.size
        )

        if (size == 0) return

        val yesterday = LocalDate.now().minusDays(1)

        val monthRows = (0 until size)
            .filter { index ->
                val date = hourly.time[index].take(10)
                date.startsWith(monthKey) &&
                    (LocalDate.parse(date) <= yesterday)
            }

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
                isLoading = false,
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

        _state.update {
            it.copy(
                selectedHistoryDay = dayKey,
                historicalDayAiDescription = null,
                isHistoricalDayAiDescriptionLoading = false,
                historicalDayAiDescriptionError = null,
                climateComparison = null,
                isClimateComparisonLoading = false,
                climateComparisonError = null
            )
        }

        val hourlyItems = dayIndexes.map { index ->
            HourlyForecastItemUi(
                time = formatHour(hourly.time[index]),
                temperature = "${hourly.temperature[index].roundToInt()}°",
                weatherCode = calculateStormWeatherCode(
                    currentIndex = index,
                    hourly = hourly,
                    originalWeatherCode = hourly.weatherCode[index]
                ),
                isDay = hourly.isDay[index] == 1,
                cloudCover = hourly.cloudCover[index]
            )
        }

        val dayNightStats = computeDayNightStatsForIndexes(hourly, dayIndexes)
        val daySummary = buildHistoryDaySummary(
            dayKey = dayKey,
            indexes = dayIndexes,
            hourly = hourly,
            dayNightStats = dayNightStats,
            response = response
        )

        _state.update {
            it.copy(
                selectedHistoryDay = dayKey,
                historicalHourlyForecast = hourlyItems,
                historyDaySummary = daySummary,
                historicalDayAiDescription = null,
                isHistoricalDayAiDescriptionLoading = false,
                historicalDayAiDescriptionError = null,
                climateComparison = null,
                isClimateComparisonLoading = false,
                climateComparisonError = null
            )
        }
    }

    fun requestHistoricalDayAiDescription() {
        val dayKey = _state.value.selectedHistoryDay ?: return
        val summary = _state.value.historyDaySummary ?: return
        val hourlyItems = _state.value.historicalHourlyForecast

        generateHistoricalDayAiDescription(
            dayKey = dayKey,
            summary = summary,
            hourlyItems = hourlyItems
        )
    }

    fun requestClimateComparison() {
        val dayKey = _state.value.selectedHistoryDay ?: return
        val summary = _state.value.historyDaySummary ?: return

        loadClimateComparison(
            selectedDayKey = dayKey,
            selectedSummary = summary
        )
    }

    private fun generateHistoricalDayAiDescription(
        dayKey: String,
        summary: HistoryDaySummaryUi,
        hourlyItems: List<HourlyForecastItemUi>
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    historicalDayAiDescription = null,
                    isHistoricalDayAiDescriptionLoading = true,
                    historicalDayAiDescriptionError = null
                )
            }

            try {
                val text = generateAiResponse.historicalDayDescription(
                    request = buildHistoricalDayDescriptionRequest(summary, hourlyItems),
                    serverUrl = aiSettingsStore.getServerUrl()
                ).trim()

                if (text.startsWith("Backend error:", ignoreCase = true)) {
                    error("Serviciul AI este momentan ocupat. Reincearca peste cateva secunde.")
                }

                _state.update {
                    it.copy(
                        historicalDayAiDescription = text,
                        isHistoricalDayAiDescriptionLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isHistoricalDayAiDescriptionLoading = false,
                        historicalDayAiDescriptionError = "Nu am putut genera descrierea AI."
                    )
                }
            }
        }
    }

    private fun buildHistoricalDayDescriptionRequest(
        summary: HistoryDaySummaryUi,
        hourlyItems: List<HourlyForecastItemUi>
    ): HistoricalDayDescriptionRequest {
        val snapshots = hourlyItems
            .filter { it.time in listOf("07:00", "13:00", "19:00") }
            .ifEmpty { hourlyItems.take(3) }
            .map { item ->
                HistoricalHourlySnapshot(
                    time = item.time,
                    temperature = item.temperature,
                    weatherCode = item.weatherCode,
                    cloudCover = item.cloudCover
                )
            }

        return HistoricalDayDescriptionRequest(
            dateLabel = summary.dateLabel,
            maxTemperature = summary.maxTemperature,
            minTemperature = summary.minTemperature,
            averageHumidity = summary.averageHumidity,
            averagePressure = summary.averagePressure,
            sunrise = summary.sunrise,
            sunset = summary.sunset,
            hourlySnapshots = snapshots
        )
    }

    private fun resolveComparisonDate(selectedDate: LocalDate): LocalDate {
        val latestAvailableDate = LocalDate.now().minusDays(1)
        val candidate2026 = LocalDate.of(2026, selectedDate.month, selectedDate.dayOfMonth)

        return if (!candidate2026.isAfter(latestAvailableDate)) {
            candidate2026
        } else {
            LocalDate.of(2025, selectedDate.month, selectedDate.dayOfMonth)
        }
    }

    private fun loadClimateComparison(
        selectedDayKey: String,
        selectedSummary: HistoryDaySummaryUi
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    climateComparison = null,
                    isClimateComparisonLoading = true,
                    climateComparisonError = null
                )
            }

            try {
                val selectedDate = LocalDate.parse(selectedDayKey)
                val comparisonDate = resolveComparisonDate(selectedDate)
                val comparisonMonthKey = comparisonDate.toString().take(7)

                val selectedCity = weatherRepository.archiveCities.firstOrNull {
                    it.name == _state.value.selectedArchiveCity
                } ?: weatherRepository.buildDynamicArchiveCity(
                    name = _state.value.selectedArchiveCity,
                    latitude = _state.value.selectedArchiveLatitude,
                    longitude = _state.value.selectedArchiveLongitude,
                    timezone = _state.value.selectedArchiveTimezone
                )

                val comparisonResponse = withContext(Dispatchers.IO) {
                    weatherRepository.getHistoricalForecastForCity(
                        city = selectedCity,
                        monthKey = comparisonMonthKey,
                        source = "API"
                    )
                }

                val hourly = comparisonResponse.hourly ?: error("Nu există date orare pentru comparație.")
                val comparisonDayKey = comparisonDate.toString()

                val indexes = hourly.time.indices.filter {
                    hourly.time[it].startsWith(comparisonDayKey)
                }

                if (indexes.isEmpty()) {
                    error("Nu există date pentru ${formatHistoryDayLabel(comparisonDayKey)}.")
                }

                val comparisonSummary = buildHistoryDaySummary(
                    dayKey = comparisonDayKey,
                    indexes = indexes,
                    hourly = hourly,
                    dayNightStats = computeDailyDayNightStats(hourly)[comparisonDayKey],
                    response = comparisonResponse
                ) ?: error("Nu am putut calcula comparația.")

                val aiText = generateAiResponse.climateComparison(
                    request = buildClimateComparisonRequest(
                        selectedSummary = selectedSummary,
                        comparisonSummary = comparisonSummary
                    ),
                    serverUrl = aiSettingsStore.getServerUrl()
                ).trim()

                if (aiText.startsWith("Backend error:", ignoreCase = true)) {
                    error("Serviciul AI este momentan ocupat. Reincearca peste cateva secunde.")
                }

                _state.update {
                    it.copy(
                        isClimateComparisonLoading = false,
                        climateComparison = ClimateComparisonUi(
                            selectedDateLabel = selectedSummary.dateLabel,
                            comparisonDateLabel = comparisonSummary.dateLabel,
                            selectedMaxTemperature = selectedSummary.maxTemperature,
                            comparisonMaxTemperature = comparisonSummary.maxTemperature,
                            selectedMinTemperature = selectedSummary.minTemperature,
                            comparisonMinTemperature = comparisonSummary.minTemperature,
                            selectedAverageHumidity = selectedSummary.averageHumidity,
                            comparisonAverageHumidity = comparisonSummary.averageHumidity,
                            selectedAveragePressure = selectedSummary.averagePressure,
                            comparisonAveragePressure = comparisonSummary.averagePressure,
                            aiDescription = aiText
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isClimateComparisonLoading = false,
                        climateComparisonError = e.message ?: "Nu am putut încărca comparația climatică."
                    )
                }
            }
        }
    }

    private fun buildClimateComparisonRequest(
        selectedSummary: HistoryDaySummaryUi,
        comparisonSummary: HistoryDaySummaryUi
    ): ClimateComparisonRequest {
        return ClimateComparisonRequest(
            selectedDay = selectedSummary.toClimateDaySummaryRequest(),
            comparisonDay = comparisonSummary.toClimateDaySummaryRequest()
        )
    }

    private fun HistoryDaySummaryUi.toClimateDaySummaryRequest(): ClimateDaySummaryRequest {
        return ClimateDaySummaryRequest(
            dateLabel = dateLabel,
            maxTemperature = maxTemperature,
            minTemperature = minTemperature,
            averageHumidity = averageHumidity,
            averagePressure = averagePressure
        )
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
        val yesterday = LocalDate.now().minusDays(1)
        val endMonthKey = "%04d-%02d".format(
            yesterday.year,
            yesterday.monthValue
        )

        return buildHistoricalMonthRange(
            startMonthKey = "2012-01",
            endMonthKey = endMonthKey
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
        hourly: com.example.weathersimulator.data.remote.weather.HourlyDto,
        dayNightStats: DayNightStats?,
        response: OpenMeteoResponse
    ): HistoryDaySummaryUi? {
        if (indexes.isEmpty()) return null

        val temperatures = indexes.map { hourly.temperature[it] }
        val humidities = indexes.map { hourly.humidity[it] }
        val pressures = indexes.map { hourly.pressure[it] }
        val dayIndexes = indexes.filter { hourly.isDay[it] == 1 }
        val nightIndexes = indexes.filter { hourly.isDay[it] == 0 }
        val dayWeatherCodes = dayIndexes.map { hourly.weatherCode[it] }
        val dayCloudCovers = dayIndexes.map { hourly.cloudCover[it] }
        val nightWeatherCodes = nightIndexes.map { hourly.weatherCode[it] }
        val nightCloudCovers = nightIndexes.map { hourly.cloudCover[it] }
        val dailyRow = historicalDailyRowsByDate[dayKey]
        val daily = response.daily
        val dailyIndex = daily?.time?.indexOf(dayKey) ?: -1

        val sunriseFromCsv = dailyRow?.sunrise
        val sunsetFromCsv = dailyRow?.sunset

        val sunriseFromApi = if (dailyIndex >= 0) {
            daily?.sunrise?.getOrNull(dailyIndex)
        } else null

        val sunsetFromApi = if (dailyIndex >= 0) {
            daily?.sunset?.getOrNull(dailyIndex)
        } else null

        val calculatedSolarTimes = calculateHistoricalSunriseSunset(
            dayKey = dayKey,
            latitude = response.latitude,
            longitude = response.longitude,
            timezone = historicalTimezone
        )

        val sunrise = calculatedSolarTimes?.first
            ?: sunriseFromCsv?.let { normalizeHistoricalSolarTime(it) }
            ?: sunriseFromApi?.let { formatHour(it) }

        val sunset = calculatedSolarTimes?.second
            ?: sunsetFromCsv?.let { normalizeHistoricalSolarTime(it) }
            ?: sunsetFromApi?.let { formatHour(it) }

        val fallbackWeatherCode = hourly.weatherCode[indexes.firstOrNull() ?: 0]
        val fallbackCloudCover = hourly.cloudCover[indexes.firstOrNull() ?: 0]

        return HistoryDaySummaryUi(
            dateLabel = formatHistoryDayLabel(dayKey),
            maxTemperature = "${temperatures.maxOrNull()?.roundToInt() ?: 0}°",
            minTemperature = "${temperatures.minOrNull()?.roundToInt() ?: 0}°",
            dayWeatherCode = dayNightStats?.dayWeatherCode ?: dayWeatherCodes.withRainPriorityOr(fallbackWeatherCode),
            dayCloudCover = dayNightStats?.dayCloudCover ?: dayCloudCovers.averageIntOr(fallbackCloudCover),
            nightWeatherCode = dayNightStats?.nightWeatherCode ?: nightWeatherCodes.withRainPriorityOr(fallbackWeatherCode),
            nightCloudCover = dayNightStats?.nightCloudCover ?: nightCloudCovers.averageIntOr(fallbackCloudCover),
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

        val zoneId = try {
            ZoneId.of(response.timezone ?: "Europe/Bucharest")
        } catch (e: Exception) {
            ZoneId.systemDefault()
        }

        val currentHour = ZonedDateTime.now(zoneId)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))

        val startIndex = times.indexOfFirst { it == currentHour }.let { index ->
            if (index >= 0) index else 0
        }

        val endIndex = minOf(startIndex + 24, maxAvailable)

        return (startIndex until endIndex).map { index ->
            HourlyForecastItemUi(
                time = formatHour(times[index]),
                temperature = "${temperatures[index].roundToInt()}°",
                weatherCode = calculateStormWeatherCode(index, hourly, weatherCodes[index]),
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
                weatherCode = dayNightStats?.dayWeatherCode ?: weatherCodes[index],
                cloudCover = dailyCloudCover,
                isDay = true,
                dayWeatherCode = dayNightStats?.dayWeatherCode ?: weatherCodes[index],
                dayCloudCover = dayNightStats?.dayCloudCover ?: dailyCloudCover,
                dayMaxTemperature = "${(dayNightStats?.dayMaxTemperature ?: maxTemps[index]).roundToInt()}°",
                nightWeatherCode = dayNightStats?.nightWeatherCode ?: weatherCodes[index],
                nightCloudCover = dayNightStats?.nightCloudCover ?: dailyCloudCover,
                nightMinTemperature = "${(dayNightStats?.nightMinTemperature ?: minTemps[index]).roundToInt()}°"
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
            var weatherCode = hourly.weatherCode[index]
            
            // Apply storm detection logic to override weather code if storm is detected
            weatherCode = calculateStormWeatherCode(index, hourly, weatherCode)
            
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
                dayWeatherCode = value.dayCodes.withRainPriorityOr(fallbackCode),
                dayCloudCover = value.dayClouds.averageIntOr(fallbackCloud),
                dayMaxTemperature = value.dayTemperatures.maxOrNull() ?: fallbackTemp,
                nightWeatherCode = value.nightCodes.withRainPriorityOr(fallbackCode),
                nightCloudCover = value.nightClouds.averageIntOr(fallbackCloud),
                nightMinTemperature = value.nightTemperatures.minOrNull() ?: fallbackTemp
            )
        }
    }

    private fun computeDayNightStatsForIndexes(
        hourly: HourlyDto,
        indexes: List<Int>
    ): DayNightStats? {
        if (indexes.isEmpty()) return null

        val bucket = MutableDayNightStats()

        indexes.forEach { index ->
            val weatherCode = calculateStormWeatherCode(
                currentIndex = index,
                hourly = hourly,
                originalWeatherCode = hourly.weatherCode[index]
            )
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

        val fallbackCode = bucket.dayCodes.firstOrNull()
            ?: bucket.nightCodes.firstOrNull()
            ?: 0

        val fallbackCloud = bucket.dayClouds.firstOrNull()
            ?: bucket.nightClouds.firstOrNull()
            ?: 100

        val fallbackTemp = bucket.dayTemperatures.maxOrNull()
            ?: bucket.nightTemperatures.maxOrNull()
            ?: 0.0

        return DayNightStats(
            dayWeatherCode = bucket.dayCodes.withRainPriorityOr(fallbackCode),
            dayCloudCover = bucket.dayClouds.averageIntOr(fallbackCloud),
            dayMaxTemperature = bucket.dayTemperatures.maxOrNull() ?: fallbackTemp,
            nightWeatherCode = bucket.nightCodes.withRainPriorityOr(fallbackCode),
            nightCloudCover = bucket.nightClouds.averageIntOr(fallbackCloud),
            nightMinTemperature = bucket.nightTemperatures.minOrNull() ?: fallbackTemp
        )
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

    private fun calculateHistoricalSunriseSunset(
        dayKey: String,
        latitude: Double,
        longitude: Double,
        timezone: String?
    ): Pair<String, String>? {
        if (latitude == 0.0 && longitude == 0.0) return null

        return try {
            val date = LocalDate.parse(dayKey)
            val zoneId = ZoneId.of(timezone ?: "UTC")
            val sunriseMinutes = calculateSunEventMinutesForZone(
                date = date,
                zoneId = zoneId,
                latitude = latitude,
                longitude = longitude,
                isSunrise = true
            )

            val sunsetMinutes = calculateSunEventMinutesForZone(
                date = date,
                zoneId = zoneId,
                latitude = latitude,
                longitude = longitude,
                isSunrise = false
            )

            if (sunriseMinutes == null || sunsetMinutes == null) {
                null
            } else {
                minutesToClock(sunriseMinutes) to minutesToClock(sunsetMinutes)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateSunEventMinutesForZone(
        date: LocalDate,
        zoneId: ZoneId,
        latitude: Double,
        longitude: Double,
        isSunrise: Boolean
    ): Int? {
        val noonOffsetHours = date
            .atTime(12, 0)
            .atZone(zoneId)
            .offset
            .totalSeconds / 3600.0

        val firstPassMinutes = calculateSunEventMinutes(
            dayOfYear = date.dayOfYear,
            latitude = latitude,
            longitude = longitude,
            utcOffsetHours = noonOffsetHours,
            isSunrise = isSunrise
        ) ?: return null

        val firstPassTime = java.time.LocalTime.of(
            firstPassMinutes / 60,
            firstPassMinutes % 60
        )

        val eventOffsetHours = date
            .atTime(firstPassTime)
            .atZone(zoneId)
            .offset
            .totalSeconds / 3600.0

        return calculateSunEventMinutes(
            dayOfYear = date.dayOfYear,
            latitude = latitude,
            longitude = longitude,
            utcOffsetHours = eventOffsetHours,
            isSunrise = isSunrise
        )
    }

    private fun calculateSunEventMinutes(
        dayOfYear: Int,
        latitude: Double,
        longitude: Double,
        utcOffsetHours: Double,
        isSunrise: Boolean
    ): Int? {
        val lngHour = longitude / 15.0
        val approxTime = if (isSunrise) {
            dayOfYear + (6.0 - lngHour) / 24.0
        } else {
            dayOfYear + (18.0 - lngHour) / 24.0
        }

        val meanAnomaly = 0.9856 * approxTime - 3.289
        val trueLongitude = normalizeDegrees(
            meanAnomaly +
                1.916 * sin(Math.toRadians(meanAnomaly)) +
                0.020 * sin(Math.toRadians(2 * meanAnomaly)) +
                282.634
        )

        var rightAscension = Math.toDegrees(
            atan(0.91764 * tan(Math.toRadians(trueLongitude)))
        )
        rightAscension = normalizeDegrees(rightAscension)

        val lQuadrant = floor(trueLongitude / 90.0) * 90.0
        val raQuadrant = floor(rightAscension / 90.0) * 90.0
        rightAscension += lQuadrant - raQuadrant
        rightAscension /= 15.0

        val sinDec = 0.39782 * sin(Math.toRadians(trueLongitude))
        val cosDec = cos(asin(sinDec))

        val cosH = (
            cos(Math.toRadians(90.833)) -
                sinDec * sin(Math.toRadians(latitude))
            ) / (
            cosDec * cos(Math.toRadians(latitude))
            )

        if (cosH > 1 || cosH < -1) return null

        var hourAngle = if (isSunrise) {
            360.0 - Math.toDegrees(acos(cosH))
        } else {
            Math.toDegrees(acos(cosH))
        }

        hourAngle /= 15.0

        val localMeanTime = hourAngle + rightAscension - 0.06571 * approxTime - 6.622

        var utcHours = localMeanTime - lngHour
        while (utcHours < 0) utcHours += 24.0
        while (utcHours >= 24) utcHours -= 24.0

        val localHours = utcHours + utcOffsetHours
        val localMinutes = (localHours * 60.0).toInt()

        return ((localMinutes % 1440) + 1440) % 1440
    }

    private fun normalizeDegrees(value: Double): Double {
        var result = value % 360.0
        if (result < 0) result += 360.0
        return result
    }

    private fun minutesToClock(totalMinutes: Int): String {
        val normalized = ((totalMinutes % 1440) + 1440) % 1440
        val hour = normalized / 60
        val minute = normalized % 60
        return "%02d:%02d".format(hour, minute)
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
        val nightMinTemperature: Double
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

    private fun List<Int>.withRainPriorityOr(default: Int): Int {
        if (isEmpty()) return default

        val stormCodes = setOf(996, 997, 998)
        val detectedStorms = filter { it in stormCodes }

        if (detectedStorms.isNotEmpty()) {
            return when {
                detectedStorms.contains(998) -> 998
                detectedStorms.contains(997) -> 997
                else -> 996
            }
        }
        
        val snowCodes = setOf(71, 73, 75, 77, 85, 86)
        val rainCodes = setOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99)
        
        val snowyCodes = filter { it in snowCodes }
        val rainyCodes = filter { it in rainCodes }
        
        // If both snow and rain are present, check their frequency
        if (snowyCodes.isNotEmpty() && rainyCodes.isNotEmpty()) {
            val snowCount = snowyCodes.size
            val rainCount = rainyCodes.size
            
            // If frequencies are similar (difference less than 50% of the larger group), 
            // or both have significant occurrences (at least 2 hours each), use combined icon code 999
            val maxCount = maxOf(snowCount, rainCount)
            val minCount = minOf(snowCount, rainCount)
            val differencePercentage = if (maxCount > 0) (maxCount - minCount) * 100 / maxCount else 0
            
            if (differencePercentage <= 50 || (snowCount >= 2 && rainCount >= 2)) {
                return 999  // Special code for snow + rain combined
            }
            
            // If one is significantly more frequent, use that
            return if (snowCount > rainCount) snowyCodes.mostFrequentIntOr(default) 
                   else rainyCodes.mostFrequentIntOr(default)
        }
        
        // Only snow present
        if (snowyCodes.isNotEmpty()) {
            return snowyCodes.mostFrequentIntOr(default)
        }
        
        // Only rain present
        if (rainyCodes.isNotEmpty()) {
            return rainyCodes.mostFrequentIntOr(default)
        }
        
        return mostFrequentIntOr(default)
    }

    private fun applyStormDetection(response: OpenMeteoResponse): OpenMeteoResponse {
        val hourly = response.hourly ?: return response

        val size = minOf(
            hourly.time.size,
            hourly.temperature.size,
            hourly.weatherCode.size,
            hourly.cloudCover.size,
            hourly.humidity.size,
            hourly.pressure.size,
            hourly.isDay.size
        )

        if (size == 0) return response

        val detectedWeatherCodes = hourly.weatherCode.mapIndexed { index, originalCode ->
            if (index < size) {
                calculateStormWeatherCode(
                    currentIndex = index,
                    hourly = hourly,
                    originalWeatherCode = originalCode
                )
            } else {
                originalCode
            }
        }

        val detectedHourly = hourly.copy(
            weatherCode = detectedWeatherCodes
        )

        val current = response.current
        val detectedCurrent = current?.let {
            val currentIndex = hourly.time.indexOf(current.time)
                .takeIf { index -> index >= 0 }
                ?: 0

            it.copy(
                weatherCode = detectedWeatherCodes.getOrNull(currentIndex) ?: it.weatherCode
            )
        }

        return response.copy(
            current = detectedCurrent,
            hourly = detectedHourly
        )
    }

    private fun calculateStormWeatherCode(
        currentIndex: Int,
        hourly: com.example.weathersimulator.data.remote.weather.HourlyDto,
        originalWeatherCode: Int
    ): Int {
        if (currentIndex < 0 || currentIndex >= hourly.time.size) return originalWeatherCode

        when (originalWeatherCode) {
            96, 99 -> return 998
            95 -> return 997
        }

        val nearbyIndexes = (currentIndex - 1..currentIndex + 1)
            .filter { it >= 0 && it < hourly.time.size }

        val precipitation = nearbyIndexes.maxOfOrNull {
            hourly.precipitation.getOrNull(it) ?: 0.0
        } ?: 0.0

        val rain = nearbyIndexes.maxOfOrNull {
            hourly.rain.getOrNull(it) ?: 0.0
        } ?: 0.0

        val cloudCover = nearbyIndexes.maxOfOrNull {
            hourly.cloudCover.getOrNull(it) ?: 0
        } ?: 0

        val humidity = nearbyIndexes.maxOfOrNull {
            hourly.humidity.getOrNull(it) ?: 0
        } ?: 0

        val windGusts = nearbyIndexes.maxOfOrNull {
            hourly.windGusts.getOrNull(it) ?: 0.0
        } ?: 0.0

        val temperature = nearbyIndexes.maxOfOrNull {
            hourly.temperature.getOrNull(it) ?: 0.0
        } ?: 0.0

        val snow = nearbyIndexes.maxOfOrNull {
            hourly.snowfall.getOrNull(it) ?: 0.0
        } ?: 0.0

        val isColdWeather = temperature <= 2.0
        val hasSnowSignal = snow > 0.0 || originalWeatherCode in setOf(71, 73, 75, 77, 85, 86)
        val isFreezingRainCode = originalWeatherCode in setOf(56, 57, 66, 67)

        if (isColdWeather && hasSnowSignal) {
            return when {
                snow >= 1.0 || precipitation >= 2.0 -> 75
                else -> 73
            }
        }

        if (temperature <= 0.0 && !isFreezingRainCode) {
            return originalWeatherCode
        }

        val pressure = hourly.pressure.getOrNull(currentIndex) ?: 1013.0
        val pressure3HoursAgo = hourly.pressure.getOrNull(currentIndex - 3) ?: pressure
        val pressureDrop3h = pressure - pressure3HoursAgo

        val hasRain = rain >= 0.8 || precipitation >= 1.0
        val hasHeavyRain = rain >= 3.0 || precipitation >= 4.0
        val hasVeryHeavyRain = rain >= 5.0 || precipitation >= 6.0

        val isSevereStorm =
            temperature >= 1.0 &&
                cloudCover >= 95 &&
                humidity >= 85 &&
                pressure <= 1005 &&
                hasVeryHeavyRain &&
                windGusts >= 50

       val isStorm =
            temperature >= 1.0 &&
                cloudCover >= 85 &&
                humidity >= 75 &&
                pressure <= 1008 &&
                hasRain &&
                (windGusts >= 35 || pressureDrop3h <= -3.0 || hasHeavyRain)

        val isLikelyStorm =
            temperature >= 1.0 &&
                cloudCover >= 85 &&
                humidity >= 75 &&
                pressure <= 1010 &&
                pressureDrop3h <= -4.0 &&
                windGusts >= 35 &&
                hasRain

        val isSunStorm =
            cloudCover in 45..84 &&
                humidity >= 70 &&
                pressure <= 1010 &&
                (hasRain || pressureDrop3h <= -3.0) &&
                windGusts >= 30 &&
                temperature >= 1.0

        return when {
            isSevereStorm -> 998
            isStorm -> 997
            isLikelyStorm -> 997
            isSunStorm -> 996
            else -> originalWeatherCode
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteCityRepository.observeFavorites().collect { favorites ->
                _state.update {
                    it.copy(favoriteCities = favorites)
                }
            }
        }
    }

    private fun loadLastSelectedCity() {
        viewModelScope.launch {
            val lastCity = favoriteCityRepository.getLastSelectedCity()

            if (lastCity != null) {
                _state.update {
                    it.copy(
                        selectedCityName = lastCity.displayName,
                        isUsingCurrentLocation = false
                    )
                }

                load(lastCity.latitude, lastCity.longitude)
            }
        }
    }

    fun updateCitySearchQuery(query: String) {
        _state.update {
            it.copy(
                citySearchQuery = query,
                citySearchError = null
            )
        }
    }

    fun searchCity() {
        val query = _state.value.citySearchQuery.trim()
        if (query.length < 2) {
            _state.update {
                it.copy(citySearchError = "Introdu cel puțin 2 caractere.")
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSearchingCity = true,
                    citySearchError = null
                )
            }

            try {
                val results = citySearchRepository.searchCity(query)

                _state.update {
                    it.copy(
                        citySearchResults = results,
                        isSearchingCity = false,
                        citySearchError = if (results.isEmpty()) "Nu am găsit orașul căutat." else null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSearchingCity = false,
                        citySearchError = "Nu am putut căuta orașul. Verifică internetul."
                    )
                }
            }
        }
    }

    fun selectCity(city: CityResultDto) {
        val displayName = city.displayName()

        _state.update {
            it.copy(
                selectedCityName = displayName,
                isUsingCurrentLocation = false,
                citySearchResults = emptyList(),
                citySearchQuery = displayName,
                citySearchError = null
            )
        }

        viewModelScope.launch {
            favoriteCityRepository.saveLastSelected(
                FavoriteCityEntity(
                    id = "last_selected_city",
                    name = city.name,
                    displayName = displayName,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    country = city.country,
                    timezone = city.timezone,
                    isLastSelected = true
                )
            )
        }

        load(city.latitude, city.longitude)
    }

    fun useCurrentLocation() {
        _state.update {
            it.copy(
                selectedCityName = "Locația ta",
                isUsingCurrentLocation = true,
                citySearchQuery = "",
                citySearchResults = emptyList(),
                citySearchError = null
            )
        }

        lastFetchAtMs = 0L
        lastFetchLat = null
        lastFetchLon = null
    }

    fun saveSelectedCityToFavorites() {
        val state = _state.value
        val data = state.data ?: return
        val currentLat = lastFetchLat ?: return
        val currentLon = lastFetchLon ?: return

        viewModelScope.launch {
            favoriteCityRepository.saveFavorite(
                FavoriteCityEntity(
                    id = "${state.selectedCityName}_${currentLat}_${currentLon}",
                    name = state.selectedCityName.substringBefore(","),
                    displayName = state.selectedCityName,
                    latitude = currentLat,
                    longitude = currentLon,
                    country = null,
                    timezone = data.timezone
                )
            )
        }
    }

    fun selectFavoriteCity(city: FavoriteCityEntity) {
        _state.update {
            it.copy(
                selectedCityName = city.displayName,
                isUsingCurrentLocation = false,
                citySearchQuery = city.displayName,
                citySearchResults = emptyList()
            )
        }

        viewModelScope.launch {
            favoriteCityRepository.saveLastSelected(city)
        }

        load(city.latitude, city.longitude)
    }

    fun deleteFavoriteCity(city: FavoriteCityEntity) {
        viewModelScope.launch {
            favoriteCityRepository.deleteFavorite(city.id)
        }
    }

    fun updateArchiveCitySearchQuery(query: String) {
        _state.update {
            it.copy(
                archiveCitySearchQuery = query,
                archiveCitySearchError = null
            )
        }
    }

    fun searchArchiveCity() {
        val query = _state.value.archiveCitySearchQuery.trim()

        if (query.length < 2) {
            _state.update {
                it.copy(archiveCitySearchError = "Introdu cel puțin 2 caractere.")
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSearchingArchiveCity = true,
                    archiveCitySearchError = null
                )
            }

            try {
                val results = citySearchRepository.searchCity(query)

                _state.update {
                    it.copy(
                        archiveCitySearchResults = results,
                        isSearchingArchiveCity = false,
                        archiveCitySearchError = if (results.isEmpty()) "Nu am găsit orașul căutat." else null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSearchingArchiveCity = false,
                        archiveCitySearchError = "Nu am putut căuta orașul. Verifică internetul."
                    )
                }
            }
        }
    }

    fun selectArchiveCityFromSearch(city: CityResultDto) {
        val displayName = city.displayName()

        historicalCachedResponse = null
        historicalDailyRowsByDate = emptyMap()

        _state.update {
            it.copy(
                selectedArchiveCity = displayName,
                selectedArchiveSource = "API",
                selectedArchiveLatitude = city.latitude,
                selectedArchiveLongitude = city.longitude,
                selectedArchiveTimezone = city.timezone ?: "auto",
                archiveCitySearchQuery = displayName,
                archiveCitySearchResults = emptyList(),
                archiveCitySearchError = null,
                selectedHistoryMonth = null,
                selectedHistoryDay = null,
                availableHistoryMonths = buildAvailableHistoryMonths(),
                availableHistoryDays = emptyList(),
                historicalHourlyForecast = emptyList(),
                historyMonthSummary = null,
                historyDaySummary = null,
                historicalDayAiDescription = null,
                historicalDayAiDescriptionError = null,
                climateComparison = null,
                climateComparisonError = null,
                data = null,
                error = null
            )
        }
    }

    fun generateOutfitRecommendation(cityName: String) {
        val response = _state.value.data

        if (response?.current == null) {
            _state.update {
                it.copy(outfitRecommendationError = "Nu exista date meteo disponibile pentru recomandare.")
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    outfitRecommendation = null,
                    isOutfitRecommendationLoading = true,
                    outfitRecommendationError = null
                )
            }

            try {
                val text = withContext(Dispatchers.IO) {
                    generateAiResponse.outfitRecommendation(
                        request = buildOutfitRecommendationRequest(response, cityName),
                        serverUrl = aiSettingsStore.getServerUrl()
                    )
                }.trim()

                _state.update {
                    it.copy(
                        outfitRecommendation = text,
                        isOutfitRecommendationLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isOutfitRecommendationLoading = false,
                        outfitRecommendationError = "Nu am putut genera recomandarea de tinuta."
                    )
                }
            }
        }
    }

    private fun buildOutfitRecommendationRequest(
        response: OpenMeteoResponse,
        cityName: String
    ): OutfitRecommendationRequest {
        val current = response.current
        val hourly = response.hourly

        val currentIndex = current?.time
            ?.let { hourly?.time?.indexOf(it) }
            ?.takeIf { it >= 0 }
            ?: 0

        return OutfitRecommendationRequest(
            cityName = cityName,
            temperature = current?.temperature,
            apparentTemperature = current?.apparentTemperature,
            humidity = current?.humidity,
            windSpeed = current?.windSpeed,
            precipitationNextHours = hourly?.precipitation
                ?.drop(currentIndex)
                ?.take(8)
                ?: emptyList(),
            uvIndex = current?.uvIndex,
            momentOfDay = if (current?.isDay == 1) "zi" else "seara/noapte",
            nextHours = hourly?.time
                ?.drop(currentIndex)
                ?.take(8)
                ?: emptyList(),
            nextTemperatures = hourly?.temperature
                ?.drop(currentIndex)
                ?.take(8)
                ?: emptyList()
        )
    }
}
