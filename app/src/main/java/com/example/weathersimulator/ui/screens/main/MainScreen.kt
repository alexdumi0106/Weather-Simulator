package com.example.weathersimulator.ui.screens.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse
import com.example.weathersimulator.ui.components.PrimaryActionButton
import com.example.weathersimulator.ui.components.SecondaryActionButton
import com.example.weathersimulator.ui.navigation.Routes
import com.example.weathersimulator.ui.sensors.location.LocationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.weathersimulator.ui.components.HourlyForecastRow
import com.example.weathersimulator.ui.screens.main.HourlyForecastItemUi

@Composable
fun WeatherHomeSection(
    isLoading: Boolean,
    error: String?,
    data: OpenMeteoResponse?,
    hourlyForecast: List<HourlyForecastItemUi>
) {
    if (isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
    }

    if (error != null) {
        Text(text = "Eroare meteo: $error")
        Spacer(Modifier.height(8.dp))
        return
    }

    val current = data?.current ?: return

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Acum", style = MaterialTheme.typography.titleMedium)
            Text(text = "${current.temperature?.toInt() ?: "--"}°C", style = MaterialTheme.typography.displaySmall)
            Text(text = "Feels like: ${current.apparentTemperature?.toInt() ?: "--"}°C")
            Text(text = "Wind: ${current.windSpeed?.toInt() ?: "--"} km/h • Humidity: ${current.humidity ?: "--"}%")
            Text(text = "Pressure: ${current.pressure?.toInt() ?: "--"} hPa")
        }
    }

    Spacer(Modifier.height(12.dp))

    HourlyForecastRow(
        items = hourlyForecast,
        modifier = Modifier.fillMaxWidth()
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {

    val activity = LocalContext.current as ComponentActivity
    val locationVm: LocationViewModel = hiltViewModel(activity)
    val s = locationVm.state.collectAsState().value

    LaunchedEffect(Unit) {
        locationVm.start()
    }

    val weatherVm: WeatherViewModel = hiltViewModel(activity)
    val weatherState = weatherVm.state.collectAsState().value

    LaunchedEffect(s.lat, s.lon) {
        val lat = s.lat
        val lon = s.lon
        if (lat != null && lon != null) {
            weatherVm.load(lat, lon)
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Weather Simulator AI") }
                )
                val label = s.placeName ?: "Determin locația..."
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            WeatherHomeSection(
                isLoading = weatherState.isLoading,
                error = weatherState.error,
                data = weatherState.data,
                hourlyForecast = weatherState.hourlyForecast
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryActionButton(
                text = "Weather Simulation",
                onClick = { navController.navigate(Routes.SIMULATOR) },
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryActionButton(
                text = "Settings",
                onClick = { navController.navigate(Routes.SETTINGS) },
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryActionButton(
                text = "Profile",
                onClick = { navController.navigate(Routes.PROFILE) },
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryActionButton(
                text = "Logout",
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Logout") },
                    text = { Text("Are you sure you want to log out?") },
                    confirmButton = {
                        TextButton(onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.MAIN) { inclusive = true }
                            }
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
