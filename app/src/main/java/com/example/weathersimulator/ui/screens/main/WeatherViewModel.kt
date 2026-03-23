package com.example.weathersimulator.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersimulator.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state

    fun load(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val response = weatherRepository.getForecast(lat, lon)
                Log.d("WeatherDebug", "HOURLY RESPONSE = ${response.hourly}")
                Log.d("WeatherDebug", "HOURLY TIMES = ${response.hourly?.time}")
                Log.d("WeatherDebug", "HOURLY TEMPS = ${response.hourly?.temperature}")
                Log.d("WeatherDebug", "HOURLY CODES = ${response.hourly?.weatherCode}")
                val hourlyItems = mapHourlyForecast(response)
                val dailyItems = mapDailyForecast(response)
                _state.update {
                    it.copy(
                        isLoading = false,
                        data = response,
                        hourlyForecast = hourlyItems,
                        dailyForecast = dailyItems,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load weather"
                    )
                }
            }
        }
    }

    private fun mapHourlyForecast(response: com.example.weathersimulator.data.remote.weather.OpenMeteoResponse): List<HourlyForecastItemUi> {
        val hourly = response.hourly ?: return emptyList()

        val times = hourly.time
        val temperatures = hourly.temperature
        val weatherCodes = hourly.weatherCode
        val isDayList = hourly.isDay

        val maxAvailable = minOf(
            times.size,
            temperatures.size,
            weatherCodes.size,
            isDayList.size
        )

        if (maxAvailable == 0) return emptyList()

        val currentHour = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:00", java.util.Locale.getDefault())
            .format(java.util.Date())

        val startIndex = times.indexOfFirst { it == currentHour }.let { index ->
            if (index >= 0) index else 0
        }

        val endIndex = minOf(startIndex + 12, maxAvailable)

        return (startIndex until endIndex).map { index ->
            HourlyForecastItemUi(
                time = formatHour(times[index]),
                temperature = "${temperatures[index].toInt()}°",
                weatherCode = weatherCodes[index],
                isDay = isDayList[index] == 1
            )
        }
    }

    private fun mapDailyForecast(
        response: com.example.weathersimulator.data.remote.weather.OpenMeteoResponse
    ): List<DailyForecastItemUi> {
        val daily = response.daily ?: return emptyList()

        val times = daily.time
        val maxTemps = daily.tempMax
        val minTemps = daily.tempMin
        val weatherCodes = daily.weatherCode

        val size = minOf(times.size, maxTemps.size, minTemps.size, weatherCodes.size, 15)

        return (0 until size).map { index ->
            DailyForecastItemUi(
                dayLabel = formatDayLabel(times[index], index),
                maxTemperature = "${maxTemps[index].toInt()}°",
                minTemperature = "${minTemps[index].toInt()}°",
                weatherCode = weatherCodes[index]
            )
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
}