package com.example.weathersimulator.ui.screens.simulator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.weathersimulator.ui.sensors.audio.AudioController
import com.example.weathersimulator.R
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.layout.Row
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntOffset
import kotlin.math.sin
import kotlin.math.PI
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersimulator.sensors.pressure.PressureTrend
import com.example.weathersimulator.sensors.pressure.PressureViewModel
import androidx.compose.foundation.layout.*
import com.example.weathersimulator.ui.navigation.Routes



fun getBackgroundColor(cloudCoverage: Float): Color {
    return when {
        cloudCoverage < 40f ->
            Color(0xFF1565C0)

        cloudCoverage in 40f..80f ->
            Color(0xFF90CAF9)

        cloudCoverage > 80f ->
            Color(0xFFCFD8DC)

        else -> Color(0xFF64B5F6)
    }
}

private fun computeWeatherDescription(
    temperature: Float,
    humidity: Float,
    pressure: Float,
    wind: Float,
    cloudCoverage: Float
): Pair<Int, String> {
    val cc = (cloudCoverage / 20f).roundToInt() * 20f
    return when {
        humidity > 95 && pressure < 1010 && temperature in 0f..15f ->
            R.drawable.icon_weather_11 to "Ceață"

        temperature < 0 && humidity > 70 ->
            R.drawable.icon_weather_22 to "Ninsoare"

        humidity > 90 && wind >= 50 && pressure < 1000 && cloudCoverage > 60->
            R.drawable.icon_weather_17 to "Furtună"

        humidity > 70 && wind >= 40 && pressure in 995f..1005f && temperature > 20 && cloudCoverage in 20f..60f ->
            R.drawable.icon_weather_16 to "Furtună cu soare"

        humidity > 85 && pressure < 1005 && cloudCoverage >= 80 ->
            R.drawable.icon_weather_12 to "Ploaie"

        cc == 0f   -> R.drawable.icon_weather_01 to "Însorit"
        cc == 20f  -> R.drawable.icon_weather_02 to "Predominant însorit"
        cc == 40f  -> R.drawable.icon_weather_03 to "Parțial însorit"
        cc == 60f  -> R.drawable.icon_weather_04 to "Nori și soare"
        cc == 80f  -> R.drawable.icon_weather_06 to "Predominant noros"
        cc == 100f -> R.drawable.icon_weather_07 to "Noros"

        else -> R.drawable.icon_weather_39 to "Condiții variabile"
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    navController: NavController,
    pressureViewModel: PressureViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        Log.d("SimulatorScreen", "SimulatorScreen started")
    }

    var temperature by remember { mutableStateOf(20f) }
    var humidity by remember { mutableStateOf(50f) }
    var pressure by remember { mutableStateOf(1013f) }
    var wind by remember { mutableStateOf(10f) }
    var cloudCoverage by remember { mutableStateOf(0f) }

    val context = LocalContext.current
    val audio = remember { AudioController(context) }
    val pressureSensorState by pressureViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        pressureViewModel.start()
    }

    // pragul de vant puternic (km/h)
    val strongWindThreshold = 50f

    // pentru a porni/opri sunetele de fundal
    var soundsEnabled by remember { mutableStateOf(true) }
    var useBarometer by remember { mutableStateOf(true) }

    // derivăm condiția meteo din valorile actuale
    val (_, descriptionNow) = remember(temperature, humidity, pressure, wind, cloudCoverage) {
        computeWeatherDescription(temperature, humidity, pressure, wind, cloudCoverage)
    }
    val sensorPressure = pressureSensorState.pressureHpa
    LaunchedEffect(useBarometer, sensorPressure) {
        if (useBarometer && sensorPressure != null) {
            pressure = sensorPressure
        }
    }

    // Vant
    LaunchedEffect(wind, soundsEnabled, descriptionNow) {
        if (!soundsEnabled) {
            audio.stopWind()
            return@LaunchedEffect
        }

        val isStorm = (descriptionNow == "Furtună" || descriptionNow == "Furtună cu soare")
        val isRain = (
                descriptionNow == "Ploaie" ||
                        descriptionNow == "Averse" ||
                        descriptionNow == "Ploaie cu soare"
                )
        if (isStorm || isRain) {
            audio.stopWind()
            return@LaunchedEffect
        }


        if (wind >= strongWindThreshold) {
            val t = ((wind - strongWindThreshold) / (120f - strongWindThreshold)).coerceIn(0f, 1f)
            val volume = 0.4f + 0.6f * t
            audio.startWindLoop(resId = R.raw.strong_wind, volume = volume)
        } else {
            audio.stopWind()
        }
    }

    // Ploaie (loop) când sunt condiții de ploaie
    LaunchedEffect(descriptionNow, soundsEnabled) {
        if (!soundsEnabled) {
            audio.stopRain()
            return@LaunchedEffect
        }

        if (descriptionNow == "Ploaie") {
            audio.startRainLoop(resId = R.raw.rain, volume = 0.6f)
        } else {
            audio.stopRain()
        }
    }

    // Tunet, cand sunt conditii de furtuna
    //R = ID-ul intern Android pentru fișierul thunder.mp3 din res/raw
    LaunchedEffect(descriptionNow, soundsEnabled) {
        val isStorm = (descriptionNow == "Furtună" || descriptionNow == "Furtună cu soare")

        if (!soundsEnabled) {
            audio.stopThunder()
            return@LaunchedEffect
        }

        if (isStorm) {
            audio.playThunder(R.raw.thunder)
        } else {
            audio.stopThunder()
        }
    }


    // 3) Cleanup când ieși din ecran
    DisposableEffect(Unit) {
        onDispose {
            audio.releaseAll()
        }
    }

    LaunchedEffect(soundsEnabled) {
        if (!soundsEnabled) {
            audio.releaseAll() // oprește tot (wind + rain + thunder)
        }
    }


    val backgroundColor by animateColorAsState(
        targetValue = getBackgroundColor(cloudCoverage),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = ""
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Weather Simulator AI") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Switch(
                        checked = soundsEnabled,
                        onCheckedChange = { soundsEnabled = it }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // 1) FUNDAL ANIMAT (spate)
            AnimatedSky(
                cloudCoverage = cloudCoverage,
                isStormy = (descriptionNow == "Furtună" || descriptionNow == "Furtună cu soare"),
                pressureTrend = pressureSensorState.trendLabel,
                windSpeed = wind,
                humidity = humidity
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Alege valorile atmosferice:", fontSize = 20.sp, color = Color.Black)

                val sliderColors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFB08A),
                    activeTrackColor = Color(0xFFFFB08A),
                    inactiveTrackColor = Color(0xFF4F4F4F)
                )

                Text(text = "Temperatura: ${temperature.toInt()}°C", fontSize = 18.sp, color = Color.Black)
                Slider(
                    value = temperature,
                    onValueChange = {
                        temperature = it.roundToInt().toFloat()
                    },
                    valueRange = -20f..50f,
                    steps = 69,
                    colors = sliderColors
                )

                Text(text = "Umiditate: ${humidity.toInt()}%", fontSize = 18.sp, color = Color.Black)
                Slider(
                    value = humidity,
                    onValueChange = { v ->
                        humidity = (v / 10f).roundToInt() * 10f
                    },
                    valueRange = 0f..100f,
                    steps = 9,
                    colors = sliderColors
                )

                //Text(text = "Presiune : ${pressure.toInt()} hPa", fontSize = 18.sp)
                val isBarometerAvailable = pressureSensorState.isAvailable
                val trend = pressureSensorState.trendLabel
                val trendHpaPerHour = pressureSensorState.trendHpaPerHour

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (useBarometer && sensorPressure != null)
                            "Presiune (LIVE): ${sensorPressure.toInt()} hPa"
                        else
                            "Presiune: ${pressure.toInt()} hPa",
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    // Toggle doar dacă există senzor
                    if (isBarometerAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "LIVE", fontSize = 14.sp, color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = useBarometer,
                                onCheckedChange = { useBarometer = it }
                            )
                        }
                    }
                }

                Slider(
                    value = pressure,
                    onValueChange = { p ->
                        pressure = (p / 10f).roundToInt() * 10f
                    },
                    valueRange = 950f..1050f,
                    steps = 9,
                    enabled = !(useBarometer && sensorPressure != null),
                    colors = sliderColors
                )
                if (isBarometerAvailable && trend != PressureTrend.UNKNOWN && trendHpaPerHour != null) {
                    Text(
                        text = "Trend: ${"%.1f".format(trendHpaPerHour)} hPa/oră • $trend",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                } else if (!isBarometerAvailable) {
                    Text(
                        text = "Barometru indisponibil. Folosește modul manual (slider).",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }

                Text(text = "Viteza vântului: ${wind.toInt()} km/h", fontSize = 18.sp, color = Color.Black)
                Slider(
                    value = wind,
                    onValueChange = { w ->
                        wind = (w / 10f).roundToInt() * 10f
                    },
                    valueRange = 0f..120f,
                    steps = 11,
                    colors = sliderColors
                )

                Text(text = "Acoperire nori: ${cloudCoverage.toInt()}%", fontSize = 18.sp, color = Color.Black)
                Slider(
                    value = cloudCoverage,
                    onValueChange = { value ->
                        cloudCoverage = (value / 20f).roundToInt() * 20f
                    },
                    valueRange = 0f..100f,
                    steps = 4,
                    colors = sliderColors
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { navController.navigate(Routes.AI_SIMULATION) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simulare bazată pe AI")
                }

                WeatherDisplayCard(
                    temperature = temperature,
                    humidity = humidity,
                    pressure = pressure,
                    wind = wind,
                    cloudCoverage = cloudCoverage
                )
            }
        }
    }
}

@Composable
fun WeatherDisplayCard(
    temperature: Float,
    humidity: Float,
    pressure: Float,
    wind: Float,
    cloudCoverage: Float
) {
    val (iconRes, description) = remember(temperature, humidity, pressure, wind, cloudCoverage) {
        computeWeatherDescription(temperature, humidity, pressure, wind, cloudCoverage)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = description,
                modifier = Modifier.size(98.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun AnimatedSky(
    cloudCoverage: Float,
    isStormy: Boolean,
    pressureTrend: PressureTrend,
    windSpeed: Float,
    humidity: Float
) {
    val infinite = rememberInfiniteTransition(label = "sky")
    val windFactor = (windSpeed / 120f).coerceIn(0f, 1f)
    val cloudDriftDuration = (26000f - windFactor * 17000f).roundToInt().coerceIn(7000, 26000)

    // Soarele pulsează (lumina)
    val sunPulse by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunPulse"
    )

    val sunDrift by infinite.animateFloat(
        initialValue = -0.02f,
        targetValue = 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunDrift"
    )

    val sunRayRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(46000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRayRotation"
    )

    // Norii se mișcă
    val cloudMove by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(cloudDriftDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloudMove"
    )

    val cloudBob by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudBob"
    )

    val stormPulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stormPulse"
    )

    val cloudAlphaBase = (cloudCoverage / 100f).coerceIn(0f, 1f)

    // dacă e furtună sau presiunea scade -> mai multă “dramă” la nori
    val stormBoost = when {
        isStormy -> 0.35f
        pressureTrend == PressureTrend.RAPID_FALL -> 0.25f
        pressureTrend == PressureTrend.FALLING -> 0.12f
        else -> 0f
    }

    val cloudsAlpha = (cloudAlphaBase + stormBoost).coerceIn(0f, 1f)
    val sunAlpha = (1f - cloudAlphaBase * 0.9f).coerceIn(0f, 1f)
    val hazeAlpha = (((humidity - 60f) / 40f).coerceIn(0f, 1f) * (1f - cloudsAlpha * 0.5f))

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.18f),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.1f)
                )
            )
        )

        // 🌞 Sun (sus-stânga) cu glow
        if (sunAlpha > 0.02f) {
            val center = Offset(
                x = w * (0.18f + sunDrift),
                y = h * (0.18f + (0.015f * sunDrift))
            )
            val r = size.minDimension * 0.10f * sunPulse

            drawSunWithRays(
                center = center,
                radius = r,
                sunAlpha = sunAlpha,
                rayRotation = sunRayRotation
            )
        }

        // ☁️ Clouds (straturi)
        if (cloudsAlpha > 0.02f) {
            fun xPos(speed: Float): Float {
                val speedBoost = speed + (windFactor * 0.75f)
                return (-0.35f * w) + (cloudMove * (1.85f * w) * speedBoost)
            }

            val cloudColor = if (isStormy || pressureTrend == PressureTrend.RAPID_FALL)
                Color(0xFF90A4AE).copy(alpha = cloudsAlpha * 0.9f)
            else
                Color.White.copy(alpha = cloudsAlpha * 0.85f)

            drawCloudStrip(
                baseX = xPos(0.60f),
                baseY = h * (0.20f + cloudBob * 0.01f),
                scale = 1.2f,
                color = cloudColor,
                wobble = cloudBob
            )
            drawCloudStrip(
                baseX = xPos(0.90f),
                baseY = h * (0.33f - cloudBob * 0.012f),
                scale = 1.5f,
                color = cloudColor.copy(alpha = cloudColor.alpha * 0.90f),
                wobble = -cloudBob
            )
            drawCloudStrip(
                baseX = xPos(1.15f),
                baseY = h * (0.47f + cloudBob * 0.009f),
                scale = 1.8f,
                color = cloudColor.copy(alpha = cloudColor.alpha * 0.80f),
                wobble = cloudBob
            )
            drawCloudStrip(
                baseX = xPos(1.35f),
                baseY = h * (0.57f - cloudBob * 0.008f),
                scale = 1.35f,
                color = cloudColor.copy(alpha = cloudColor.alpha * 0.68f),
                wobble = -cloudBob
            )
        }

        if (hazeAlpha > 0.02f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = hazeAlpha * 0.18f),
                        Color.White.copy(alpha = hazeAlpha * 0.08f),
                        Color.Transparent
                    ),
                    startY = h * 0.05f,
                    endY = h * 0.8f
                )
            )
        }

        if (isStormy) {
            drawRect(color = Color.White.copy(alpha = 0.05f * stormPulse))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSunWithRays(
    center: Offset,
    radius: Float,
    sunAlpha: Float,
    rayRotation: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF59D).copy(alpha = 0.58f * sunAlpha),
                Color(0xFFFFE082).copy(alpha = 0.2f * sunAlpha),
                Color.Transparent
            ),
            center = center,
            radius = radius * 3f
        ),
        radius = radius * 3f,
        center = center
    )

    for (i in 0 until 12) {
        rotate(degrees = rayRotation + i * 30f, pivot = center) {
            drawLine(
                color = Color(0xFFFFD54F).copy(alpha = 0.28f * sunAlpha),
                start = Offset(center.x, center.y - radius * 1.45f),
                end = Offset(center.x, center.y - radius * 2.15f),
                strokeWidth = radius * 0.12f
            )
        }
    }

    drawCircle(
        color = Color(0xFFFFF176).copy(alpha = 0.92f * sunAlpha),
        radius = radius,
        center = center
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.28f * sunAlpha),
        radius = radius * 0.55f,
        center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloudStrip(
    baseX: Float,
    baseY: Float,
    scale: Float,
    color: Color,
    wobble: Float
) {
    val r = size.minDimension * 0.06f * scale
    val x = baseX
    val y = baseY + (r * 0.08f * wobble)

    val shadowColor = Color(0xFF37474F).copy(alpha = color.alpha * 0.12f)
    val highlightColor = Color.White.copy(alpha = color.alpha * 0.25f)

    // “pufuri”
    drawCircle(color = color, radius = r * 0.95f, center = Offset(x + r * 1.0f, y))
    drawCircle(color = color, radius = r * 1.15f, center = Offset(x + r * 2.1f, y - r * 0.35f))
    drawCircle(color = color, radius = r * 1.0f, center = Offset(x + r * 3.3f, y))
    drawCircle(color = color, radius = r * 0.85f, center = Offset(x + r * 4.3f, y + r * 0.1f))

    // “corp” (oval)
    drawRoundRect(
        color = color,
        topLeft = Offset(x + r * 0.6f, y),
        size = Size(width = r * 4.0f, height = r * 1.4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
    )

    // umbre + highlight pentru volum
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(x + r * 0.8f, y + r * 0.8f),
        size = Size(width = r * 3.6f, height = r * 0.65f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
    )

    drawCircle(
        color = highlightColor,
        radius = r * 0.48f,
        center = Offset(x + r * 1.55f, y - r * 0.24f)
    )
}

