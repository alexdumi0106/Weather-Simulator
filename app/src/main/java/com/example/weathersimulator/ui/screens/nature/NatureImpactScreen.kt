package com.example.weathersimulator.ui.screens.nature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathersimulator.ui.screens.main.WeatherUiState
import java.time.LocalDate
import kotlin.math.roundToInt

private const val Degree = "\u00B0"

private val NatureBackground = Color(0xFF061625)
private val NatureBackgroundDeep = Color(0xFF02101C)
private val NatureSurface = Color(0xFF0B2434)
private val NatureBorder = Color(0xFF2F6678)
private val NatureGreen = Color(0xFF5BE08A)
private val NatureYellow = Color(0xFFFFC857)
private val NatureBlue = Color(0xFF58C9FF)
private val NatureRed = Color(0xFFFF8D7A)
private val NatureMutedText = Color.White.copy(alpha = 0.70f)

private data class NatureImpactItem(
    val title: String,
    val description: String,
    val level: String,
    val score: Int,
    val icon: ImageVector,
    val accent: Color
)

private data class NatureImpactSummary(
    val headline: String,
    val shortAdvice: String,
    val items: List<NatureImpactItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NatureImpactScreen(
    state: WeatherUiState,
    cityName: String,
    onBack: () -> Unit
) {
    val current = state.data?.current
    val hourly = state.data?.hourly
    val date = LocalDate.now()
    val precipitation = hourly?.precipitation?.firstOrNull() ?: 0.0
    val snowfall = hourly?.snowfall?.firstOrNull() ?: 0.0
    val cityLabel = cityName
        .substringBefore(",")
        .replace("Loca\u010C\u203Aia ta", "Locatia ta")
        .replace("Loca\u021bia ta", "Locatia ta")
        .replace("Loca\u0163ia ta", "Locatia ta")
        .replace("Loca\u010dia ta", "Locatia ta")
        .ifBlank { "Locatia ta" }
    val summary = buildNatureImpactSummary(
        date = date,
        temperature = current?.temperature,
        humidity = current?.humidity,
        precipitation = precipitation,
        snowfall = snowfall,
        windSpeed = current?.windSpeed,
        uvIndex = current?.uvIndex,
        weatherCode = current?.weatherCode
    )

    Scaffold(
        containerColor = NatureBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Natura",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Impactul vremii asupra naturii",
                            color = NatureMutedText,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Inapoi",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = null,
                        tint = NatureGreen,
                        modifier = Modifier
                            .padding(end = 18.dp)
                            .size(28.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NatureBackgroundDeep,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = NatureGreen
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            NatureBackgroundDeep,
                            NatureBackground,
                            Color(0xFF073140)
                        )
                    )
                )
                .padding(padding)
        ) {
            NatureBackgroundGlow(
                temperature = current?.temperature,
                precipitation = precipitation,
                snowfall = snowfall,
                weatherCode = current?.weatherCode
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                NatureHeroCard(
                    cityName = cityLabel,
                    dateLabel = date.toRomanianLabel(),
                    temperature = current?.temperature.temperatureText(),
                    condition = natureConditionLabel(
                        weatherCode = current?.weatherCode,
                        precipitation = precipitation,
                        snowfall = snowfall
                    ),
                    headline = summary.headline,
                    shortAdvice = summary.shortAdvice
                )

                NatureImpactList(items = summary.items)

                NatureWeatherFacts(
                    temperature = current?.temperature.temperatureText(),
                    humidity = current?.humidity?.let { "$it%" } ?: "--",
                    precipitation = "${precipitation.roundToInt()} mm",
                    wind = current?.windSpeed?.roundToInt()?.let { "$it km/h" } ?: "--",
                    uv = current?.uvIndex?.roundToInt()?.toString() ?: "--"
                )

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun NatureBackgroundGlow(
    temperature: Double?,
    precipitation: Double,
    snowfall: Double,
    weatherCode: Int?
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val code = weatherCode ?: -1
        val accent = when {
            snowfall > 0.0 || code in 71..86 -> NatureBlue
            precipitation > 0.0 || code in 51..67 || code in 80..82 -> NatureBlue
            temperature != null && temperature >= 24.0 -> NatureYellow
            else -> NatureGreen
        }

        drawCircle(
            color = accent.copy(alpha = 0.18f),
            radius = size.width * 0.62f,
            center = Offset(size.width * 0.90f, size.height * 0.08f)
        )
        drawCircle(
            color = NatureGreen.copy(alpha = 0.10f),
            radius = size.width * 0.44f,
            center = Offset(size.width * 0.10f, size.height * 0.42f)
        )
    }
}

@Composable
private fun NatureHeroCard(
    cityName: String,
    dateLabel: String,
    temperature: String,
    condition: String,
    headline: String,
    shortAdvice: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(214.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NatureSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF14354A),
                            Color(0xFF0C2B3C),
                            Color(0xFF0A1C2D)
                        )
                    )
                )
        ) {
            NatureLandscape()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Impactul vremii",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                        Text(
                            text = cityName,
                            color = Color.White.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HeroChipRow(dateLabel = dateLabel, condition = condition)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = temperature,
                            color = Color.White,
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Light,
                            lineHeight = 48.sp
                        )
                        Text(
                            text = "acum",
                            color = Color.White.copy(alpha = 0.70f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = headline,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = shortAdvice,
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroChipRow(dateLabel: String, condition: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NatureHeroChip(
            icon = Icons.Rounded.DateRange,
            text = dateLabel
        )
        NatureHeroChip(
            icon = Icons.Rounded.Cloud,
            text = condition
        )
    }
}

@Composable
private fun NatureHeroChip(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NatureGreen,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.86f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

@Composable
private fun NatureLandscape() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = NatureYellow.copy(alpha = 0.38f),
            radius = h * 0.16f,
            center = Offset(w * 0.80f, h * 0.34f)
        )

        val hill = Path().apply {
            moveTo(0f, h * 0.78f)
            cubicTo(w * 0.18f, h * 0.62f, w * 0.38f, h * 0.84f, w * 0.54f, h * 0.68f)
            cubicTo(w * 0.72f, h * 0.50f, w * 0.88f, h * 0.72f, w, h * 0.62f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(hill, Color(0xFF082538).copy(alpha = 0.86f))

        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color.Transparent,
                    Color(0xFF061625).copy(alpha = 0.72f)
                )
            )
        )
    }
}

@Composable
private fun NatureImpactList(items: List<NatureImpactItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NatureSurface.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, NatureBorder.copy(alpha = 0.64f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = NatureGreen,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Semnale pentru natura",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            items.forEach { item ->
                NatureImpactRow(item = item)
            }
        }
    }
}

@Composable
private fun NatureImpactRow(item: NatureImpactItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(22.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(item.accent.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.accent,
                modifier = Modifier.size(27.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = item.description,
                color = NatureMutedText,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 17.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.level,
                color = item.accent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = "${item.score}/100",
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun NatureWeatherFacts(
    temperature: String,
    humidity: String,
    precipitation: String,
    wind: String,
    uv: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NatureSurface.copy(alpha = 0.82f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Date folosite",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NatureFactTile(
                    icon = Icons.Rounded.Thermostat,
                    label = "Temperatura",
                    value = temperature,
                    accent = NatureYellow,
                    modifier = Modifier.weight(1f)
                )
                NatureFactTile(
                    icon = Icons.Rounded.WaterDrop,
                    label = "Umiditate",
                    value = humidity,
                    accent = NatureBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NatureFactTile(
                    icon = Icons.Rounded.Cloud,
                    label = "Precipitatii",
                    value = precipitation,
                    accent = NatureBlue,
                    modifier = Modifier.weight(1f)
                )
                NatureFactTile(
                    icon = Icons.Rounded.Air,
                    label = "Vant",
                    value = wind,
                    accent = NatureGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            NatureFactTile(
                icon = Icons.Rounded.WbSunny,
                label = "Indice UV",
                value = uv,
                accent = NatureYellow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NatureFactTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(76.dp)
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(accent.copy(alpha = 0.13f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                color = NatureMutedText,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        }
    }
}

private fun buildNatureImpactSummary(
    date: LocalDate,
    temperature: Double?,
    humidity: Int?,
    precipitation: Double,
    snowfall: Double,
    windSpeed: Double?,
    uvIndex: Double?,
    weatherCode: Int?
): NatureImpactSummary {
    val month = date.monthValue
    val temp = temperature ?: 0.0
    val hum = humidity ?: 0
    val wind = windSpeed ?: 0.0
    val uv = uvIndex ?: 0.0
    val code = weatherCode ?: -1
    val rainy = precipitation > 0.2 || code in 51..67 || code in 80..82
    val storm = code in 95..99
    val snow = snowfall > 0.0 || code in 71..86
    val spring = month in 3..5
    val springOrEarlySummer = month in 3..6
    val pollenSeason = month in 3..8
    val warmSeason = month in 5..9
    val vegetationSeason = month in 3..10
    val frostSeason = month in listOf(1, 2, 3, 4, 11, 12)
    val coldSeason = month in listOf(1, 2, 12)
    val autumn = month in 9..11

    val floweringScore = when {
        !springOrEarlySummer -> if (vegetationSeason) 44 else 18
        snow || temp < 8.0 -> 30
        storm || wind > 35.0 -> 48
        temp in 15.0..27.0 && hum in 45..75 && !rainy -> 94
        temp in 12.0..30.0 && hum in 35..85 -> 78
        else -> 58
    }

    val pollenScore = when {
        !pollenSeason -> 18
        rainy || snow -> 24
        spring && temp in 14.0..28.0 && hum in 30..65 && wind in 5.0..28.0 -> 88
        temp in 12.0..31.0 && hum < 75 && !storm -> 66
        else -> 38
    }

    val pollinatorScore = when {
        month !in 3..10 -> 22
        rainy || snow || storm -> 28
        temp in 16.0..30.0 && wind < 22.0 -> 92
        temp in 12.0..32.0 && wind < 30.0 -> 68
        else -> 36
    }

    val mushroomScore = when {
        snow || temp < 5.0 -> 26
        (rainy || hum >= 78) && temp in 9.0..24.0 && month in 4..11 -> if (autumn) 96 else 92
        hum >= 70 && temp in 8.0..26.0 && month in 4..11 -> if (autumn) 82 else 74
        else -> 35
    }

    val mosquitoScore = when {
        !warmSeason -> 24
        temp >= 20.0 && hum >= 72 && wind < 15.0 -> 90
        temp >= 20.0 && hum >= 65 && wind < 15.0 && rainy -> 86
        temp >= 18.0 && hum >= 55 && wind < 22.0 -> 72
        temp >= 16.0 && hum >= 45 -> 48
        else -> 25
    }

    val frostScore = when {
        temp <= -3.0 -> 96
        temp <= 1.0 && (hum >= 75 || rainy || snow) -> 90
        temp <= 3.0 && frostSeason -> 74
        temp <= 5.0 && frostSeason -> 52
        else -> 18
    }

    val iceScore = when {
        temp <= 1.0 && (rainy || snow || precipitation > 0.0) -> 94
        temp <= 0.0 && hum >= 80 -> 78
        coldSeason && temp <= 3.0 && hum >= 70 -> 62
        else -> 16
    }

    val heatStressScore = when {
        temp >= 34.0 && uv >= 6.0 && !rainy -> 96
        temp >= 31.0 && (uv >= 5.0 || hum < 45) && !rainy -> 86
        temp >= 28.0 && hum < 45 && !rainy -> 72
        else -> 24
    }

    val soilDrynessScore = when {
        month in 4..9 && temp >= 28.0 && hum < 40 && precipitation < 0.1 -> 90
        month in 3..10 && temp >= 24.0 && hum < 50 && !rainy -> 72
        month in 6..8 && uv >= 7.0 && precipitation < 0.1 -> 66
        else -> 26
    }

    val autumnColorScore = when {
        !autumn -> 18
        temp in 4.0..18.0 && !storm -> 84
        temp in 0.0..22.0 -> 66
        else -> 38
    }

    val snowCoverScore = when {
        snow -> 90
        coldSeason && temp <= 0.0 -> 72
        month in listOf(1, 2, 12) && temp <= 3.0 -> 46
        else -> 12
    }

    val stormImpactScore = when {
        storm -> 92
        wind >= 45.0 -> 88
        wind >= 30.0 && rainy -> 74
        wind >= 25.0 -> 58
        else -> 18
    }

    val items = listOf(
        NatureImpactItem(
            title = "Inflorire",
            description = floweringDescription(floweringScore, springOrEarlySummer),
            level = positiveLevelLabel(floweringScore),
            score = floweringScore,
            icon = Icons.Rounded.WbSunny,
            accent = positiveScoreColor(floweringScore)
        ),
        NatureImpactItem(
            title = "Polen in aer",
            description = pollenDescription(pollenScore),
            level = riskLevelLabel(pollenScore),
            score = pollenScore,
            icon = Icons.Rounded.Air,
            accent = riskScoreColor(pollenScore)
        ),
        NatureImpactItem(
            title = "Activitate polenizatori",
            description = pollinatorDescription(pollinatorScore),
            level = positiveLevelLabel(pollinatorScore),
            score = pollinatorScore,
            icon = Icons.Rounded.WbSunny,
            accent = positiveScoreColor(pollinatorScore)
        ),
        NatureImpactItem(
            title = "Aparitia ciupercilor",
            description = mushroomDescription(mushroomScore),
            level = positiveLevelLabel(mushroomScore),
            score = mushroomScore,
            icon = Icons.Rounded.WaterDrop,
            accent = positiveScoreColor(mushroomScore)
        ),
        NatureImpactItem(
            title = "Conditii pentru tantari",
            description = mosquitoDescription(mosquitoScore),
            level = riskLevelLabel(mosquitoScore),
            score = mosquitoScore,
            icon = Icons.Rounded.Air,
            accent = riskScoreColor(mosquitoScore)
        ),
        NatureImpactItem(
            title = "Risc de inghet",
            description = frostDescription(frostScore),
            level = riskLevelLabel(frostScore),
            score = frostScore,
            icon = Icons.Rounded.Thermostat,
            accent = riskScoreColor(frostScore)
        ),
        NatureImpactItem(
            title = "Risc de polei",
            description = iceDescription(iceScore),
            level = riskLevelLabel(iceScore),
            score = iceScore,
            icon = Icons.Rounded.Cloud,
            accent = riskScoreColor(iceScore)
        ),
        NatureImpactItem(
            title = "Stres termic pentru plante",
            description = heatStressDescription(heatStressScore),
            level = riskLevelLabel(heatStressScore),
            score = heatStressScore,
            icon = Icons.Rounded.Thermostat,
            accent = riskScoreColor(heatStressScore)
        ),
        NatureImpactItem(
            title = "Uscaciune la sol",
            description = soilDrynessDescription(soilDrynessScore),
            level = riskLevelLabel(soilDrynessScore),
            score = soilDrynessScore,
            icon = Icons.Rounded.WbSunny,
            accent = riskScoreColor(soilDrynessScore)
        ),
        NatureImpactItem(
            title = "Culori de toamna",
            description = autumnColorDescription(autumnColorScore),
            level = positiveLevelLabel(autumnColorScore),
            score = autumnColorScore,
            icon = Icons.Rounded.WbSunny,
            accent = positiveScoreColor(autumnColorScore)
        ),
        NatureImpactItem(
            title = "Strat de zapada",
            description = snowCoverDescription(snowCoverScore),
            level = positiveLevelLabel(snowCoverScore),
            score = snowCoverScore,
            icon = Icons.Rounded.Cloud,
            accent = if (snowCoverScore >= 65) NatureBlue else NatureMutedText
        ),
        NatureImpactItem(
            title = "Impact de vant/furtuna",
            description = stormImpactDescription(stormImpactScore),
            level = riskLevelLabel(stormImpactScore),
            score = stormImpactScore,
            icon = Icons.Rounded.Air,
            accent = riskScoreColor(stormImpactScore)
        )
    ).sortedByDescending { it.score }

    val strongest = items.firstOrNull()
    val headline = strongest?.let { "${it.title}: ${it.level.lowercase()}" }
        ?: "Impact meteo moderat"
    val advice = when (strongest?.title) {
        "Inflorire" -> "Vremea favorizeaza activitatea plantelor si aspectul vegetatiei."
        "Polen in aer" -> "Conditiile pot favoriza raspandirea polenului, mai ales in zone verzi."
        "Activitate polenizatori" -> "Vremea este potrivita pentru albine si alte insecte polenizatoare."
        "Aparitia ciupercilor" -> "Umezeala si temperatura pot favoriza aparitia ciupercilor."
        "Conditii pentru tantari" -> "Conditiile pot creste disconfortul produs de insecte in zone verzi."
        "Risc de inghet" -> "Temperatura poate afecta plantele sensibile si suprafetele expuse."
        "Risc de polei" -> "Combinatia de frig si umezeala poate face suprafetele alunecoase."
        "Stres termic pentru plante" -> "Plantele pot avea nevoie de atentie din cauza caldurii."
        "Uscaciune la sol" -> "Solul se poate usca mai repede in zonele expuse la soare."
        "Culori de toamna" -> "Vremea poate favoriza schimbarea culorilor frunzelor."
        "Strat de zapada" -> "Conditiile sunt potrivite pentru mentinerea sau aparitia zapezii."
        "Impact de vant/furtuna" -> "Vantul poate afecta ramurile, florile si vegetatia expusa."
        else -> "Conditiile sunt echilibrate, cu impact natural redus."
    }

    return NatureImpactSummary(
        headline = headline,
        shortAdvice = advice,
        items = items
    )
}

private fun floweringDescription(score: Int, inSeason: Boolean): String {
    return when {
        !inSeason -> "Sezonul nu este ideal pentru inflorire intensa."
        score >= 85 -> "Conditii excelente pentru flori si vegetatie activa."
        score >= 65 -> "Conditii bune, dar vremea poate limita usor ritmul natural."
        else -> "Conditii mai slabe pentru inflorire in acest moment."
    }
}

private fun pollenDescription(score: Int): String {
    return when {
        score >= 85 -> "Vreme calda, uscata si cu vant favorabil raspandirii polenului."
        score >= 65 -> "Exista conditii moderate pentru polen in aer."
        else -> "Conditiile limiteaza raspandirea polenului."
    }
}

private fun pollinatorDescription(score: Int): String {
    return when {
        score >= 85 -> "Conditii foarte bune pentru activitatea albinelor si polenizatorilor."
        score >= 65 -> "Activitate posibila, mai ales in intervalele mai calde ale zilei."
        else -> "Ploaia, frigul sau vantul pot reduce activitatea polenizatorilor."
    }
}

private fun mushroomDescription(score: Int): String {
    return when {
        score >= 85 -> "Umezeala si temperatura sunt favorabile aparitiei ciupercilor."
        score >= 65 -> "Exista sanse moderate in zone umbrite sau umede."
        else -> "Conditiile sunt mai putin favorabile pentru ciuperci."
    }
}

private fun mosquitoDescription(score: Int): String {
    return when {
        score >= 85 -> "Conditii favorabile, mai ales seara, langa apa sau vegetatie."
        score >= 65 -> "Conditii moderate in zone umede si ferite de vant."
        else -> "Conditii reduse pentru activitatea tantarilor."
    }
}

private fun frostDescription(score: Int): String {
    return when {
        score >= 85 -> "Risc ridicat pentru plante sensibile si suprafete expuse."
        score >= 65 -> "Risc moderat, mai ales dimineata sau noaptea."
        else -> "Risc redus de inghet in conditiile actuale."
    }
}

private fun iceDescription(score: Int): String {
    return when {
        score >= 85 -> "Frigul si umezeala pot favoriza poleiul."
        score >= 65 -> "Exista sanse moderate de suprafete alunecoase."
        else -> "Risc redus de polei."
    }
}

private fun heatStressDescription(score: Int): String {
    return when {
        score >= 85 -> "Caldura si UV-ul pot stresa plantele expuse."
        score >= 65 -> "Stres moderat pentru vegetatie in zone insorite."
        else -> "Stres termic redus pentru plante."
    }
}

private fun soilDrynessDescription(score: Int): String {
    return when {
        score >= 85 -> "Solul se poate usca rapid fara precipitatii."
        score >= 65 -> "Uscaciune moderata in zone expuse la soare."
        else -> "Uscaciune redusa la nivelul solului."
    }
}

private fun autumnColorDescription(score: Int): String {
    return when {
        score >= 85 -> "Conditii bune pentru intensificarea culorilor de toamna."
        score >= 65 -> "Schimbarea frunzelor poate fi vizibila treptat."
        else -> "Sezonul sau vremea nu favorizeaza in mod special culorile de toamna."
    }
}

private fun snowCoverDescription(score: Int): String {
    return when {
        score >= 85 -> "Conditii favorabile pentru zapada sau strat persistent."
        score >= 65 -> "Frigul poate mentine zapada in zonele expuse."
        else -> "Conditii slabe pentru strat de zapada."
    }
}

private fun stormImpactDescription(score: Int): String {
    return when {
        score >= 85 -> "Vantul sau furtuna pot afecta ramurile si plantele fragile."
        score >= 65 -> "Impact moderat asupra vegetatiei expuse."
        else -> "Impact redus al vantului asupra naturii."
    }
}

private fun positiveLevelLabel(score: Int): String {
    return when {
        score >= 85 -> "Excelent"
        score >= 65 -> "Bun"
        score >= 45 -> "Moderat"
        else -> "Scazut"
    }
}

private fun riskLevelLabel(score: Int): String {
    return when {
        score >= 85 -> "Ridicat"
        score >= 65 -> "Moderat"
        score >= 45 -> "Scazut"
        else -> "Foarte scazut"
    }
}

private fun positiveScoreColor(score: Int): Color {
    return when {
        score >= 85 -> NatureGreen
        score >= 65 -> NatureYellow
        score >= 45 -> NatureBlue
        else -> NatureMutedText
    }
}

private fun riskScoreColor(score: Int): Color {
    return when {
        score >= 85 -> NatureRed
        score >= 65 -> NatureYellow
        score >= 45 -> NatureBlue
        else -> NatureMutedText
    }
}

private fun natureConditionLabel(
    weatherCode: Int?,
    precipitation: Double,
    snowfall: Double
): String {
    val code = weatherCode ?: -1

    return when {
        snowfall > 0.0 || code in 71..86 -> "Ninsoare"
        code in 95..99 -> "Furtuna"
        precipitation > 0.0 || code in 51..67 || code in 80..82 -> "Ploaie"
        code in 45..48 -> "Ceata"
        code == 0 -> "Cer senin"
        code in 1..3 -> "Partial noros"
        else -> "Vreme actuala"
    }
}

private fun Double?.temperatureText(): String {
    return this?.roundToInt()?.let { "$it$Degree" } ?: "--"
}

private fun LocalDate.toRomanianLabel(): String {
    val monthName = when (monthValue) {
        1 -> "ianuarie"
        2 -> "februarie"
        3 -> "martie"
        4 -> "aprilie"
        5 -> "mai"
        6 -> "iunie"
        7 -> "iulie"
        8 -> "august"
        9 -> "septembrie"
        10 -> "octombrie"
        11 -> "noiembrie"
        else -> "decembrie"
    }

    return "$dayOfMonth $monthName $year"
}
