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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.weathersimulator.ui.components.DailyForecastList
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun WeatherHomeSection(
    isLoading: Boolean,
    error: String?,
    data: OpenMeteoResponse?,
    hourlyForecast: List<HourlyForecastItemUi>,
    dailyForecast: List<DailyForecastItemUi>
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Acum",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${current.temperature?.toInt() ?: "--"}°C",
                        style = MaterialTheme.typography.displaySmall
                    )

                    Text(
                        text = currentWeatherLabel(
                            code = current.weatherCode ?: 0,
                            isDay = current.isDay == 1
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = weatherCodeToEmoji(
                        code = current.weatherCode ?: 0,
                        isDay = current.isDay == 1
                    ),
                    style = MaterialTheme.typography.displayMedium
                )
            }

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

    Spacer(Modifier.height(16.dp))

    DailyForecastList(
        items = dailyForecast,
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
        containerColor = Color.White,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Weather Simulator AI") },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black
                    )
                )
                val label = s.placeName ?: "Determin locația..."
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.Black
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            WeatherHomeSection(
                isLoading = weatherState.isLoading,
                error = weatherState.error,
                data = weatherState.data,
                hourlyForecast = weatherState.hourlyForecast,
                dailyForecast = weatherState.dailyForecast
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

private fun weatherCodeToEmoji(code: Int, isDay: Boolean): String {
    return when (code) {
        0 -> if (isDay) "☀" else "🌙"
        1, 2 -> if (isDay) "🌤" else "🌙☁"
        3 -> if (isDay) "☁" else "☁🌙"
        45, 48 -> "🌫"
        51, 53, 55, 56, 57 -> if (isDay) "🌦" else "🌧🌙"
        61, 63, 65, 66, 67 -> "🌧"
        71, 73, 75, 77 -> "❄"
        80, 81, 82 -> "🌧"
        85, 86 -> "🌨"
        95, 96, 99 -> "⛈"
        else -> if (isDay) "☁" else "☁🌙"
    }
}

private fun currentWeatherLabel(code: Int, isDay: Boolean): String {
    return when (code) {
        0 -> if (isDay) "Cer senin" else "Noapte senină"
        1 -> if (isDay) "Mai mult senin" else "Noapte mai mult senină"
        2 -> "Parțial noros"
        3 -> "Înnorat"
        45, 48 -> "Ceață"
        51, 53, 55, 56, 57 -> "Burniță"
        61, 63, 65, 66, 67 -> "Ploaie"
        71, 73, 75, 77 -> "Ninsoare"
        80, 81, 82 -> "Averse"
        85, 86 -> "Averse de ninsoare"
        95, 96, 99 -> "Furtună"
        else -> "Vreme necunoscută"
    }
}
