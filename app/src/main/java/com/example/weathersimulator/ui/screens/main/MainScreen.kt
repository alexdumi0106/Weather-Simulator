package com.example.weathersimulator.ui.screens.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weathersimulator.R
import com.example.weathersimulator.data.remote.weather.OpenMeteoResponse
import com.example.weathersimulator.ui.components.DailyForecastList
import com.example.weathersimulator.ui.components.HourlyForecastRow
import com.example.weathersimulator.ui.components.WeatherDetailsGrid
import com.example.weathersimulator.ui.navigation.Routes
import com.example.weathersimulator.ui.sensors.location.LocationViewModel
import com.example.weathersimulator.ui.weather.WeatherIconRules
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import kotlin.math.roundToInt
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun WeatherHomeSection(
    locationName: String,
    isLoading: Boolean,
    error: String?,
    data: OpenMeteoResponse?,
    hourlyForecast: List<HourlyForecastItemUi>,
    dailyForecast: List<DailyForecastItemUi>,
    latitude: Double = 0.0,
    longitude: Double = 0.0
) {
    val cityName = locationName.substringBefore(",").ifBlank { "Locatia ta" }

    if (isLoading) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small),
            color = Color(0xFFBEE7FF),
            trackColor = Color.White.copy(alpha = 0.2f)
        )
        Spacer(Modifier.height(8.dp))
    }

    if (error != null) {
        Text(
            text = "Eroare meteo: $error",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(8.dp))
        return
    }

    val current = data?.current ?: return
    val today = dailyForecast.firstOrNull()
    val weatherVisual = WeatherIconRules.resolve(
        weatherCode = current.weatherCode ?: 0,
        isDay = current.isDay == 1,
        cloudCover = current.cloudCover
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = cityName,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 30.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${current.temperature?.roundToInt() ?: "--"}°",
            color = Color.White,
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Light,
                fontSize = 90.sp,
                letterSpacing = (-2).sp
            )
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(
                    id = weatherVisual.iconRes
                ),
                contentDescription = "Icon meteo",
                modifier = Modifier.size(100.dp)
            )

            Text(
                text = weatherVisual.label,
                color = Color.White.copy(alpha = 0.96f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Text(
            text = "Temperatura resimtita: ${current.apparentTemperature?.roundToInt() ?: "--"}°",
            color = Color.White.copy(alpha = 0.86f),
            style = MaterialTheme.typography.titleMedium
        )

        if (today != null) {
            Text(
                text = "Max: ${today.maxTemperature}  Min: ${today.minTemperature}",
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    HourlyForecastRow(
        items = hourlyForecast,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(16.dp))

    DailyForecastList(
        items = dailyForecast,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(16.dp))

    WeatherDetailsGrid(
        data = data,
        latitude = latitude,
        longitude = longitude,
        modifier = Modifier.fillMaxWidth()
    )
}


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

    LaunchedEffect(s.lat, s.lon, weatherState.isHistoryMode) {
        val lat = s.lat
        val lon = s.lon
        if (!weatherState.isHistoryMode && lat != null && lon != null) {
            weatherVm.load(lat, lon)
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    val baseBackground = weatherBackground(
        code = weatherState.data?.current?.weatherCode ?: 0,
        isDay = weatherState.data?.current?.isDay == 1
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(baseBackground))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x4D0C1A2E),
                                Color(0xB30A1528)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherHomeSection(
                    locationName = s.placeName ?: "Locatia ta",
                    isLoading = weatherState.isLoading,
                    error = weatherState.error,
                    data = weatherState.data,
                    hourlyForecast = weatherState.hourlyForecast,
                    dailyForecast = weatherState.dailyForecast,
                    latitude = s.lat ?: 0.0,
                    longitude = s.lon ?: 0.0
                )

                Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.White.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                weatherVm.setHistoryMode(true)
                                weatherVm.loadHistorical()
                                navController.navigate(Routes.WEATHER_HISTORY_ROUTE)
                            }
                            .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "HISTORY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )

                        Text(
                            text = "Weather Data",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            fontSize = 32.sp
                        )

                        Text(
                            text = "Browse historical data",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(110.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    FloatingActionButton(
                        onClick = { showSettingsMenu = true },
                        modifier = Modifier.size(58.dp),
                        shape = CircleShape,
                        containerColor = Color(0xFF2C4E73),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Setari"
                        )
                    }

                    DropdownMenu(
                        expanded = showSettingsMenu,
                        onDismissRequest = { showSettingsMenu = false },
                        modifier = Modifier.background(Color(0xFF244263))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profil", color = Color.White) },
                            onClick = {
                                showSettingsMenu = false
                                navController.navigate(Routes.PROFILE)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout", color = Color.White) },
                            onClick = {
                                showSettingsMenu = false
                                showDialog = true
                            }
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { navController.navigate(Routes.SIMULATOR) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(68.dp),
                    shape = CircleShape,
                    containerColor = Color(0xFF5A83B4),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_robot),
                        contentDescription = "Weather Simulation",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

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

private fun weatherBackground(code: Int, isDay: Boolean): List<Color> {
    if (!isDay) {
        return listOf(
            Color(0xFF0F1D35),
            Color(0xFF182A45),
            Color(0xFF243852)
        )
    }

    return when (code) {
        61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99 -> listOf(
            Color(0xFF425E7D),
            Color(0xFF5E7593),
            Color(0xFF7A8FAA)
        )

        45, 48 -> listOf(
            Color(0xFF6E8499),
            Color(0xFF8097AC),
            Color(0xFF9BB0C3)
        )

        else -> listOf(
            Color(0xFF3D6FA6),
            Color(0xFF5E88BB),
            Color(0xFF86A7CF)
        )
    }
}
