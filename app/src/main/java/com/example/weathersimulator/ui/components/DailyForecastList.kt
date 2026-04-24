package com.example.weathersimulator.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    var expandedDateKey by remember(visibleItems) { mutableStateOf<String?>(null) }
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
                    globalMax = maxGlobal,
                    isExpanded = expandedDateKey == item.dateKey,
                    onToggleExpanded = {
                        expandedDateKey = if (expandedDateKey == item.dateKey) null else item.dateKey
                    }
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
    globalMax: Int,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val visual = WeatherIconRules.resolve(
        weatherCode = item.weatherCode,
        isDay = item.isDay,
        cloudCover = item.cloudCover
    )
    val dayVisual = WeatherIconRules.resolve(
        weatherCode = item.weatherCode,
        isDay = item.isDay,
        cloudCover = item.cloudCover
    )
    val nightVisual = WeatherIconRules.resolve(
        weatherCode = item.nightWeatherCode,
        isDay = false,
        cloudCover = item.nightCloudCover
    )

    val itemMin = parseTemp(item.minTemperature)
    val itemMax = parseTemp(item.maxTemperature)
    val totalRange = (globalMax - globalMin).coerceAtLeast(1)
    val startFraction = (itemMin - globalMin).toFloat() / totalRange
    val endFraction = (itemMax - globalMin).toFloat() / totalRange
    val activeFraction = (endFraction - startFraction).coerceAtLeast(0.08f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() }
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.dayLabel,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(min = 72.dp, max = 108.dp)
            )

            Image(
                painter = painterResource(id = visual.iconRes),
                contentDescription = "Icon meteo zilnic",
                modifier = Modifier.width(24.dp)
            )

            Text(
                text = item.minTemperature,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                modifier = Modifier.width(30.dp)
            )

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
            ) {
                val radius = size.height / 2f
                drawRoundRect(
                    color = Color(0x55A6C4D8),
                    cornerRadius = CornerRadius(radius, radius)
                )

                val safeStart = startFraction.coerceIn(0f, 1f)
                val safeActive = activeFraction.coerceIn(0.08f, 1f)
                val startX = size.width * safeStart
                val activeWidth = (size.width * safeActive).coerceAtLeast(10f)
                val clampedWidth = (size.width - startX).coerceAtLeast(0f).coerceAtMost(activeWidth)

                drawRoundRect(
                    color = Color(0xFFB8E46A),
                    topLeft = Offset(startX, 0f),
                    size = Size(clampedWidth, size.height),
                    cornerRadius = CornerRadius(radius, radius)
                )
            }

            Text(
                text = item.maxTemperature,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.width(30.dp)
            )

            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = if (isExpanded) "Ascunde detalii" else "Afișează detalii",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(20.dp)
            )
        }

        if (isExpanded) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.16f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ForecastPartColumn(
                    title = "Zi",
                    iconRes = dayVisual.iconRes,
                    description = dayVisual.label,
                    temperatureLabel = "Max",
                    temperature = item.dayMaxTemperature,
                    modifier = Modifier.weight(1f)
                )

                ForecastPartColumn(
                    title = "Noapte",
                    iconRes = nightVisual.iconRes,
                    description = nightVisual.label,
                    temperatureLabel = "Min",
                    temperature = item.nightMinTemperature,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ForecastPartColumn(
    title: String,
    iconRes: Int,
    description: String,
    temperatureLabel: String,
    temperature: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.75f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = description,
                modifier = Modifier.size(28.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = "$temperatureLabel: $temperature",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f)
                )
            }
        }
    }
}

private fun parseTemp(temp: String): Int {
    return temp.replace("°", "").trim().toIntOrNull() ?: 0
}
