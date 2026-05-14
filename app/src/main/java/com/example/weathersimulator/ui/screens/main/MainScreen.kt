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
import androidx.compose.foundation.BorderStroke
import com.example.weathersimulator.ui.screens.auth.AuthAccentColor
import com.example.weathersimulator.ui.screens.auth.AuthFieldTextColor
import com.example.weathersimulator.ui.screens.auth.AuthTitleColor
import com.example.weathersimulator.ui.screens.auth.AuthLinkColor
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.CircularProgressIndicator

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

    

    if (error != null) {
        Text(
            text = "Eroare meteo: $error",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(8.dp))
        return
    }

    if (isLoading || data?.current == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.18f)
                )

                Text(
                    text = "Se încarcă prognoza meteo...",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        return
    }

    val current = data.current
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
            text = "Temperatura resimțită: ${current.apparentTemperature?.roundToInt() ?: "--"}°",
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

    val baseBackground = weatherBackground(
        code = weatherState.data?.current?.weatherCode ?: 0,
        isDay = weatherState.data?.current?.isDay == 1
    )

   Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            WeatherBottomNavBar(
                onHomeClick = { },
                onWeatherDataClick = {
                    weatherVm.setHistoryMode(true)
                    weatherVm.loadHistorical()
                    navController.navigate(Routes.WEATHER_HISTORY_ROUTE)
                },
                onSimulatorClick = {
                    navController.navigate(Routes.SIMULATOR)
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
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
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun WeatherBottomNavBar(
    onHomeClick: () -> Unit,
    onWeatherDataClick: () -> Unit,
    onSimulatorClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF173A5E).copy(alpha = 0.96f),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = onHomeClick,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Acasă") },
            colors = navItemColors()
        )

        NavigationBarItem(
            selected = false,
            onClick = onWeatherDataClick,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DateRange,
                    contentDescription = "Weather Data"
                )
            },
            label = { Text("Arhivă") },
            colors = navItemColors()
        )

        NavigationBarItem(
            selected = false,
            onClick = onSimulatorClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_sun),
                    contentDescription = "Simulator",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Simulator") },
            colors = navItemColors()
        )

        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Setări") },
            colors = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.White,
    selectedTextColor = Color.White,
    unselectedIconColor = Color.White.copy(alpha = 0.65f),
    unselectedTextColor = Color.White.copy(alpha = 0.65f),
    indicatorColor = Color(0xFF5A83B4).copy(alpha = 0.55f)
)

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
