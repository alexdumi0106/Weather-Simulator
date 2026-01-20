package com.example.weathersimulator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersimulator.ui.navigation.AppNavigation
import com.example.weathersimulator.ui.screens.WeatherSimulatorTheme
import com.example.weathersimulator.ui.sensors.location.LocationViewModel
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.platform.LocalContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            WeatherSimulatorTheme {
                val locationVm: LocationViewModel = hiltViewModel()
                LocationPermissionGate(locationVm)
                AppNavigation()
            }
        }
    }
}

@Composable
private fun LocationPermissionGate(
    locationVm: LocationViewModel
) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        locationVm.setPermission(granted)
        if (granted) locationVm.start() else locationVm.stop()
    }

    LaunchedEffect(Unit) {
        locationVm.setPermission(hasPermission)
        if (hasPermission) {
            locationVm.start()
        } else {
            launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    DisposableEffect(Unit) {
        onDispose { locationVm.stop() }
    }
}
