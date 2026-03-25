package com.example.weathersimulator.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.weathersimulator.R
import com.example.weathersimulator.ui.screens.main.DailyForecastItemUi

@Composable
fun DailyForecastList(
    items: List<DailyForecastItemUi>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Următoarele 15 zile",
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(vertical = 4.dp)
        ) {
            items.forEachIndexed { index, item ->
                DailyForecastRow(item = item)

                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(item: DailyForecastItemUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.dayLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = painterResource(id = weatherCodeToIconRes(item.weatherCode)),
            contentDescription = "Icon meteo zilnic",
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Text(
            text = "${item.maxTemperature} / ${item.minTemperature}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun weatherCodeToIconRes(code: Int): Int {
    return when (code) {
        0 -> R.drawable.icon_weather_01
        1, 2 -> R.drawable.icon_weather_02
        3 -> R.drawable.icon_weather_04
        45, 48 -> R.drawable.icon_weather_11
        51, 53, 55, 56, 57 -> R.drawable.icon_weather_14
        61, 63, 65, 66, 67 -> R.drawable.icon_weather_12
        71, 73, 75, 77 -> R.drawable.icon_weather_13
        80, 81, 82 -> R.drawable.icon_weather_39
        85, 86 -> R.drawable.icon_weather_14
        95, 96, 99 -> R.drawable.icon_weather_17
        else -> R.drawable.icon_weather_04
    }
}