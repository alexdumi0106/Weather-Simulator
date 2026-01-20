package com.example.weathersimulator.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weathersimulator.ui.navigation.Routes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api


enum class TempUnit { C, F }
enum class PressureUnit { HPA, MMHG }
enum class WindUnit { KMH, MS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var tempUnit by remember { mutableStateOf(TempUnit.C) }
    var pressureUnit by remember { mutableStateOf(PressureUnit.HPA) }
    var windUnit by remember { mutableStateOf(WindUnit.KMH) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Units", style = MaterialTheme.typography.headlineSmall)

            UnitSection(
                title = "Temperature",
                options = listOf("Celsius (°C)" to TempUnit.C, "Fahrenheit (°F)" to TempUnit.F),
                selected = tempUnit,
                onSelect = { tempUnit = it }
            )

            UnitSection(
                title = "Pressure",
                options = listOf("hPa" to PressureUnit.HPA, "mmHg" to PressureUnit.MMHG),
                selected = pressureUnit,
                onSelect = { pressureUnit = it }
            )

            UnitSection(
                title = "Wind speed",
                options = listOf("km/h" to WindUnit.KMH, "m/s" to WindUnit.MS),
                selected = windUnit,
                onSelect = { windUnit = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SETTINGS) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun <T> UnitSection(
    title: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        options.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label)
                RadioButton(
                    selected = (value == selected),
                    onClick = { onSelect(value) }
                )
            }
        }
        Divider()
    }
}
