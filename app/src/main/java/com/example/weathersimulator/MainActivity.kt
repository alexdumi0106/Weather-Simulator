package com.example.weathersimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WeatherSimulatorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSimulatorApp() {
    var temperature by remember { mutableStateOf(20f) }
    var humidity by remember { mutableStateOf(50f) }
    var pressure by remember { mutableStateOf(1013f) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Weather Simulator AI") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
        }
    }
}
