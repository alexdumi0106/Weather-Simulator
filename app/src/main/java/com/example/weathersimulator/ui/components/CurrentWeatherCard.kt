package com.example.weathersimulator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weathersimulator.data.remote.weather.CurrentDto
import kotlin.math.roundToInt

@Composable
fun CurrentWeatherCard(
    weather: CurrentDto?,
    locationName: String? = null,
    modifier: Modifier = Modifier
) {
    if (weather == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Acum",
                style = MaterialTheme.typography.titleMedium
            )

            if (!locationName.isNullOrBlank()) {
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${weather.temperature?.roundToInt() ?: "--"}°C",
                    style = MaterialTheme.typography.displayMedium
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = weatherConditionText(weather.weatherCode),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Feels like ${weather.apparentTemperature?.roundToInt() ?: "--"}°C",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Wind: ${weather.windSpeed?.toInt() ?: "--"} km/h",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Humidity: ${weather.humidity ?: "--"}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Pressure: ${weather.pressure?.toInt() ?: "--"} hPa",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun weatherConditionText(code: Int?): String {
    return when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75 -> "Snow"
        77 -> "Snow grains"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Storm with hail"
        else -> "Unknown"
    }
}