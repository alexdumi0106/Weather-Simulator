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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton




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
        humidity > 95 && pressure >= 1010 && wind < 10 ->
            R.drawable.icon_weather_11 to "Ceață"

        temperature <= 0 && humidity >= 70 && cloudCoverage >= 60 ->
            R.drawable.icon_weather_22 to "Ninsoare"

        humidity >= 70 && wind >= 40 && pressure < 1000 && temperature > 0 && cloudCoverage in 60f..80f ->
            R.drawable.icon_weather_16 to "Furtună cu soare"

        humidity >= 70 && wind >= 40 && pressure < 1000 && cloudCoverage > 80->
            R.drawable.icon_weather_15 to "Furtună"

        humidity >= 85 && pressure <= 1010 && cloudCoverage >= 80 ->
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

    var showAiHelpPopup by remember { mutableStateOf(true) }
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

        val isStorm = isStormWeatherDescription(descriptionNow)
        val isRain = isRainWeatherDescription(descriptionNow)
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
    // Ploaie continuă când sunt condiții de ploaie SAU furtună
    LaunchedEffect(descriptionNow, soundsEnabled) {
        if (!soundsEnabled) {
            audio.stopRain()
            return@LaunchedEffect
        }

        val isRain = isRainWeatherDescription(descriptionNow)
        val isStorm = isStormWeatherDescription(descriptionNow)

        if (isRain || isStorm) {
            audio.startRainLoop(resId = R.raw.rain, volume = 0.55f)
        } else {
            audio.stopRain()
        }
    }

    // Tunet, cand sunt conditii de furtuna
    //R = ID-ul intern Android pentru fișierul thunder.mp3 din res/raw
    LaunchedEffect(descriptionNow, soundsEnabled) {
        val isStorm = isStormWeatherDescription(descriptionNow)

        if (!soundsEnabled) {
            audio.stopThunder()
            return@LaunchedEffect
        }

        if (isStorm) {
            audio.startThunderLoop(resId = R.raw.thunder, volume = 0.75f)
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
                        text = "Simulator meteo",
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
                isStormy = isStormWeatherDescription(descriptionNow),
                pressureTrend = PressureTrend.STABLE,
                windSpeed = wind,
                humidity = humidity,
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .background(Color.White.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(10.dp)
                                        .height(10.dp)
                                        .background(Color(0xFFFFD180), RoundedCornerShape(99.dp))
                                )
                                Text(
                                    text = "Alege valorile atmosferice",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = "Ajustează valorile pentru a simula diferite condiții meteo.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.84f)
                            )
                        }

                        val sliderColors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD180),
                            activeTrackColor = Color(0xFFFFD180),
                            inactiveTrackColor = Color.White.copy(alpha = 0.28f)
                        )

                        WeatherSliderSection(
                            label = "Temperatura",
                            value = "${temperature.toInt()}°C"
                        ) {
                            Slider(
                                value = temperature,
                                onValueChange = {
                                    temperature = it.roundToInt().toFloat()
                                },
                                valueRange = -20f..50f,
                                steps = 69,
                                colors = sliderColors
                            )
                        }

                        WeatherSliderSection(
                            label = "Umiditate",
                            value = "${humidity.toInt()}%"
                        ) {
                            Slider(
                                value = humidity,
                                onValueChange = { v ->
                                    humidity = (v / 10f).roundToInt() * 10f
                                },
                                valueRange = 0f..100f,
                                steps = 9,
                                colors = sliderColors
                            )
                        }

                        val isBarometerAvailable = pressureSensorState.isAvailable
                        val trend = pressureSensorState.trendLabel
                        val trendHpaPerHour = pressureSensorState.trendHpaPerHour

                        WeatherSliderSection(
                            label = if (useBarometer && sensorPressure != null) "Presiune (LIVE)" else "Presiune",
                            value = if (useBarometer && sensorPressure != null) "${sensorPressure.toInt()} hPa" else "${pressure.toInt()} hPa"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (useBarometer && sensorPressure != null)
                                        "Senzor activ"
                                    else
                                        "Mod manual",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.84f)
                                )

                                if (isBarometerAvailable) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "LIVE",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.92f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Switch(
                                            checked = useBarometer,
                                            onCheckedChange = { useBarometer = it }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Slider(
                                value = pressure,
                                onValueChange = { p ->
                                    pressure = p.roundToInt().toFloat()
                                },
                                valueRange = 950f..1050f,
                                steps = 99,
                                enabled = !(useBarometer && sensorPressure != null),
                                colors = sliderColors
                            )

                            if (isBarometerAvailable && trend != PressureTrend.UNKNOWN && trendHpaPerHour != null) {
                                Text(
                                    text = "Trend: ${"%.1f".format(trendHpaPerHour)} hPa/oră • $trend",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.84f)
                                )
                            } else if (!isBarometerAvailable) {
                                Text(
                                    text = "Barometru indisponibil. Folosește modul manual.",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.84f)
                                )
                            }
                        }

                        WeatherSliderSection(
                            label = "Viteza vântului",
                            value = "${wind.toInt()} km/h"
                        ) {
                            Slider(
                                value = wind,
                                onValueChange = { w ->
                                    wind = (w / 10f).roundToInt() * 10f
                                },
                                valueRange = 0f..120f,
                                steps = 11,
                                colors = sliderColors
                            )
                        }

                        WeatherSliderSection(
                            label = "Acoperire nori",
                            value = "${cloudCoverage.toInt()}%"
                        ) {
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
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .background(Color.White.copy(alpha = 0.18f))
                ) {
                    Button(
                        onClick = { navController.navigate(Routes.AI_SIMULATION) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = "Simulare bazată pe AI",
                            fontSize = 17.sp
                        )
                    }
                }

                WeatherDisplayCard(
                    temperature = temperature,
                    humidity = humidity,
                    pressure = pressure,
                    wind = wind,
                    cloudCoverage = cloudCoverage
                )
            }

            if (showAiHelpPopup) {
                AlertDialog(
                    onDismissRequest = {
                        showAiHelpPopup = false
                    },
                    containerColor = Color(0xFF12345A),
                    titleContentColor = Color.White,
                    textContentColor = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(28.dp),
                    title = {
                        Text(
                            text = "Ai nevoie de ajutor?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        )
                    },
                    text = {
                        Text(
                            text = "Folosește modulul AI al aplicației pentru a afla cum să modifici corect temperatura, umiditatea, presiunea, vântul și norii ca să simulezi ploaie, furtună, ninsoare sau ceață.",
                            fontSize = 15.sp,
                            lineHeight = 21.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showAiHelpPopup = false
                                navController.navigate(Routes.AI_SIMULATION)
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5FA8FF),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Deschide AI")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showAiHelpPopup = false
                            }
                        ) {
                            Text(
                                text = "Mai târziu",
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
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
            .padding(bottom = 16.dp) // Adăugăm puțin spațiu jos față de marginea ecranului
            .clip(RoundedCornerShape(28.dp))
            .border(
                width = 1.dp, 
                color = Color.White.copy(alpha = 0.35f), // Contur mai opac/pronunțat
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            // Am crescut alpha la 0.25f pentru a face culoarea mai închisă/densă
            containerColor = Color.White.copy(alpha = 0.18f) 
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = description,
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = description,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
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
fun WeatherSliderSection(
    label: String,
    value: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.95f)
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        content()
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
    val cloudDriftDuration = (36000f - windFactor * 18000f).roundToInt().coerceIn(12000, 36000)

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
        "Predominant însorit" -> 0.10f
        "Parțial însorit" -> 0.12f
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
        "Predominant însorit" -> 1.0f
        "Parțial însorit" -> 1.0f
        "Nori și soare" -> 1.0f
        "Predominant noros" -> 1.0f
        "Furtună cu soare" -> 0.94f
        "Noros" -> 0.0f
        "Ploaie", "Ploaie ușoară", "Ploaie intensa" -> 0.10f
        "Furtună" -> 0.04f
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

    val cloudsAlpha = when (weatherDescription) {
        "Însorit" -> 0f
        else -> (cloudAlphaBase + conditionCloudBoost + stormBoost).coerceIn(0f, 1f)
    }
    val keepStrongSun = weatherDescription in setOf(
        "Însorit",
        "Predominant însorit",
        "Parțial însorit",
        "Nori și soare",
        "Predominant noros",
        "Furtună cu soare"
    )
    val sunAlpha = if (keepStrongSun) {
        sunConditionFactor.coerceIn(0f, 1f)
    } else {
        ((1f - cloudAlphaBase * 0.9f) * sunConditionFactor).coerceIn(0f, 1f)
    }
    val hazeAlpha = (
        ((humidity - 60f) / 40f).coerceIn(0f, 1f) * (1f - cloudsAlpha * 0.35f) + hazeBoost
    ).coerceIn(0f, 1f)
    
    val skyColors = when (weatherDescription) {
        "Însorit" -> listOf(
            Color(0xFF1C5EBA),
            Color(0xFF3E89DE),
            Color(0xFFB8DAF8)
        )

        "Predominant însorit" -> listOf(
            Color(0xFF1C5EBA),
            Color(0xFF3E89DE),
            Color(0xFFB8DAF8)
        )

        "Parțial însorit" -> listOf(
            Color(0xFF1C5EBA),
            Color(0xFF3E89DE),
            Color(0xFFB8DAF8)
        )

        "Nori și soare" -> listOf(
            Color(0xFF478FD8),
            Color(0xFF8AC0ED),
            Color(0xFFE2F1FF)
        )

        "Predominant noros" -> listOf(
            Color(0xFF4E88C2),
            Color(0xFF8DB2D2),
            Color(0xFFDDEAF4)
        )

        "Noros" -> listOf(
            Color(0xFF5D6773),
            Color(0xFF8F9AA7),
            Color(0xFFC7D0D8)
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
            val isStormWithSun = weatherDescription == "Furtună cu soare"

            drawSunWithRays(
                center = center,
                radius = r,
                sunAlpha = if (isStormWithSun) (sunAlpha * 1.08f).coerceIn(0f, 1f) else sunAlpha,
                rayRotation = sunRayRotation
            )
        }

        // Cloud layout is tuned per weather type to match visual references.
        if (cloudsAlpha > 0.02f) {
            fun wrap01(value: Float): Float {
                val wrapped = value % 1f
                return if (wrapped < 0f) wrapped + 1f else wrapped
            }

            fun xPos(speed: Float, phaseOffset: Float, xOffsetRatio: Float, driftFactor: Float): Float {
                val speedBoost = (((speed * 0.72f) + (windFactor * 0.48f) + 0.28f) * driftFactor).coerceIn(0.55f, 1.55f)
                val localMove = wrap01(cloudMove + phaseOffset)
                val startX = -0.62f * w
                val travelWidth = 2.24f * w
                val xSpread = 0.35f
                return startX + (localMove * travelWidth * speedBoost) + (w * xOffsetRatio * xSpread)
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
                    baseX = xPos(
                        speed = band.speed,
                        phaseOffset = band.phaseOffset,
                        xOffsetRatio = band.xOffsetRatio,
                        driftFactor = band.driftFactor
                    ),
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
            if (isFogWeatherDescription(weatherDescription)) {
                drawRect(color = Color.White.copy(alpha = 0.10f))
            }

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

            if (isFogWeatherDescription(weatherDescription)) {
                drawCircle(
                    color = Color.White.copy(alpha = fogAlpha * 0.10f),
                    radius = w * 0.48f,
                    center = Offset(w * 0.52f, h * 0.70f)
                )
            }
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
    val invertBob: Boolean,
    val xOffsetRatio: Float = 0f,
    val driftFactor: Float = 1f,
    val phaseOffset: Float = 0f
)

private data class CloudProfile(
    val baseColor: Color,
    val bands: List<CloudBandSpec>
)

private fun denseOvercastBands(count: Int): List<CloudBandSpec> {
    return List(count) { i ->
        val t = if (count <= 1) 0f else i / (count - 1f)
        val speed = 0.44f + t * 0.98f
        val yRatio = 0.12f + t * 0.54f
        val scale = (1.56f + (kotlin.math.sin(i * 0.9f) * 0.24f).toFloat() + (1f - t) * 0.10f).coerceIn(1.40f, 1.98f)
        val alphaMul = (1.00f - t * 0.28f).coerceIn(0.70f, 1.00f)
        val flatness = (0.58f + t * 0.18f).coerceIn(0.58f, 0.78f)
        val darkness = 1.14f + t * 0.20f
        val puffiness = (0.42f - t * 0.18f).coerceIn(0.22f, 0.42f)
        val bobFactor = (0.010f - t * 0.003f).coerceIn(0.006f, 0.010f)
        val invertBob = i % 2 == 1
        val xOffsetRatio = -0.28f + t * 0.58f
        val driftFactor = 0.86f + t * 0.36f
        val phaseOffset = (0.03f + i * 0.11f) % 1f

        CloudBandSpec(
            speed = speed,
            yRatio = yRatio,
            scale = scale,
            alphaMul = alphaMul,
            flatness = flatness,
            darkness = darkness,
            puffiness = puffiness,
            bobFactor = bobFactor,
            invertBob = invertBob,
            xOffsetRatio = xOffsetRatio,
            driftFactor = driftFactor,
            phaseOffset = phaseOffset
        )
    }
}

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
            baseColor = Color(0xFFF6FBFF).copy(alpha = 0f),
            bands = emptyList()
        )

        "Predominant însorit" -> CloudProfile(
            baseColor = Color(0xFFDDE7F0).copy(alpha = (alpha * 1.15f).coerceIn(0f, 1f)),
            bands = denseOvercastBands(count = 5)
        )

        "Parțial însorit" -> CloudProfile(
            baseColor = Color(0xFFDDE7F0).copy(alpha = (alpha * 1.15f).coerceIn(0f, 1f)),
            bands = denseOvercastBands(count = 10)
        )

        "Nori și soare" -> CloudProfile(
            baseColor = Color(0xFFDDE7F0).copy(alpha = (alpha * 1.15f).coerceIn(0f, 1f)),
            bands = denseOvercastBands(count = 20)
        )

        "Predominant noros" -> CloudProfile(
            baseColor = Color(0xFFDDE7F0).copy(alpha = (alpha * 1.15f).coerceIn(0f, 1f)),
            bands = denseOvercastBands(count = 40)
        )

        "Noros" -> CloudProfile(
            baseColor = Color(0xFFCDD6DE).copy(alpha = alpha * 0.94f),
            bands = denseOvercastBands(count = 40).map { band ->
                band.copy(darkness = band.darkness + pressureDarkBoost)
            }
        )

        "Ploaie", "Ploaie ușoară", "Ploaie intensa" -> CloudProfile(
            baseColor = Color(0xFFC5D0D9).copy(alpha = alpha * 0.92f),
            bands = denseOvercastBands(count = 40).map { band ->
                band.copy(
                    yRatio = (band.yRatio - 0.02f).coerceIn(0.10f, 0.72f),
                    scale = (band.scale + 0.26f).coerceIn(1.62f, 2.42f),
                    flatness = (band.flatness + 0.14f).coerceIn(0.70f, 0.94f),
                    darkness = band.darkness + 0.24f + pressureDarkBoost,
                    puffiness = (band.puffiness - 0.10f).coerceIn(0.16f, 0.34f),
                    bobFactor = (band.bobFactor * 0.80f).coerceIn(0.004f, 0.009f)
                )
            }
        )

        "Furtună" -> CloudProfile(
            baseColor = Color(0xFFB5C1CC).copy(alpha = alpha * 0.94f),
            bands = denseOvercastBands(count = 40).map { band ->
                band.copy(
                    yRatio = (band.yRatio - 0.03f).coerceIn(0.08f, 0.70f),
                    scale = (band.scale + 0.34f).coerceIn(1.78f, 2.68f),
                    flatness = (band.flatness + 0.20f).coerceIn(0.78f, 0.98f),
                    darkness = band.darkness + 0.42f + pressureDarkBoost,
                    puffiness = (band.puffiness - 0.14f).coerceIn(0.12f, 0.30f),
                    driftFactor = (band.driftFactor + 0.06f).coerceIn(0.90f, 1.34f),
                    bobFactor = (band.bobFactor * 0.66f).coerceIn(0.003f, 0.008f)
                )
            }
        )

        "Furtună cu soare" -> CloudProfile(
            baseColor = Color(0xFFC5CFD8).copy(alpha = alpha * if (isStormy) 0.88f else 0.78f),
            bands = denseOvercastBands(count = 30).map { band ->
                band.copy(
                    yRatio = (band.yRatio - 0.01f).coerceIn(0.10f, 0.72f),
                    scale = (band.scale + 0.18f).coerceIn(1.56f, 2.24f),
                    flatness = (band.flatness + 0.12f).coerceIn(0.68f, 0.90f),
                    darkness = band.darkness + 0.24f + if (isStormy) 0.16f else 0.04f,
                    puffiness = (band.puffiness - 0.08f).coerceIn(0.18f, 0.34f),
                    driftFactor = (band.driftFactor + 0.04f).coerceIn(0.88f, 1.30f)
                )
            }
        )

        "Ninsoare", "Ninsoare usoara", "Ninsoare intensa" -> CloudProfile(
            baseColor = Color(0xFFE8EEF4).copy(alpha = alpha * 0.86f),
            bands = denseOvercastBands(count = 40).map { band ->
                band.copy(
                    yRatio = (band.yRatio + 0.03f).coerceIn(0.14f, 0.76f),
                    scale = (band.scale + 0.04f).coerceIn(1.46f, 2.12f),
                    flatness = (band.flatness + 0.06f).coerceIn(0.62f, 0.84f),
                    darkness = band.darkness - 0.10f,
                    puffiness = (band.puffiness + 0.05f).coerceIn(0.24f, 0.44f),
                    driftFactor = (band.driftFactor - 0.06f).coerceIn(0.76f, 1.14f),
                    bobFactor = (band.bobFactor * 1.08f).coerceIn(0.006f, 0.011f)
                )
            }
        )

        "Ceață" -> CloudProfile(
            baseColor = Color(0xFFEFF3F7).copy(alpha = alpha * 0.72f),
            bands = denseOvercastBands(count = 40).map { band ->
                band.copy(
                    yRatio = (band.yRatio + 0.08f).coerceIn(0.20f, 0.82f),
                    scale = (band.scale + 0.20f).coerceIn(1.66f, 2.30f),
                    flatness = (band.flatness + 0.24f).coerceIn(0.82f, 1.00f),
                    darkness = band.darkness - 0.28f,
                    puffiness = (band.puffiness - 0.14f).coerceIn(0.10f, 0.24f),
                    driftFactor = (band.driftFactor - 0.18f).coerceIn(0.62f, 1.00f),
                    bobFactor = (band.bobFactor * 0.58f).coerceIn(0.003f, 0.007f)
                )
            }
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
    val r = size.minDimension * 0.072f * scale
    val x = baseX
    val y = baseY + (r * 0.08f * wobble)
    val flat = flatness.coerceIn(0f, 1f)
    val puff = puffiness.coerceIn(0f, 1f)

    val width = r * (4.6f + puff * 1.3f)
    val height = r * (1.55f - flat * 0.28f + puff * 0.22f)
    val top = y - height * (0.62f + puff * 0.08f)
    val left = x

    val shadowColor = Color(0xFF4A5C6D).copy(alpha = (color.alpha * 0.16f * darkness).coerceIn(0f, 1f))
    val brightTop = Color.White.copy(alpha = (color.alpha * (0.22f - flat * 0.05f)).coerceIn(0f, 1f))
    val midTone = color.copy(alpha = (color.alpha * (0.96f - flat * 0.05f)).coerceIn(0f, 1f))
    val lowerTone = Color(
        red = (color.red * (0.90f - flat * 0.08f)).coerceIn(0f, 1f),
        green = (color.green * (0.92f - flat * 0.10f)).coerceIn(0f, 1f),
        blue = (color.blue * (0.96f - flat * 0.06f)).coerceIn(0f, 1f),
        alpha = (color.alpha * 0.92f).coerceIn(0f, 1f)
    )

    val cloudShape = Path().apply {
        val baseY = top + height * 0.90f
        moveTo(left + width * 0.03f, baseY)
        cubicTo(
            left - width * 0.02f,
            top + height * 0.68f,
            left + width * 0.12f,
            top + height * 0.36f,
            left + width * 0.28f,
            top + height * 0.32f
        )
        cubicTo(
            left + width * 0.38f,
            top + height * 0.02f,
            left + width * 0.54f,
            top - height * 0.02f,
            left + width * 0.66f,
            top + height * 0.24f
        )
        cubicTo(
            left + width * 0.78f,
            top + height * 0.10f,
            left + width * 0.94f,
            top + height * 0.28f,
            left + width * 0.97f,
            top + height * 0.52f
        )
        cubicTo(
            left + width * 1.02f,
            top + height * 0.70f,
            left + width * 0.98f,
            top + height * 0.90f,
            left + width * 0.88f,
            top + height * 0.92f
        )
        lineTo(left + width * 0.10f, top + height * 0.92f)
        cubicTo(
            left + width * 0.03f,
            top + height * 0.92f,
            left,
            top + height * 0.92f,
            left + width * 0.03f,
            baseY
        )
        close()
    }

    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(left + width * 0.06f, top + height * 0.80f),
        size = Size(width = width * 0.90f, height = height * 0.34f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(height * 0.28f, height * 0.28f)
    )

    drawPath(
        path = cloudShape,
        color = Color.White.copy(alpha = (color.alpha * 0.16f).coerceIn(0f, 1f))
    )

    drawPath(
        path = cloudShape,
        brush = Brush.verticalGradient(
            colors = listOf(brightTop, midTone, lowerTone),
            startY = top,
            endY = top + height
        )
    )

    drawRoundRect(
        color = Color.White.copy(alpha = (color.alpha * 0.08f).coerceIn(0f, 1f)),
        topLeft = Offset(left + width * 0.18f, top + height * 0.35f),
        size = Size(width = width * 0.46f, height = height * 0.20f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(height * 0.14f, height * 0.14f)
    )
}

