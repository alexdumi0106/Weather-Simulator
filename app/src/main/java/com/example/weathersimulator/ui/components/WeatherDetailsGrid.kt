package com.example.weathersimulator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse
import java.util.Calendar
import java.util.Locale
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.tan
import kotlin.math.PI

@Composable
fun WeatherDetailsGrid(
    data: OpenMeteoResponse?,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    modifier: Modifier = Modifier
) {
    if (data == null) return

    val current = data.current ?: return

    // Calculate sunrise and sunset for local timezone and current date.
    val calendar = Calendar.getInstance()
    val sunTimes = calculateSunriseSunsetLocal(latitude, longitude, calendar)

    val sunriseStr = sunTimes.first?.let { minutesToClock(it) } ?: "--:--"
    val sunsetStr = sunTimes.second?.let { minutesToClock(it) } ?: "--:--"

    val moonPhase = calculateMoonPhase(calendar)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Temperatura resimțită și Răsărit/Apus
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailCard(
                title = "TEMPERATURĂ RESIMȚITĂ",
                value = "${current.apparentTemperature?.toInt() ?: "--"}°",
                subtitle = "Acum: ${current.temperature?.toInt() ?: "--"}°",
                modifier = Modifier.weight(1f)
            )

            WeatherDetailCard(
                title = "RĂSĂRIT ȘI APUS",
                value = sunriseStr,
                subtitle = "Apus: $sunsetStr",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Precipitații și Umiditate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailCard(
                title = "PRECIPITAȚII",
                value = "${data.hourly?.precipitation?.firstOrNull()?.toInt() ?: 0} mm",
                subtitle = "În ultimele 24 h",
                modifier = Modifier.weight(1f)
            )

            WeatherDetailCard(
                title = "UMIDITATE",
                value = "${current.humidity?.toInt() ?: "--"}%",
                subtitle = "Punctul de roua: ${calculateDewPoint(current.temperature ?: 0.0, current.humidity ?: 0)}°",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Faza lunii și Presiune atmosferică
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailCard(
                title = "FAZA LUNII",
                value = moonPhase.name,
                subtitle = "Iluminare: ${moonPhase.illuminationPercent}%",
                modifier = Modifier.weight(1f)
            )

            WeatherDetailCard(
                title = "PRESIUNE ATMOSFERICĂ",
                value = "${current.pressure?.toInt() ?: "--"} hPa",
                subtitle = "Normală",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WeatherDetailCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                fontSize = 32.sp
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

// Calcul pentru punctul de roua (formula aproximativă)
private fun calculateDewPoint(temp: Double, humidity: Int): Int {
    if (humidity <= 0) return temp.toInt()
    val a = 17.27
    val b = 237.7
    val alpha = ((a * temp) / (b + temp)) + ln(humidity / 100.0)
    val dewPoint = (b * alpha) / (a - alpha)
    return dewPoint.toInt()
}

private data class MoonPhaseInfo(
    val name: String,
    val illuminationPercent: Int
)

private fun calculateMoonPhase(calendar: Calendar): MoonPhaseInfo {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var y = year
    var m = month
    if (m <= 2) {
        y -= 1
        m += 12
    }

    val a = floor(y / 100.0)
    val b = 2 - a + floor(a / 4.0)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    val dayFraction = (hour + minute / 60.0 + second / 3600.0) / 24.0

    val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + dayFraction + b - 1524.5
    val daysSinceNewMoon = jd - 2451550.1
    val synodicMonth = 29.530588853
    val normalized = ((daysSinceNewMoon % synodicMonth) + synodicMonth) % synodicMonth
    val phase = normalized / synodicMonth

    val illumination = ((1 - cos(2 * PI * phase)) / 2.0 * 100).toInt().coerceIn(0, 100)

    val name = when {
        phase < 0.03 || phase >= 0.97 -> "Lună nouă"
        phase < 0.22 -> "Semilună în creștere"
        phase < 0.28 -> "Primul pătrar"
        phase < 0.47 -> "Lună convexă în creștere"
        phase < 0.53 -> "Lună plină"
        phase < 0.72 -> "Lună convexă în descreștere"
        phase < 0.78 -> "Ultimul pătrar"
        else -> "Semilună în descreștere"
    }

    return MoonPhaseInfo(name = name, illuminationPercent = illumination)
}

private fun minutesToClock(totalMinutes: Int): String {
    val normalized = ((totalMinutes % 1440) + 1440) % 1440
    val h = normalized / 60
    val m = normalized % 60
    return String.format(Locale.getDefault(), "%02d:%02d", h, m)
}

private fun normalizeDegrees(value: Double): Double {
    var result = value % 360.0
    if (result < 0) result += 360.0
    return result
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
        meanAnomaly + 1.916 * sin(Math.toRadians(meanAnomaly)) +
            0.020 * sin(Math.toRadians(2 * meanAnomaly)) + 282.634
    )

    var rightAscension = Math.toDegrees(atan(0.91764 * tan(Math.toRadians(trueLongitude))))
    rightAscension = normalizeDegrees(rightAscension)

    val lQuadrant = floor(trueLongitude / 90.0) * 90.0
    val raQuadrant = floor(rightAscension / 90.0) * 90.0
    rightAscension += lQuadrant - raQuadrant
    rightAscension /= 15.0

    val sinDec = 0.39782 * sin(Math.toRadians(trueLongitude))
    val cosDec = cos(asin(sinDec))

    val cosH = (
        cos(Math.toRadians(90.833)) - sinDec * sin(Math.toRadians(latitude))
        ) / (cosDec * cos(Math.toRadians(latitude)))

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

private fun calculateSunriseSunsetLocal(latitude: Double, longitude: Double, calendar: Calendar): Pair<Int?, Int?> {
    if (latitude == 0.0 && longitude == 0.0) return Pair(null, null)

    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val offsetMillis = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)
    val utcOffsetHours = offsetMillis / 3_600_000.0

    val sunrise = calculateSunEventMinutes(dayOfYear, latitude, longitude, utcOffsetHours, isSunrise = true)
    val sunset = calculateSunEventMinutes(dayOfYear, latitude, longitude, utcOffsetHours, isSunrise = false)

    return Pair(sunrise, sunset)
}
