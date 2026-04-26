package com.example.weathersimulator.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.weathersimulator.ui.weather.WeatherIconRules
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherHistoryDayScreen(
    state: WeatherUiState,
    onBackClick: () -> Unit,
    onDaySelected: (String) -> Unit
) {
    val selectedDayKey = state.selectedHistoryDay ?: state.availableHistoryDays.firstOrNull()?.key
    val selectedDate = remember(selectedDayKey) {
        selectedDayKey?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }
    val selectedMonth = remember(selectedDate) {
        selectedDate?.let { YearMonth.from(it) }
    }
    val availableDays = remember(state.availableHistoryDays) {
        state.availableHistoryDays.associateBy { it.key }
    }
    val monthCells = remember(selectedMonth) {
        selectedMonth?.let { buildMonthGrid(it) } ?: emptyList()
    }
    val monthTitle = selectedMonth
        ?.format(DateTimeFormatter.ofPattern("MMMM", Locale("ro")))
        ?.replaceFirstChar { it.uppercase() }

    Scaffold(
        containerColor = Color(0xFF10243A),
        topBar = {
            TopAppBar(
                title = { Text("Weather History Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0B1E33),
                            Color(0xFF102B45),
                            Color(0xFF0B1E33)
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF26384C).copy(alpha = 0.95f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Daily history",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Text(
                                text = "Alege ziua direct din calendarul lunii selectate.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF26384C).copy(alpha = 0.92f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Select day",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Selecteaza o zi!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )

                                Text(
                                    text = monthTitle ?: "Calendar",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { label ->
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    monthCells.chunked(7).forEach { week ->
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            week.forEach { date ->
                                                if (date == null) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(4.dp)
                                                            .size(44.dp)
                                                    )
                                                } else {
                                                    val key = date.toString()
                                                    val isAvailable = availableDays.containsKey(key)
                                                    val isSelected = key == selectedDayKey

                                                    Surface(
                                                        onClick = {
                                                            if (isAvailable) {
                                                                onDaySelected(key)
                                                            }
                                                        },
                                                        enabled = isAvailable,
                                                        color = when {
                                                            isSelected -> Color(0xFF5D8CC1)
                                                            isAvailable -> Color(0xFF34495E).copy(alpha = 0.85f)
                                                            else -> Color(0xFF223247).copy(alpha = 0.45f)
                                                        },
                                                        contentColor = when {
                                                            isSelected -> Color.White
                                                            isAvailable -> Color.White.copy(alpha = 0.9f)
                                                            else -> Color.White.copy(alpha = 0.35f)
                                                        },
                                                        tonalElevation = 0.dp,
                                                        shadowElevation = 0.dp,
                                                        shape = MaterialTheme.shapes.medium,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(4.dp)
                                                            .size(44.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = date.dayOfMonth.toString(),
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }

                state.historyDaySummary?.let { summary ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E344A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = summary.dateLabel,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    WeatherSummaryTemperatureColumn(
                                        title = "Zi",
                                        temperatureLabel = "Max temperature",
                                        temperature = summary.maxTemperature,
                                        iconRes = WeatherIconRules.resolve(
                                            weatherCode = summary.dayWeatherCode,
                                            isDay = true,
                                            cloudCover = summary.dayCloudCover
                                        ).iconRes,
                                        description = WeatherIconRules.resolve(
                                            weatherCode = summary.dayWeatherCode,
                                            isDay = true,
                                            cloudCover = summary.dayCloudCover
                                        ).label,
                                        modifier = Modifier.weight(1f),
                                        titleColor = Color.White.copy(alpha = 0.8f),
                                        textColor = Color.White
                                    )
                                    WeatherSummaryTemperatureColumn(
                                        title = "Noapte",
                                        temperatureLabel = "Min temperature",
                                        temperature = summary.minTemperature,
                                        iconRes = WeatherIconRules.resolve(
                                            weatherCode = summary.nightWeatherCode,
                                            isDay = false,
                                            cloudCover = summary.nightCloudCover
                                        ).iconRes,
                                        description = WeatherIconRules.resolve(
                                            weatherCode = summary.nightWeatherCode,
                                            isDay = false,
                                            cloudCover = summary.nightCloudCover
                                        ).label,
                                        modifier = Modifier.weight(1f),
                                        titleColor = Color.White.copy(alpha = 0.8f),
                                        textColor = Color.White
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Average humidity",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            summary.averageHumidity,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Average pressure",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            summary.averagePressure,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Sunrise",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            summary.sunrise ?: "-",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Sunset",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            summary.sunset ?: "-",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Hourly weather",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (state.historicalHourlyForecast.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Selectează o zi pentru a vedea detaliile pe ore.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6A7280)
                            )
                        }
                    }
                } else {
                    items(state.historicalHourlyForecast) { item ->
                        HistoricalHourRow(
                            item = item,
                            sunrise = state.historyDaySummary?.sunrise,
                            sunset = state.historyDaySummary?.sunset
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherSummaryTemperatureColumn(
    title: String,
    temperatureLabel: String,
    temperature: String,
    iconRes: Int,
    description: String,
    modifier: Modifier = Modifier,
    titleColor: Color = Color(0xFF6A7280),
    textColor: Color = Color.Unspecified
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = titleColor
        )

        Image(
            painter = painterResource(id = iconRes),
            contentDescription = description,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = temperatureLabel,
            style = MaterialTheme.typography.labelMedium,
            color = titleColor
        )

        Text(
            text = temperature,
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
    }
}

private fun buildMonthGrid(yearMonth: YearMonth): List<LocalDate?> {
    val firstDay = yearMonth.atDay(1)
    val leadingEmptyCells = when (firstDay.dayOfWeek) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }

    val cells = mutableListOf<LocalDate?>()
    repeat(leadingEmptyCells) {
        cells.add(null)
    }

    for (day in 1..yearMonth.lengthOfMonth()) {
        cells.add(yearMonth.atDay(day))
    }

    while (cells.size % 7 != 0) {
        cells.add(null)
    }

    return cells
}

@Composable
private fun HistoricalHourRow(
    item: HourlyForecastItemUi,
    sunrise: String?,
    sunset: String?
) {
    val isDay = resolveHistoricalIsDay(
        timeLabel = item.time,
        sunrise = sunrise,
        sunset = sunset,
        fallbackIsDay = item.isDay
    )
    val visual = WeatherIconRules.resolve(
        weatherCode = item.weatherCode,
        isDay = isDay,
        cloudCover = item.cloudCover
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E344A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.time,
                modifier = Modifier.width(64.dp),
                color = Color.White
            )

            Spacer(modifier = Modifier.size(8.dp))

            Image(
                painter = painterResource(id = visual.iconRes),
                contentDescription = visual.label,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.size(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = visual.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Text(
                    text = "Cloud cover: ${item.cloudCover}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }

            Text(
                text = item.temperature,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

private fun resolveHistoricalIsDay(
    timeLabel: String,
    sunrise: String?,
    sunset: String?,
    fallbackIsDay: Boolean
): Boolean {
    val hour = timeLabel.substringBefore(":").toIntOrNull() ?: return fallbackIsDay
    val sunriseHour = ceilHourFromTime(sunrise)
    val sunsetHour = ceilHourFromTime(sunset)

    if (sunriseHour == null || sunsetHour == null) {
        return fallbackIsDay
    }

    return hour >= sunriseHour && hour < sunsetHour
}

private fun ceilHourFromTime(value: String?): Int? {
    val parsed = value?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: return null
    return if (parsed.minute == 0 && parsed.second == 0) parsed.hour else parsed.hour + 1
}
