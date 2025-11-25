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


fun getBackgroundColor(cloudCoverage: Float): Color {
    return when {
        cloudCoverage <= 40f ->
            Color(0xFF1565C0)

        cloudCoverage in 41f..80f ->
            Color(0xFF90CAF9)

        cloudCoverage > 80f ->
            Color(0xFFCFD8DC)

        else -> Color(0xFF64B5F6)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(navController: NavController) {
    var temperature by remember { mutableStateOf(20f) }
    var humidity by remember { mutableStateOf(50f) }
    var pressure by remember { mutableStateOf(1013f) }
    var wind by remember { mutableStateOf(10f) }
    var cloudCoverage by remember { mutableStateOf(0f) }

    val backgroundColor by animateColorAsState(
        targetValue = getBackgroundColor(cloudCoverage),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = ""
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Weather Simulator AI") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Alege valorile atmosferice:", fontSize = 20.sp)

            Text(text = "Temperatura: ${temperature.toInt()}°C", fontSize = 18.sp)
            Slider(
                value = temperature,
                onValueChange = { temperature = it },
                valueRange = -20f..50f,
                steps = 7
            )

            Text(text = "Umiditate: ${humidity.toInt()}%", fontSize = 18.sp)
            Slider(
                value = humidity,
                onValueChange = { humidity = it },
                valueRange = 0f..100f,
                steps = 9
            )

            Text(text = "Presiune : ${pressure.toInt()} hPa", fontSize = 18.sp)
            Slider(
                value = pressure,
                onValueChange = { pressure = it },
                valueRange = 950f..1050f,
                steps = 9
            )

            Text(text = "Viteza vântului: ${wind.toInt()} km/h", fontSize = 18.sp)
            Slider(
                value = wind,
                onValueChange = { wind = it },
                valueRange = 0f..120f,
                steps = 11
            )


            Text(text = "Acoperire nori: ${cloudCoverage.toInt()}%", fontSize = 18.sp)
            Slider(
                value = cloudCoverage,
                onValueChange = { cloudCoverage = it },
                valueRange = 0f..100f,
                steps = 4
            )


            Spacer(modifier = Modifier.height(32.dp))

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

@Composable
fun WeatherDisplayCard(
    temperature: Float,
    humidity: Float,
    pressure: Float,
    wind: Float,
    cloudCoverage: Float
) {
    val (icon, description) = remember(temperature, humidity, pressure, wind, cloudCoverage) {
        when {
            humidity > 95 && pressure < 1010 && temperature in 0f..15f ->
                "🌫️" to "Ceață"

            temperature < 0 && humidity > 70 ->
                "🌨️" to "Ninsoare"

            humidity > 90 && wind >= 50 && pressure < 1000 ->
                "⛈️" to "Furtună"

            humidity > 70 && wind >= 40 && pressure in 995f..1005f && temperature > 20 && cloudCoverage in 20f .. 60f ->
                "🌦️" to "Furtună cu soare"

            humidity > 85 && pressure < 1005 && cloudCoverage >= 80 ->
                "🌧️" to "Ploaie"

            cloudCoverage == 0f -> "☀️" to "Insorit"
            cloudCoverage == 20f -> "🌤️" to "Predominant insorit"
            cloudCoverage == 40f -> "⛅" to "Parțial insorit"
            cloudCoverage == 60f -> "🌥️" to "Nori și soare"
            cloudCoverage == 80f -> "🌥️" to "Predominant noros"
            cloudCoverage == 100f -> "☁️" to "Noros"

            else -> "🌦️" to "Condiții variabile"
        }
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
            Text(text = icon, fontSize = 56.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}