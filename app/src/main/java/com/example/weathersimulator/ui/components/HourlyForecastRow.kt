package com.example.weathersimulator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weathersimulator.ui.screens.main.HourlyForecastItemUi

@Composable
fun HourlyForecastRow(
    items: List<HourlyForecastItemUi>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Hourly forecast",
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(items) { item ->
                HourlyForecastCard(item = item)
            }
        }
    }
}

@Composable
private fun HourlyForecastCard(item: HourlyForecastItemUi) {
    Column(
        modifier = Modifier
            .width(78.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = item.time,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = weatherCodeToEmoji(item.weatherCode, item.isDay),
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = item.temperature,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun weatherCodeToEmoji(code: Int, isDay: Boolean): String {
    return when (code) {
        0 -> if (isDay) "☀" else "🌙"
        1, 2 -> if (isDay) "🌤" else "🌙☁"
        3 -> if (isDay) "☁" else "☁🌙"
        45, 48 -> "🌫"
        51, 53, 55, 56, 57 -> if (isDay) "🌦" else "🌧🌙"
        61, 63, 65, 66, 67 -> "🌧"
        71, 73, 75, 77 -> "❄"
        80, 81, 82 -> "🌧"
        85, 86 -> "🌨"
        95, 96, 99 -> "⛈"
        else -> if (isDay) "☁" else "☁🌙"
    }
}