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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width




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
            R.drawable.icon_weather_15 to "Furtună"

        humidity > 70 && wind >= 40 && pressure in 995f..1005f && temperature > 20 && cloudCoverage in 20f..60f ->
            R.drawable.icon_weather_16 to "Furtună cu soare"

        humidity > 85 && pressure < 1005 && cloudCoverage >= 80 ->
            R.drawable.icon_weather_18 to "Ploaie"

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
                title = {
                    Text(
                        text = "Weather Simulator AI",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sunet",
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = soundsEnabled,
                            onCheckedChange = { soundsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.White.copy(alpha = 0.45f),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.22f)
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
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
            WeatherScene(
                cloudCoverage = cloudCoverage,
                isStormy = (descriptionNow == "Furtună" || descriptionNow == "Furtună cu soare"),
                pressureTrend = pressureSensorState.trendLabel,
                windSpeed = wind,
                humidity = humidity,
                temperature = temperature,
                weatherDescription = descriptionNow
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Simulează vremea în timp real",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Ajustează atmosfera și vezi cum cerul prinde viață.",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.88f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.16f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Alege valorile atmosferice",
                            fontSize = 20.sp,
                            color = Color.White
                        )

                        val sliderColors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD180),
                            activeTrackColor = Color(0xFFFFD180),
                            inactiveTrackColor = Color.White.copy(alpha = 0.35f)
                        )

                        Text(
                            text = "Temperatura: ${temperature.toInt()}°C",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Slider(
                            value = temperature,
                            onValueChange = {
                                temperature = it.roundToInt().toFloat()
                            },
                            valueRange = -20f..50f,
                            steps = 69,
                            colors = sliderColors
                        )

                        Text(
                            text = "Umiditate: ${humidity.toInt()}%",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Slider(
                            value = humidity,
                            onValueChange = { v ->
                                humidity = (v / 10f).roundToInt() * 10f
                            },
                            valueRange = 0f..100f,
                            steps = 9,
                            colors = sliderColors
                        )

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
                                color = Color.White
                            )

                            if (isBarometerAvailable) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
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
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        } else if (!isBarometerAvailable) {
                            Text(
                                text = "Barometru indisponibil. Folosește modul manual (slider).",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Text(
                            text = "Viteza vântului: ${wind.toInt()} km/h",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Slider(
                            value = wind,
                            onValueChange = { w ->
                                wind = (w / 10f).roundToInt() * 10f
                            },
                            valueRange = 0f..120f,
                            steps = 11,
                            colors = sliderColors
                        )

                        Text(
                            text = "Acoperire nori: ${cloudCoverage.toInt()}%",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Slider(
                            value = cloudCoverage,
                            onValueChange = { value ->
                                cloudCoverage = (value / 20f).roundToInt() * 20f
                            },
                            valueRange = 0f..100f,
                            steps = 4,
                            colors = sliderColors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { navController.navigate(Routes.AI_SIMULATION) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.20f),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 14.dp
                    )
                ) {
                    Text(
                        text = "Simulare bazată pe AI",
                        fontSize = 17.sp
                    )
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
            .height(220.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.35f),
                shape = RoundedCornerShape(28.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.18f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Condiția meteo simulată",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.92f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = description,
                modifier = Modifier.size(104.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = description,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${temperature.toInt()}°C • ${humidity.toInt()}% • ${wind.toInt()} km/h",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
fun WeatherSliderLabel(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.92f)
        )

        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun AnimatedSky(
    cloudCoverage: Float,
    isStormy: Boolean,
    pressureTrend: PressureTrend,
    windSpeed: Float,
    humidity: Float,
    weatherDescription: String
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

    val conditionCloudBoost = when (weatherDescription) {
        "Însorit" -> -0.18f
        "Predominant însorit" -> -0.10f
        "Parțial însorit" -> -0.04f
        "Nori și soare" -> 0.06f
        "Predominant noros" -> 0.16f
        "Noros" -> 0.24f
        "Ploaie", "Ploaie ușoară", "Ploaie intensa" -> 0.22f
        "Furtună", "Furtună cu soare" -> 0.30f
        "Ninsoare", "Ninsoare usoara", "Ninsoare intensa" -> 0.18f
        "Ceață" -> 0.10f
        else -> 0f
    }

    val stormBoost = when {
        isStormy -> 0.35f
        pressureTrend == PressureTrend.RAPID_FALL -> 0.25f
        pressureTrend == PressureTrend.FALLING -> 0.12f
        else -> 0f
    }

    val sunConditionFactor = when (weatherDescription) {
        "Însorit" -> 1.0f
        "Predominant însorit" -> 0.92f
        "Parțial însorit" -> 0.78f
        "Nori și soare" -> 0.62f
        "Predominant noros" -> 0.32f
        "Noros" -> 0.18f
        "Ploaie", "Ploaie ușoară", "Ploaie intensa" -> 0.10f
        "Furtună" -> 0.04f
        "Furtună cu soare" -> 0.18f
        "Ninsoare", "Ninsoare usoara", "Ninsoare intensa" -> 0.22f
        "Ceață" -> 0.12f
        else -> 0.7f
    }

    val hazeBoost = when (weatherDescription) {
        "Ceață" -> 0.55f
        "Ninsoare", "Ninsoare usoara", "Ninsoare intensa" -> 0.18f
        "Ploaie", "Ploaie ușoară", "Ploaie intensa" -> 0.10f
        "Predominant noros", "Noros" -> 0.08f
        else -> 0f
    }

    val cloudsAlpha = (cloudAlphaBase + conditionCloudBoost + stormBoost).coerceIn(0f, 1f)
    val sunAlpha = ((1f - cloudAlphaBase * 0.9f) * sunConditionFactor).coerceIn(0f, 1f)
    val hazeAlpha = (
        ((humidity - 60f) / 40f).coerceIn(0f, 1f) * (1f - cloudsAlpha * 0.35f) + hazeBoost
    ).coerceIn(0f, 1f)
    
    val skyColors = when (weatherDescription) {
        "Însorit" -> listOf(
            Color(0xFF2D77D3),
            Color(0xFF5FA9F0),
            Color(0xFFCFE8FF)
        )

        "Predominant însorit" -> listOf(
            Color(0xFF367DCE),
            Color(0xFF75B2EC),
            Color(0xFFD8EDFF)
        )

        "Parțial însorit" -> listOf(
            Color(0xFF3A83D4),
            Color(0xFF82BDEB),
            Color(0xFFDDEDF8)
        )

        "Nori și soare" -> listOf(
            Color(0xFF5578B0),
            Color(0xFF8FA8C3),
            Color(0xFFD7E0E8)
        )

        "Predominant noros" -> listOf(
            Color(0xFF5F748A),
            Color(0xFF95A5B3),
            Color(0xFFD6DEE4)
        )

        "Noros" -> listOf(
            Color(0xFF4E6276),
            Color(0xFF8598A8),
            Color(0xFFC8D2DA)
        )

        "Ploaie", "Ploaie ușoară", "Ploaie intensa" -> listOf(
            Color(0xFF3B4F60),
            Color(0xFF657A8A),
            Color(0xFFAEBBC5)
        )

        "Furtună", "Furtună cu soare" -> listOf(
            Color(0xFF2A3846),
            Color(0xFF485C6D),
            Color(0xFF7A8C99)
        )

        "Ninsoare", "Ninsoare usoara", "Ninsoare intensa" -> listOf(
            Color(0xFF93A1AF),
            Color(0xFFC4D1DB),
            Color(0xFFF0F5FA)
        )

        "Ceață" -> listOf(
            Color(0xFF8FA0AC),
            Color(0xFFBCC7CF),
            Color(0xFFE8EDF1)
        )

        else -> listOf(
            Color(0xFF3E88D8),
            Color(0xFF7DB9EC),
            Color(0xFFD8ECFD)
        )
    }

    val isOvercastScene = weatherDescription in setOf(
        "Predominant noros",
        "Noros",
        "Ploaie",
        "Ploaie ușoară",
        "Ploaie intensa",
        "Furtună",
        "Furtună cu soare",
        "Ninsoare",
        "Ninsoare usoara",
        "Ninsoare intensa",
        "Ceață"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = skyColors,
                startY = 0f,
                endY = h
            )
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.08f)
                )
            )
        )

        if (!isOvercastScene) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF2C4).copy(alpha = 0.12f * sunAlpha),
                        Color.Transparent,
                        Color(0xFFFFE7B2).copy(alpha = 0.07f * sunAlpha)
                    ),
                    startY = h * 0.06f,
                    endY = h
                )
            )
        }

        val stormMoodAlpha = when (weatherDescription) {
            "Ploaie", "Ploaie ușoară", "Ploaie intensa" -> 0.18f
            "Furtună" -> 0.30f
            "Furtună cu soare" -> 0.22f
            else -> 0f
        }

        if (stormMoodAlpha > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1C313A).copy(alpha = stormMoodAlpha * 0.75f),
                        Color(0xFF37474F).copy(alpha = stormMoodAlpha * 0.55f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.58f
                )
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF263238).copy(alpha = stormMoodAlpha * 0.22f),
                        Color(0xFF102027).copy(alpha = stormMoodAlpha * 0.34f)
                    ),
                    startY = h * 0.45f,
                    endY = h
                )
            )
        }

        val coldLightAlpha = when (weatherDescription) {
            "Ninsoare", "Ninsoare usoara", "Ninsoare intensa" -> 0.22f
            "Ceață" -> 0.18f
            else -> 0f
        }

        if (coldLightAlpha > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD).copy(alpha = coldLightAlpha * 0.55f),
                        Color(0xFFF3F8FF).copy(alpha = coldLightAlpha * 0.35f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.52f
                )
            )

            drawCircle(
                color = Color(0xFFF8FBFF).copy(alpha = coldLightAlpha * 0.24f),
                radius = w * 0.34f,
                center = Offset(w * 0.78f, h * 0.20f)
            )
        }

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

        // Cloud layout is tuned per weather type to match visual references.
        if (cloudsAlpha > 0.02f) {
            fun xPos(speed: Float): Float {
                val speedBoost = speed + (windFactor * 0.75f)
                return (-0.35f * w) + (cloudMove * (1.85f * w) * speedBoost)
            }

            val cloudProfile = cloudProfileFor(
                weatherDescription = weatherDescription,
                alpha = cloudsAlpha,
                isStormy = isStormy,
                pressureTrend = pressureTrend
            )

            cloudProfile.bands.forEach { band ->
                val wobble = if (band.invertBob) -cloudBob else cloudBob
                drawCloudStrip(
                    baseX = xPos(band.speed),
                    baseY = h * (band.yRatio + wobble * band.bobFactor),
                    scale = band.scale,
                    color = cloudProfile.baseColor.copy(alpha = (cloudProfile.baseColor.alpha * band.alphaMul).coerceIn(0f, 1f)),
                    wobble = wobble,
                    flatness = band.flatness,
                    darkness = band.darkness,
                    puffiness = band.puffiness
                )
            }
        }

        if (hazeAlpha > 0.02f) {
            val hazeTopColor = when (weatherDescription) {
                "Ninsoare", "Ninsoare usoara", "Ninsoare intensa" -> Color(0xFFF3F8FF)
                "Ceață" -> Color(0xFFF5F7FA)
                else -> Color.White
            }

            val hazeMidColor = when (weatherDescription) {
                "Ninsoare", "Ninsoare usoara", "Ninsoare intensa" -> Color(0xFFEAF3FF)
                "Ceață" -> Color(0xFFECEFF1)
                else -> Color.White
            }

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        hazeTopColor.copy(alpha = hazeAlpha * 0.20f),
                        hazeMidColor.copy(alpha = hazeAlpha * 0.10f),
                        Color.Transparent
                    ),
                    startY = h * 0.05f,
                    endY = h * 0.8f
                )
            )
        }

        val fogAlpha = when (weatherDescription) {
            "Ceață" -> 0.30f
            "Predominant noros" -> 0.12f
            "Noros" -> 0.16f
            "Ninsoare", "Ninsoare usoara", "Ninsoare intensa" -> 0.14f
            else -> 0f
        }

        if (fogAlpha > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = fogAlpha * 0.40f),
                        Color.White.copy(alpha = fogAlpha * 0.70f)
                    ),
                    startY = h * 0.38f,
                    endY = h
                )
            )

            drawCircle(
                color = Color.White.copy(alpha = fogAlpha * 0.22f),
                radius = w * 0.42f,
                center = Offset(w * 0.25f, h * 0.78f)
            )

            drawCircle(
                color = Color.White.copy(alpha = fogAlpha * 0.18f),
                radius = w * 0.36f,
                center = Offset(w * 0.72f, h * 0.82f)
            )

            drawCircle(
                color = Color.White.copy(alpha = fogAlpha * 0.14f),
                radius = w * 0.30f,
                center = Offset(w * 0.52f, h * 0.88f)
            )
        }

        if (isStormy) {
            drawRect(color = Color.White.copy(alpha = 0.05f * stormPulse))
        }
    }
}

private data class CloudBandSpec(
    val speed: Float,
    val yRatio: Float,
    val scale: Float,
    val alphaMul: Float,
    val flatness: Float,
    val darkness: Float,
    val puffiness: Float,
    val bobFactor: Float,
    val invertBob: Boolean
)

private data class CloudProfile(
    val baseColor: Color,
    val bands: List<CloudBandSpec>
)

private fun cloudProfileFor(
    weatherDescription: String,
    alpha: Float,
    isStormy: Boolean,
    pressureTrend: PressureTrend
): CloudProfile {
    val pressureDarkBoost = when (pressureTrend) {
        PressureTrend.RAPID_FALL -> 0.14f
        PressureTrend.FALLING -> 0.08f
        else -> 0f
    }

    return when (weatherDescription) {
        "Însorit" -> CloudProfile(
            baseColor = Color(0xFFF6FBFF).copy(alpha = alpha * 0.30f),
            bands = listOf(
                CloudBandSpec(0.58f, 0.18f, 0.78f, 0.90f, 0.12f, 0.82f, 0.22f, 0.006f, false),
                CloudBandSpec(1.10f, 0.28f, 0.66f, 0.75f, 0.10f, 0.80f, 0.18f, 0.004f, true)
            )
        )

        "Predominant însorit" -> CloudProfile(
            baseColor = Color(0xFFF3F8FE).copy(alpha = alpha * 0.46f),
            bands = listOf(
                CloudBandSpec(0.56f, 0.18f, 0.92f, 1.00f, 0.16f, 0.86f, 0.34f, 0.008f, false),
                CloudBandSpec(0.92f, 0.29f, 0.98f, 0.88f, 0.14f, 0.84f, 0.30f, 0.009f, true),
                CloudBandSpec(1.32f, 0.42f, 0.82f, 0.72f, 0.18f, 0.88f, 0.26f, 0.006f, false)
            )
        )

        "Parțial însorit" -> CloudProfile(
            baseColor = Color(0xFFF2F7FD).copy(alpha = alpha * 0.66f),
            bands = listOf(
                CloudBandSpec(0.54f, 0.16f, 1.18f, 1.00f, 0.24f, 0.92f, 0.58f, 0.010f, false),
                CloudBandSpec(0.88f, 0.28f, 1.34f, 0.95f, 0.30f, 0.94f, 0.62f, 0.011f, true),
                CloudBandSpec(1.14f, 0.41f, 1.24f, 0.84f, 0.34f, 0.98f, 0.56f, 0.009f, false),
                CloudBandSpec(1.36f, 0.54f, 1.06f, 0.68f, 0.36f, 1.00f, 0.50f, 0.007f, true)
            )
        )

        "Nori și soare" -> CloudProfile(
            baseColor = Color(0xFFE8EEF4).copy(alpha = alpha * 0.78f),
            bands = listOf(
                CloudBandSpec(0.52f, 0.17f, 1.36f, 1.00f, 0.42f, 1.06f, 0.62f, 0.011f, false),
                CloudBandSpec(0.84f, 0.29f, 1.56f, 0.95f, 0.48f, 1.12f, 0.58f, 0.012f, true),
                CloudBandSpec(1.10f, 0.42f, 1.52f, 0.86f, 0.52f, 1.14f, 0.52f, 0.009f, false),
                CloudBandSpec(1.34f, 0.56f, 1.24f, 0.74f, 0.56f, 1.16f, 0.46f, 0.007f, true)
            )
        )

        "Predominant noros" -> CloudProfile(
            baseColor = Color(0xFFDBE4EC).copy(alpha = alpha * 0.88f),
            bands = listOf(
                CloudBandSpec(0.50f, 0.16f, 1.72f, 1.00f, 0.62f, 1.18f, 0.40f, 0.010f, false),
                CloudBandSpec(0.80f, 0.28f, 1.98f, 0.96f, 0.68f, 1.24f, 0.36f, 0.011f, true),
                CloudBandSpec(1.06f, 0.40f, 2.06f, 0.90f, 0.72f, 1.28f, 0.34f, 0.009f, false),
                CloudBandSpec(1.28f, 0.53f, 1.86f, 0.78f, 0.74f, 1.30f, 0.30f, 0.007f, true)
            )
        )

        "Noros" -> CloudProfile(
            baseColor = Color(0xFFD2DCE6).copy(alpha = alpha * 0.95f),
            bands = listOf(
                CloudBandSpec(0.48f, 0.14f, 1.96f, 1.00f, 0.74f, 1.28f + pressureDarkBoost, 0.30f, 0.009f, false),
                CloudBandSpec(0.76f, 0.25f, 2.22f, 0.98f, 0.80f, 1.36f + pressureDarkBoost, 0.28f, 0.010f, true),
                CloudBandSpec(0.98f, 0.36f, 2.28f, 0.94f, 0.84f, 1.42f + pressureDarkBoost, 0.26f, 0.009f, false),
                CloudBandSpec(1.20f, 0.48f, 2.06f, 0.86f, 0.86f, 1.44f + pressureDarkBoost, 0.24f, 0.008f, true),
                CloudBandSpec(1.40f, 0.60f, 1.80f, 0.74f, 0.88f, 1.46f + pressureDarkBoost, 0.22f, 0.006f, false)
            )
        )

        "Ploaie", "Ploaie ușoară", "Ploaie intensa" -> CloudProfile(
            baseColor = Color(0xFFC5D0D9).copy(alpha = alpha * 0.92f),
            bands = listOf(
                CloudBandSpec(0.52f, 0.13f, 2.05f, 1.00f, 0.82f, 1.40f + pressureDarkBoost, 0.26f, 0.008f, false),
                CloudBandSpec(0.78f, 0.24f, 2.36f, 0.98f, 0.88f, 1.48f + pressureDarkBoost, 0.24f, 0.010f, true),
                CloudBandSpec(1.00f, 0.35f, 2.40f, 0.94f, 0.90f, 1.52f + pressureDarkBoost, 0.22f, 0.009f, false),
                CloudBandSpec(1.20f, 0.46f, 2.18f, 0.86f, 0.92f, 1.56f + pressureDarkBoost, 0.22f, 0.007f, true),
                CloudBandSpec(1.38f, 0.58f, 1.92f, 0.76f, 0.94f, 1.60f + pressureDarkBoost, 0.20f, 0.006f, false)
            )
        )

        "Furtună" -> CloudProfile(
            baseColor = Color(0xFFB5C1CC).copy(alpha = alpha * 0.94f),
            bands = listOf(
                CloudBandSpec(0.56f, 0.12f, 2.25f, 1.00f, 0.90f, 1.70f, 0.22f, 0.006f, false),
                CloudBandSpec(0.80f, 0.22f, 2.58f, 0.98f, 0.94f, 1.80f, 0.20f, 0.007f, true),
                CloudBandSpec(1.00f, 0.33f, 2.66f, 0.96f, 0.95f, 1.88f, 0.18f, 0.007f, false),
                CloudBandSpec(1.18f, 0.44f, 2.46f, 0.88f, 0.96f, 1.92f, 0.18f, 0.006f, true),
                CloudBandSpec(1.36f, 0.56f, 2.10f, 0.78f, 0.96f, 1.96f, 0.16f, 0.005f, false)
            )
        )

        "Furtună cu soare" -> CloudProfile(
            baseColor = Color(0xFFC5CFD8).copy(alpha = alpha * if (isStormy) 0.88f else 0.78f),
            bands = listOf(
                CloudBandSpec(0.52f, 0.13f, 1.98f, 1.00f, 0.76f, 1.42f, 0.28f, 0.009f, false),
                CloudBandSpec(0.86f, 0.25f, 2.18f, 0.94f, 0.82f, 1.50f, 0.24f, 0.010f, true),
                CloudBandSpec(1.12f, 0.39f, 1.92f, 0.78f, 0.70f, 1.34f, 0.36f, 0.008f, false)
            )
        )

        "Ninsoare", "Ninsoare usoara", "Ninsoare intensa" -> CloudProfile(
            baseColor = Color(0xFFE8EEF4).copy(alpha = alpha * 0.86f),
            bands = listOf(
                CloudBandSpec(0.46f, 0.16f, 1.66f, 1.00f, 0.66f, 1.08f, 0.34f, 0.010f, false),
                CloudBandSpec(0.74f, 0.30f, 1.86f, 0.94f, 0.72f, 1.12f, 0.32f, 0.011f, true),
                CloudBandSpec(1.00f, 0.44f, 1.78f, 0.84f, 0.76f, 1.14f, 0.28f, 0.009f, false),
                CloudBandSpec(1.24f, 0.57f, 1.56f, 0.70f, 0.78f, 1.16f, 0.24f, 0.007f, true)
            )
        )

        "Ceață" -> CloudProfile(
            baseColor = Color(0xFFEFF3F7).copy(alpha = alpha * 0.72f),
            bands = listOf(
                CloudBandSpec(0.40f, 0.20f, 1.98f, 0.92f, 0.96f, 0.92f, 0.16f, 0.006f, false),
                CloudBandSpec(0.66f, 0.34f, 2.18f, 0.86f, 0.98f, 0.94f, 0.14f, 0.007f, true),
                CloudBandSpec(0.92f, 0.50f, 2.10f, 0.74f, 0.99f, 0.96f, 0.12f, 0.006f, false)
            )
        )

        else -> CloudProfile(
            baseColor = Color(0xFFE6EDF5).copy(alpha = alpha * 0.74f),
            bands = listOf(
                CloudBandSpec(0.54f, 0.18f, 1.22f, 1.00f, 0.32f, 0.98f, 0.48f, 0.009f, false),
                CloudBandSpec(0.90f, 0.31f, 1.40f, 0.90f, 0.38f, 1.04f, 0.46f, 0.010f, true),
                CloudBandSpec(1.18f, 0.46f, 1.26f, 0.76f, 0.42f, 1.06f, 0.42f, 0.008f, false)
            )
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSunWithRays(
    center: Offset,
    radius: Float,
    sunAlpha: Float,
    rayRotation: Float
) {
    val outerCoronaRadius = radius * 5.6f
    val midCoronaRadius = radius * 3.6f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF3B0).copy(alpha = 0.16f * sunAlpha),
                Color(0xFFFFE596).copy(alpha = 0.09f * sunAlpha),
                Color.Transparent
            ),
            center = center,
            radius = outerCoronaRadius
        ),
        radius = outerCoronaRadius,
        center = center
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF6C7).copy(alpha = 0.48f * sunAlpha),
                Color(0xFFFFE39A).copy(alpha = 0.18f * sunAlpha),
                Color.Transparent
            ),
            center = center,
            radius = midCoronaRadius
        ),
        radius = midCoronaRadius,
        center = center
    )

    for (i in 0 until 8) {
        rotate(degrees = rayRotation + i * 45f, pivot = center) {
            drawLine(
                color = Color(0xFFFFE8AB).copy(alpha = 0.11f * sunAlpha),
                start = Offset(center.x, center.y - radius * 1.38f),
                end = Offset(center.x, center.y - radius * 2.85f),
                strokeWidth = radius * 0.07f
            )
        }
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFEF1).copy(alpha = 0.96f * sunAlpha),
                Color(0xFFFFF2B0).copy(alpha = 0.93f * sunAlpha),
                Color(0xFFFFD777).copy(alpha = 0.88f * sunAlpha)
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.22f * sunAlpha),
        radius = radius * 0.45f,
        center = Offset(center.x - radius * 0.18f, center.y - radius * 0.20f)
    )

    drawCircle(
        color = Color(0xFFFFC96C).copy(alpha = 0.20f * sunAlpha),
        radius = radius * 0.84f,
        center = center
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloudStrip(
    baseX: Float,
    baseY: Float,
    scale: Float,
    color: Color,
    wobble: Float,
    flatness: Float = 0.40f,
    darkness: Float = 1f,
    puffiness: Float = 0.5f
) {
    val r = size.minDimension * 0.06f * scale
    val x = baseX
    val y = baseY + (r * 0.08f * wobble)
    val flat = flatness.coerceIn(0f, 1f)
    val puff = puffiness.coerceIn(0f, 1f)

    val shadowColor = Color(0xFF4A5C6D).copy(alpha = (color.alpha * 0.14f * darkness).coerceIn(0f, 1f))
    val midTone = color.copy(alpha = (color.alpha * (0.95f - flat * 0.10f)).coerceIn(0f, 1f))
    val lowerTone = Color(
        red = (color.red * (0.90f - flat * 0.10f)).coerceIn(0f, 1f),
        green = (color.green * (0.92f - flat * 0.12f)).coerceIn(0f, 1f),
        blue = (color.blue * (0.96f - flat * 0.08f)).coerceIn(0f, 1f),
        alpha = (color.alpha * (0.90f + flat * 0.04f)).coerceIn(0f, 1f)
    )
    val highlightColor = Color.White.copy(alpha = (color.alpha * (0.25f - flat * 0.07f)).coerceIn(0f, 1f))

    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(x + r * 0.6f, y + r * (0.82f + flat * 0.14f)),
        size = Size(width = r * 4.8f, height = r * (0.90f + flat * 0.22f)),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
    )

    val puffBoost = puff * 0.26f
    drawCircle(
        color = midTone,
        radius = r * (0.96f - flat * 0.18f + puffBoost),
        center = Offset(x + r * 1.0f, y + r * (0.20f + flat * 0.10f - puff * 0.08f))
    )
    drawCircle(
        color = midTone,
        radius = r * (1.25f - flat * 0.28f + puffBoost),
        center = Offset(x + r * 2.1f, y - r * (0.36f - flat * 0.18f + puff * 0.10f))
    )
    drawCircle(
        color = midTone,
        radius = r * (1.10f - flat * 0.24f + puffBoost),
        center = Offset(x + r * 3.25f, y - r * (0.06f - flat * 0.10f + puff * 0.06f))
    )
    drawCircle(
        color = midTone,
        radius = r * (0.92f - flat * 0.20f + puffBoost),
        center = Offset(x + r * 4.35f, y + r * (0.15f + flat * 0.08f - puff * 0.05f))
    )

    drawRoundRect(
        color = lowerTone,
        topLeft = Offset(x + r * 0.6f, y + r * (0.05f + flat * 0.12f)),
        size = Size(width = r * 4.7f, height = r * (1.45f - flat * 0.34f)),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 1.15f, r * 1.15f)
    )

    drawCircle(
        color = highlightColor,
        radius = r * (0.40f - flat * 0.08f),
        center = Offset(x + r * 1.55f, y - r * (0.20f - flat * 0.10f))
    )

    drawCircle(
        color = Color.White.copy(alpha = (color.alpha * (0.13f - flat * 0.05f)).coerceIn(0f, 1f)),
        radius = r * (0.32f - flat * 0.12f),
        center = Offset(x + r * 2.55f, y - r * (0.30f - flat * 0.14f))
    )

    if (puff > 0.45f) {
        drawCircle(
            color = Color.White.copy(alpha = (color.alpha * 0.10f * puff).coerceIn(0f, 1f)),
            radius = r * (0.40f + puff * 0.16f),
            center = Offset(x + r * 3.65f, y - r * (0.20f + puff * 0.10f))
        )
    }
}

