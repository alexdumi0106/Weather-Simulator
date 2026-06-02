package com.example.weathersimulator.ui.screens.outfit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathersimulator.ui.screens.main.WeatherUiState
import kotlin.math.roundToInt

private const val Degree = "\u00B0"

private val OutfitBackground = Color(0xFF061625)
private val OutfitBackgroundDeep = Color(0xFF02101C)
private val OutfitSurface = Color(0xFF0B2236)
private val OutfitBorder = Color(0xFF315B78)
private val OutfitAccent = Color(0xFFFFC85A)
private val OutfitAccentDeep = Color(0xFFE58A1D)
private val OutfitBlue = Color(0xFF49C8FF)
private val OutfitPurple = Color(0xFF8B7CFF)
private val OutfitTextMuted = Color.White.copy(alpha = 0.68f)

private enum class OutfitWeatherMood {
    SUNNY,
    RAIN,
    STORM,
    SNOW,
    COLD,
    MILD
}

private data class OutfitPiece(
    val label: String,
    val detail: String,
    val accent: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitRecommendationScreen(
    state: WeatherUiState,
    cityName: String,
    onBack: () -> Unit,
    onGenerateClick: (String) -> Unit
) {
    val current = state.data?.current
    val hourly = state.data?.hourly
    val cityLabel = cityName.substringBefore(",").ifBlank { "Locatia ta" }
    val hasWeatherData = current != null
    val precipitation = hourly?.precipitation?.firstOrNull() ?: 0.0
    val snowfall = hourly?.snowfall?.firstOrNull() ?: 0.0
    val mood = resolveOutfitMood(
        weatherCode = current?.weatherCode,
        temperature = current?.temperature,
        precipitation = precipitation,
        snowfall = snowfall,
        isDay = current?.isDay == 1
    )
    val score = if (hasWeatherData) {
        calculateOutfitScore(
            temperature = current?.temperature,
            apparentTemperature = current?.apparentTemperature,
            precipitation = precipitation,
            snowfall = snowfall,
            windSpeed = current?.windSpeed,
            uvIndex = current?.uvIndex
        )
    } else {
        null
    }
    val backgroundColors = outfitBackgroundColors(mood)

    Scaffold(
        containerColor = OutfitBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Outfit AI",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Tinute recomandate dupa vreme",
                            color = OutfitTextMuted,
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
                        imageVector = Icons.Rounded.Checkroom,
                        contentDescription = null,
                        tint = OutfitAccent,
                        modifier = Modifier
                            .padding(end = 18.dp)
                            .size(28.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColors.first(),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = OutfitAccent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(backgroundColors))
                .padding(padding)
        ) {
            OutfitMoodBackground(mood = mood)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                OutfitHeroCard(
                    cityName = cityLabel,
                    temperature = current?.temperature.temperatureText(),
                    apparentTemperature = current?.apparentTemperature.temperatureText(),
                    condition = skyLabel(
                        weatherCode = current?.weatherCode,
                        cloudCover = current?.cloudCover,
                        precipitation = precipitation,
                        snowfall = snowfall
                    ),
                    tagLine = outfitTagLine(
                        mood = mood,
                        temperature = current?.temperature,
                        uvIndex = current?.uvIndex
                    ),
                    isDay = current?.isDay == 1,
                    mood = mood
                )

                OutfitGenerateButton(
                    isLoading = state.isOutfitRecommendationLoading,
                    enabled = hasWeatherData && !state.isOutfitRecommendationLoading,
                    onClick = { onGenerateClick(cityLabel) }
                )

                OutfitInstantRecommendationCard(
                    pieces = quickOutfitPieces(
                        temperature = current?.temperature,
                        precipitation = precipitation,
                        snowfall = snowfall,
                        windSpeed = current?.windSpeed,
                        mood = mood
                    ),
                    score = score,
                    comfortLevel = comfortLevel(score),
                    layersNeeded = layersNeeded(
                        temperature = current?.temperature,
                        precipitation = precipitation,
                        snowfall = snowfall
                    ),
                    uvAdvice = uvAdvice(current?.uvIndex),
                    bonusAdvice = bonusAdvice(
                        mood = mood,
                        temperature = current?.temperature,
                        uvIndex = current?.uvIndex
                    )
                )

                OutfitResultCard(
                    recommendation = state.outfitRecommendation,
                    isLoading = state.isOutfitRecommendationLoading,
                    error = state.outfitRecommendationError,
                    hasWeatherData = hasWeatherData
                )

                WeatherInputCard(
                    state = state,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun OutfitMoodBackground(mood: OutfitWeatherMood) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        when (mood) {
            OutfitWeatherMood.SUNNY -> {
                drawCircle(
                    color = OutfitAccent.copy(alpha = 0.20f),
                    radius = size.width * 0.64f,
                    center = Offset(size.width * 0.92f, size.height * 0.05f)
                )
                drawCircle(
                    color = OutfitAccentDeep.copy(alpha = 0.10f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.18f, size.height * 0.30f)
                )
            }

            OutfitWeatherMood.STORM -> {
                drawCircle(
                    color = OutfitPurple.copy(alpha = 0.24f),
                    radius = size.width * 0.62f,
                    center = Offset(size.width * 0.92f, size.height * 0.14f)
                )
                drawCircle(
                    color = OutfitBlue.copy(alpha = 0.14f),
                    radius = size.width * 0.48f,
                    center = Offset(size.width * 0.12f, size.height * 0.45f)
                )
            }

            OutfitWeatherMood.SNOW -> {
                val flakes = listOf(
                    Offset(size.width * 0.16f, size.height * 0.18f),
                    Offset(size.width * 0.72f, size.height * 0.20f),
                    Offset(size.width * 0.38f, size.height * 0.36f),
                    Offset(size.width * 0.86f, size.height * 0.48f),
                    Offset(size.width * 0.20f, size.height * 0.64f),
                    Offset(size.width * 0.62f, size.height * 0.76f)
                )
                flakes.forEachIndexed { index, offset ->
                    drawCircle(
                        color = Color.White.copy(alpha = if (index % 2 == 0) 0.20f else 0.12f),
                        radius = if (index % 2 == 0) 4.5f else 3f,
                        center = offset
                    )
                }
            }

            OutfitWeatherMood.RAIN -> {
                drawCircle(
                    color = OutfitBlue.copy(alpha = 0.18f),
                    radius = size.width * 0.58f,
                    center = Offset(size.width * 0.85f, size.height * 0.14f)
                )
            }

            OutfitWeatherMood.COLD -> {
                drawCircle(
                    color = Color(0xFF9DDCFF).copy(alpha = 0.16f),
                    radius = size.width * 0.52f,
                    center = Offset(size.width * 0.82f, size.height * 0.12f)
                )
            }

            OutfitWeatherMood.MILD -> {
                drawCircle(
                    color = OutfitBlue.copy(alpha = 0.12f),
                    radius = size.width * 0.56f,
                    center = Offset(size.width * 0.82f, size.height * 0.12f)
                )
                drawCircle(
                    color = OutfitAccent.copy(alpha = 0.10f),
                    radius = size.width * 0.40f,
                    center = Offset(size.width * 0.18f, size.height * 0.34f)
                )
            }
        }
    }
}

@Composable
private fun OutfitHeroCard(
    cityName: String,
    temperature: String,
    apparentTemperature: String,
    condition: String,
    tagLine: String,
    isDay: Boolean,
    mood: OutfitWeatherMood
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(188.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = OutfitSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(heroGradient(mood)))
        ) {
            HeroLandscape(mood = mood)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.10f),
                                Color.Black.copy(alpha = 0.04f),
                                Color.Black.copy(alpha = 0.42f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(OutfitAccent.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.Cloud,
                            contentDescription = null,
                            tint = moodAccent(mood),
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = cityName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 27.sp,
                            lineHeight = 30.sp
                        )
                        Text(
                            text = tagLine,
                            color = Color.White.copy(alpha = 0.84f),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HeroInfoChip(text = "Resimtita $apparentTemperature")
                            HeroInfoChip(text = condition)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = temperature,
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Light,
                        lineHeight = 56.sp
                    )
                    Text(
                        text = "acum",
                        color = Color.White.copy(alpha = 0.70f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroInfoChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.86f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

@Composable
private fun HeroLandscape(mood: OutfitWeatherMood) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val sunColor = moodAccent(mood)

        drawCircle(
            color = sunColor.copy(alpha = if (mood == OutfitWeatherMood.STORM) 0.30f else 0.74f),
            radius = h * 0.14f,
            center = Offset(w * 0.35f, h * 0.48f)
        )

        val farMountains = Path().apply {
            moveTo(0f, h * 0.72f)
            lineTo(w * 0.18f, h * 0.48f)
            lineTo(w * 0.34f, h * 0.68f)
            lineTo(w * 0.52f, h * 0.42f)
            lineTo(w * 0.72f, h * 0.70f)
            lineTo(w, h * 0.50f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(farMountains, Color(0xFF21344A).copy(alpha = 0.82f))

        val nearMountains = Path().apply {
            moveTo(0f, h * 0.82f)
            lineTo(w * 0.20f, h * 0.64f)
            lineTo(w * 0.40f, h * 0.80f)
            lineTo(w * 0.62f, h * 0.60f)
            lineTo(w * 0.82f, h * 0.78f)
            lineTo(w, h * 0.66f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(nearMountains, Color(0xFF0B1D2E).copy(alpha = 0.9f))

        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color.Transparent,
                    Color(0xFF061625).copy(alpha = 0.66f)
                )
            )
        )
    }
}

@Composable
private fun OutfitInstantRecommendationCard(
    pieces: List<OutfitPiece>,
    score: Int?,
    comfortLevel: String,
    layersNeeded: String,
    uvAdvice: String,
    bonusAdvice: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = OutfitSurface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, OutfitBorder.copy(alpha = 0.64f)),
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
                    tint = OutfitAccent,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Recomandare instant",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pieces.take(3).forEach { piece ->
                    InstantPieceTile(
                        piece = piece,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightMetricTile(
                    icon = Icons.Rounded.AutoAwesome,
                    label = "Outfit Score",
                    value = score?.let { "$it/100" } ?: "--",
                    accent = OutfitAccent,
                    modifier = Modifier.weight(1f)
                )
                InsightMetricTile(
                    icon = Icons.Rounded.WbSunny,
                    label = "Comfort Level",
                    value = comfortLevel,
                    accent = OutfitBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightMetricTile(
                    icon = Icons.Rounded.Checkroom,
                    label = "Layers Needed",
                    value = layersNeeded,
                    accent = OutfitAccent,
                    modifier = Modifier.weight(1f)
                )
                InsightMetricTile(
                    icon = Icons.Rounded.WbSunny,
                    label = "UV Advice",
                    value = uvAdvice,
                    accent = OutfitBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            BonusAdviceRow(text = bonusAdvice)
        }
    }
}

@Composable
private fun InstantPieceTile(
    piece: OutfitPiece,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 118.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(piece.accent.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Checkroom,
                contentDescription = null,
                tint = piece.accent,
                modifier = Modifier.size(25.dp)
            )
        }

        Text(
            text = piece.label,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = piece.detail,
            color = OutfitTextMuted,
            style = MaterialTheme.typography.labelSmall,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InsightMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 74.dp)
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(accent.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = label,
                color = OutfitTextMuted,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun BonusAdviceRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        OutfitAccent.copy(alpha = 0.12f),
                        OutfitBlue.copy(alpha = 0.08f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = OutfitAccent,
            modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Bonus AI",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.84f),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun WeatherInputCard(
    state: WeatherUiState,
    modifier: Modifier = Modifier
) {
    val current = state.data?.current
    val precipitation = state.data?.hourly?.precipitation?.firstOrNull() ?: 0.0

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B2236).copy(alpha = 0.86f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Detalii meteo folosite",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutfitMetricTile(
                    icon = Icons.Rounded.Thermostat,
                    label = "Temperatura",
                    value = current?.temperature.temperatureText(),
                    accent = OutfitAccent,
                    modifier = Modifier.weight(1f)
                )
                OutfitMetricTile(
                    icon = Icons.Rounded.WaterDrop,
                    label = "Umiditate",
                    value = current?.humidity?.let { "$it%" } ?: "--",
                    accent = OutfitBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutfitMetricTile(
                    icon = Icons.Rounded.Air,
                    label = "Vant",
                    value = current?.windSpeed?.roundToInt()?.let { "$it km/h" } ?: "--",
                    accent = OutfitBlue,
                    modifier = Modifier.weight(1f)
                )
                OutfitMetricTile(
                    icon = Icons.Rounded.Umbrella,
                    label = "Precipitatii",
                    value = "${precipitation.roundToInt()} mm",
                    accent = OutfitBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            OutfitMetricTile(
                icon = Icons.Rounded.WbSunny,
                label = "Indice UV",
                value = current?.uvIndex?.roundToInt()?.toString() ?: "--",
                accent = OutfitAccent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OutfitMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(78.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(accent.copy(alpha = 0.12f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(26.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                color = OutfitTextMuted,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun OutfitGenerateButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(30.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OutfitAccent,
            contentColor = Color(0xFF1D1B15),
            disabledContainerColor = Color.White.copy(alpha = 0.16f),
            disabledContentColor = Color.White.copy(alpha = 0.48f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(21.dp),
                strokeWidth = 2.dp,
                color = Color(0xFF1D1B15)
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = if (isLoading) "AI analizeaza vremea..." else "Genereaza tinuta",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun OutfitResultCard(
    recommendation: String?,
    isLoading: Boolean,
    error: String?,
    hasWeatherData: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = OutfitSurface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, OutfitBorder.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = OutfitAccent,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = "Recomandarea AI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }

            when {
                isLoading -> LoadingOutfitState()

                !recommendation.isNullOrBlank() -> {
                    OutfitRecommendationContent(recommendation)
                }

                !error.isNullOrBlank() -> {
                    Text(
                        text = error,
                        color = Color(0xFFFFD0D0),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                !hasWeatherData -> {
                    EmptyOutfitState(
                        text = "Nu exista inca date meteo pentru recomandare. Revino dupa incarcarea prognozei."
                    )
                }

                else -> {
                    EmptyOutfitState(
                        text = "Apasa pe buton pentru recomandarea AI detaliata, adaptata vremii de acum."
                    )
                }
            }
        }
    }
}

@Composable
private fun OutfitRecommendationContent(recommendation: String) {
    val sections = parseOutfitRecommendation(recommendation)

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        if (sections.isEmpty()) {
            Text(
                text = recommendation,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 23.sp
            )
        } else {
            sections.forEach { section ->
                RecommendationSectionRow(section)
            }
        }
    }
}

@Composable
private fun RecommendationSectionRow(section: OutfitSection) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.07f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = section.tint,
                modifier = Modifier.size(26.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${section.title}:",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = section.body,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 23.sp
            )
        }
    }
}

private data class OutfitSection(
    val title: String,
    val body: String,
    val icon: ImageVector,
    val tint: Color
)

private fun parseOutfitRecommendation(text: String): List<OutfitSection> {
    val normalized = text
        .replace("\r\n", "\n")
        .replace("Recomandare de \u021binut\u0103", "Recomandare de tinuta")
        .replace("Recomandare de \u0163inut\u0103", "Recomandare de tinuta")
        .replace("Ce NU este necesar", "Ce nu este necesar")

    val titles = listOf(
        "Recomandare de tinuta",
        "Accesorii utile",
        "Ce nu este necesar"
    )

    val matches = titles.mapNotNull { title ->
        val index = normalized.indexOf(title, ignoreCase = true)
        if (index >= 0) title to index else null
    }.sortedBy { it.second }

    if (matches.isEmpty()) return emptyList()

    return matches.mapIndexedNotNull { index, match ->
        val start = match.second + match.first.length
        val end = matches.getOrNull(index + 1)?.second ?: normalized.length
        val body = normalized.substring(start, end)
            .trim()
            .trimStart(':')
            .trim()

        if (body.isBlank()) {
            null
        } else {
            val sectionTitle = match.first
            OutfitSection(
                title = sectionTitle,
                body = body,
                icon = when (sectionTitle) {
                    "Recomandare de tinuta" -> Icons.Rounded.Checkroom
                    "Accesorii utile" -> Icons.Rounded.Umbrella
                    else -> Icons.Rounded.Thermostat
                },
                tint = when (sectionTitle) {
                    "Recomandare de tinuta" -> OutfitBlue
                    "Accesorii utile" -> OutfitAccent
                    else -> OutfitAccent
                }
            )
        }
    }
}

@Composable
private fun LoadingOutfitState() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = OutfitAccent,
            trackColor = Color.White.copy(alpha = 0.14f)
        )
        Text(
            text = "AI-ul pregateste recomandarea de tinuta...",
            color = Color.White.copy(alpha = 0.78f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmptyOutfitState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 21.sp
        )
    }
}

private fun Double?.temperatureText(): String {
    return this?.roundToInt()?.let { "$it$Degree" } ?: "--"
}

private fun resolveOutfitMood(
    weatherCode: Int?,
    temperature: Double?,
    precipitation: Double,
    snowfall: Double,
    isDay: Boolean
): OutfitWeatherMood {
    val code = weatherCode ?: -1

    return when {
        snowfall > 0.0 || code in 71..86 -> OutfitWeatherMood.SNOW
        code in 95..99 -> OutfitWeatherMood.STORM
        precipitation > 0.0 || code in 51..67 || code in 80..82 -> OutfitWeatherMood.RAIN
        temperature != null && temperature <= 8.0 -> OutfitWeatherMood.COLD
        isDay && (code == 0 || code == 1) -> OutfitWeatherMood.SUNNY
        temperature != null && temperature >= 23.0 -> OutfitWeatherMood.SUNNY
        else -> OutfitWeatherMood.MILD
    }
}

private fun outfitBackgroundColors(mood: OutfitWeatherMood): List<Color> {
    return when (mood) {
        OutfitWeatherMood.SUNNY -> listOf(
            Color(0xFF061625),
            Color(0xFF082034),
            Color(0xFF123B4E),
            Color(0xFF0B2C3C)
        )

        OutfitWeatherMood.STORM -> listOf(
            Color(0xFF050B1E),
            Color(0xFF101B38),
            Color(0xFF172A4C),
            Color(0xFF071727)
        )

        OutfitWeatherMood.SNOW -> listOf(
            Color(0xFF041522),
            Color(0xFF0A2638),
            Color(0xFF15384B),
            Color(0xFF061625)
        )

        OutfitWeatherMood.RAIN -> listOf(
            Color(0xFF041420),
            Color(0xFF092235),
            Color(0xFF0C3044),
            Color(0xFF061625)
        )

        OutfitWeatherMood.COLD -> listOf(
            Color(0xFF041522),
            Color(0xFF082032),
            Color(0xFF0E3448),
            Color(0xFF061625)
        )

        OutfitWeatherMood.MILD -> listOf(
            OutfitBackgroundDeep,
            OutfitBackground,
            Color(0xFF092438)
        )
    }
}

private fun heroGradient(mood: OutfitWeatherMood): List<Color> {
    return when (mood) {
        OutfitWeatherMood.SUNNY -> listOf(
            Color(0xFF172B3B),
            Color(0xFF344454),
            Color(0xFFB9783E)
        )

        OutfitWeatherMood.STORM -> listOf(
            Color(0xFF11162B),
            Color(0xFF232B54),
            Color(0xFF4D5B84)
        )

        OutfitWeatherMood.SNOW -> listOf(
            Color(0xFF173044),
            Color(0xFF3C5D70),
            Color(0xFF9CB5C2)
        )

        OutfitWeatherMood.RAIN -> listOf(
            Color(0xFF102335),
            Color(0xFF213D52),
            Color(0xFF51718B)
        )

        OutfitWeatherMood.COLD -> listOf(
            Color(0xFF122536),
            Color(0xFF244257),
            Color(0xFF64879A)
        )

        OutfitWeatherMood.MILD -> listOf(
            Color(0xFF172B3B),
            Color(0xFF2F495D),
            Color(0xFF6D7F7D)
        )
    }
}

private fun moodAccent(mood: OutfitWeatherMood): Color {
    return when (mood) {
        OutfitWeatherMood.STORM -> OutfitPurple
        OutfitWeatherMood.SNOW -> Color(0xFFB7E8FF)
        OutfitWeatherMood.RAIN -> OutfitBlue
        OutfitWeatherMood.COLD -> Color(0xFF9DDCFF)
        else -> OutfitAccent
    }
}

private fun skyLabel(
    weatherCode: Int?,
    cloudCover: Int?,
    precipitation: Double,
    snowfall: Double
): String {
    val code = weatherCode ?: -1

    return when {
        snowfall > 0.0 || code in 71..86 -> "Ninsoare"
        code in 95..99 -> "Furtuna"
        precipitation > 0.0 || code in 51..67 || code in 80..82 -> "Ploaie"
        code == 0 || (cloudCover != null && cloudCover <= 20) -> "Cer senin"
        code in 1..3 || (cloudCover != null && cloudCover <= 70) -> "Partial noros"
        code in 45..48 -> "Ceata"
        cloudCover != null && cloudCover > 70 -> "Noros"
        else -> "Vreme actuala"
    }
}

private fun outfitTagLine(
    mood: OutfitWeatherMood,
    temperature: Double?,
    uvIndex: Double?
): String {
    return when {
        mood == OutfitWeatherMood.STORM -> "Alege piese comode si rezistente la vant"
        mood == OutfitWeatherMood.RAIN -> "Pregatit pentru umezeala si drumuri rapide"
        mood == OutfitWeatherMood.SNOW -> "Straturi calde pentru confort pe tot parcursul zilei"
        mood == OutfitWeatherMood.COLD -> "Tinuta calda, simpla si usor de ajustat"
        temperature != null && temperature >= 25.0 && (uvIndex ?: 0.0) >= 5.0 -> "Perfect pentru tinute usoare si culori deschise"
        temperature != null && temperature >= 22.0 -> "Perfect pentru tinute usoare"
        else -> "Recomandare personalizata pentru vremea de acum"
    }
}

private fun quickOutfitPieces(
    temperature: Double?,
    precipitation: Double,
    snowfall: Double,
    windSpeed: Double?,
    mood: OutfitWeatherMood
): List<OutfitPiece> {
    if (temperature == null) {
        return listOf(
            OutfitPiece("Top lejer", "adaptabil", OutfitAccent),
            OutfitPiece("Jeans", "comozi", OutfitBlue),
            OutfitPiece("Sneakers", "de zi", OutfitAccent)
        )
    }

    if (snowfall > 0.0 || mood == OutfitWeatherMood.SNOW) {
        return listOf(
            OutfitPiece("Pulover", "calduros", OutfitAccent),
            OutfitPiece("Geaca", "groasa", OutfitBlue),
            OutfitPiece("Ghete", "aderente", OutfitAccent)
        )
    }

    if (precipitation > 0.0 || mood == OutfitWeatherMood.RAIN || mood == OutfitWeatherMood.STORM) {
        return listOf(
            OutfitPiece("Geaca", "impermeabila", OutfitBlue),
            OutfitPiece("Pantaloni", "lungi", OutfitAccent),
            OutfitPiece("Sneakers", "rezistenti", OutfitBlue)
        )
    }

    if ((windSpeed ?: 0.0) >= 18.0 && temperature < 24.0) {
        return listOf(
            OutfitPiece("Bluza", "subtire", OutfitBlue),
            OutfitPiece("Jeans", "lejeri", OutfitAccent),
            OutfitPiece("Sneakers", "comozi", OutfitBlue)
        )
    }

    return when {
        temperature >= 28.0 -> listOf(
            OutfitPiece("Tricou", "deschis", OutfitAccent),
            OutfitPiece("Pantaloni", "scurti", OutfitBlue),
            OutfitPiece("Sneakers", "albi", OutfitAccent)
        )

        temperature >= 23.0 -> listOf(
            OutfitPiece("Tricou alb", "respirabil", OutfitAccent),
            OutfitPiece("Blugi", "subtiri", OutfitBlue),
            OutfitPiece("Sneakers", "albi", OutfitAccent)
        )

        temperature >= 18.0 -> listOf(
            OutfitPiece("Camasa", "subtire", OutfitAccent),
            OutfitPiece("Jeans", "lejeri", OutfitBlue),
            OutfitPiece("Sneakers", "comozi", OutfitAccent)
        )

        temperature >= 10.0 -> listOf(
            OutfitPiece("Bluza", "lejera", OutfitBlue),
            OutfitPiece("Geaca", "usoara", OutfitAccent),
            OutfitPiece("Pantofi", "comozi", OutfitBlue)
        )

        else -> listOf(
            OutfitPiece("Pulover", "cald", OutfitAccent),
            OutfitPiece("Geaca", "calduroasa", OutfitBlue),
            OutfitPiece("Ghete", "comode", OutfitAccent)
        )
    }
}

private fun calculateOutfitScore(
    temperature: Double?,
    apparentTemperature: Double?,
    precipitation: Double,
    snowfall: Double,
    windSpeed: Double?,
    uvIndex: Double?
): Int {
    if (temperature == null) return 0

    val feelsLike = apparentTemperature ?: temperature
    var score = 96

    score -= when {
        feelsLike < 0.0 -> 18
        feelsLike < 8.0 -> 13
        feelsLike > 34.0 -> 16
        feelsLike > 29.0 -> 8
        else -> 0
    }

    score -= when {
        snowfall > 0.0 -> 14
        precipitation >= 3.0 -> 13
        precipitation > 0.0 -> 8
        else -> 0
    }

    if ((windSpeed ?: 0.0) >= 25.0) score -= 8
    if ((uvIndex ?: 0.0) >= 7.0) score -= 3

    return score.coerceIn(70, 98)
}

private fun comfortLevel(score: Int?): String {
    return when {
        score == null -> "--"
        score >= 92 -> "Excelent"
        score >= 84 -> "Foarte bun"
        score >= 76 -> "Bun"
        else -> "Atentie"
    }
}

private fun layersNeeded(
    temperature: Double?,
    precipitation: Double,
    snowfall: Double
): String {
    if (temperature == null) return "--"

    val baseLayers = when {
        temperature < 5.0 -> 3
        temperature < 13.0 -> 2
        temperature < 20.0 -> 1
        else -> 0
    }
    val weatherLayers = when {
        snowfall > 0.0 -> 3
        precipitation > 0.0 -> 1
        else -> 0
    }

    return maxOf(baseLayers, weatherLayers).toString()
}

private fun uvAdvice(uvIndex: Double?): String {
    return when {
        uvIndex == null -> "--"
        uvIndex >= 8.0 -> "SPF 50"
        uvIndex >= 5.0 -> "SPF 30"
        uvIndex >= 3.0 -> "Ochelari"
        else -> "Fara extra"
    }
}

private fun bonusAdvice(
    mood: OutfitWeatherMood,
    temperature: Double?,
    uvIndex: Double?
): String {
    return when {
        mood == OutfitWeatherMood.STORM -> "Alege culori inchise, incaltaminte stabila si o piesa impermeabila."
        mood == OutfitWeatherMood.RAIN -> "O tinuta practica, cu strat impermeabil, va arata bine si ramane comoda."
        mood == OutfitWeatherMood.SNOW -> "Materialele calduroase si incaltamintea aderenta sunt cele mai importante azi."
        mood == OutfitWeatherMood.COLD -> "Un strat suplimentar te ajuta sa ramai confortabil fara sa incarci tinuta."
        temperature != null && temperature >= 24.0 && (uvIndex ?: 0.0) >= 5.0 ->
            "Conditiile sunt excelente pentru culori deschise, materiale lejere si accesorii de soare."
        temperature != null && temperature >= 22.0 ->
            "Azi merge foarte bine o tinuta aerisita, curata si usor de purtat."
        else -> "Alege piese simple, flexibile, care se pot adapta usor pe parcursul zilei."
    }
}
