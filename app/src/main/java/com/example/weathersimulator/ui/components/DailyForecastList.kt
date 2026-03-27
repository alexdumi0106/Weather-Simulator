package com.example.weathersimulator.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weathersimulator.ui.screens.main.DailyForecastItemUi
import com.example.weathersimulator.ui.weather.WeatherIconRules

@Composable
fun DailyForecastList(
    items: List<DailyForecastItemUi>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val visibleItems = items.take(10)
    val minGlobal = visibleItems.minOf { parseTemp(it.minTemperature) }
    val maxGlobal = visibleItems.maxOf { parseTemp(it.maxTemperature) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "PROGNOZA PE 10 ZILE",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White.copy(alpha = 0.8f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(vertical = 6.dp)
        ) {
            visibleItems.forEachIndexed { index, item ->
                DailyForecastRow(
                    item = item,
                    globalMin = minGlobal,
                    globalMax = maxGlobal
                )

                if (index < visibleItems.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.White.copy(alpha = 0.14f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    item: DailyForecastItemUi,
    globalMin: Int,
    globalMax: Int
) {
    val visual = WeatherIconRules.resolve(
        weatherCode = item.weatherCode,
        isDay = item.isDay,
        cloudCover = item.cloudCover
    )

    val itemMin = parseTemp(item.minTemperature)
    val itemMax = parseTemp(item.maxTemperature)
    val totalRange = (globalMax - globalMin).coerceAtLeast(1)
    val startFraction = (itemMin - globalMin).toFloat() / totalRange
    val endFraction = (itemMax - globalMin).toFloat() / totalRange
    val activeFraction = (endFraction - startFraction).coerceAtLeast(0.08f)
    val trackWidth = 118.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.dayLabel,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = painterResource(id = visual.iconRes),
            contentDescription = "Icon meteo zilnic",
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(38.dp)
        )

        Text(
            text = item.minTemperature,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.72f),
            modifier = Modifier.padding(end = 8.dp)
        )

        Box(
            modifier = Modifier
                .width(trackWidth)
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0x55A6C4D8))
        ) {
            Box(
                modifier = Modifier
                    .offset(x = trackWidth * startFraction)
                    .width(trackWidth * activeFraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFFB8E46A))
            )
        }

        Text(
            text = item.maxTemperature,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun parseTemp(temp: String): Int {
    return temp.replace("°", "").trim().toIntOrNull() ?: 0
}
