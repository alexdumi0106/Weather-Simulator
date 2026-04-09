package com.example.weathersimulator.ui.screens.main

import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse

data class WeatherUiState(
    val isLoading: Boolean = false,
    val data: OpenMeteoResponse? = null,
    val hourlyForecast: List<HourlyForecastItemUi> = emptyList(),
    val dailyForecast: List<DailyForecastItemUi> = emptyList(),
    val error: String? = null,

    // History mode
    val isHistoryMode: Boolean = false,
    val availableHistoryMonths: List<HistoryMonthUi> = emptyList(),
    val selectedHistoryMonth: String? = null,
    val availableHistoryDays: List<HistoryDayUi> = emptyList(),
    val selectedHistoryDay: String? = null,

    // History summaries
    val historyMonthSummary: HistoryMonthSummaryUi? = null,
    val historyDaySummary: HistoryDaySummaryUi? = null,
    val historicalDailyRowsByDate: Map<String, HistoryDailyRowUi> = emptyMap(),

    // Hourly rows for selected history day
    val historicalHourlyForecast: List<HourlyForecastItemUi> = emptyList()
)

data class DailyForecastItemUi(
    val dayLabel: String,
    val maxTemperature: String,
    val minTemperature: String,
    val weatherCode: Int,
    val cloudCover: Int,
    val isDay: Boolean = true
)

data class HourlyForecastItemUi(
    val time: String,
    val temperature: String,
    val weatherCode: Int,
    val isDay: Boolean,
    val cloudCover: Int
)

data class HistoryMonthUi(
    val key: String,   // ex: 2026-03
    val label: String  // ex: martie 2026
)

data class HistoryDayUi(
    val key: String,   // ex: 2026-03-01
    val label: String  // ex: 1 martie 2026
)

data class HistoryMonthSummaryUi(
    val monthLabel: String,
    val maxTemperature: String,
    val maxTemperatureDate: String,
    val minTemperature: String,
    val minTemperatureDate: String,
    val averageHumidity: String,
    val averagePressure: String
)

data class HistoryDaySummaryUi(
    val dateLabel: String,
    val maxTemperature: String,
    val minTemperature: String,
    val averageHumidity: String,
    val averagePressure: String,
    val sunrise: String?,
    val sunset: String?
)

data class HistoryDailyRowUi(
    val dateLabel: String,
    val sunrise: String?,
    val sunset: String?
)