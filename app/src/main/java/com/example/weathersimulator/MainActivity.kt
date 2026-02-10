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
import android.os.Build
import com.example.weathersimulator.workers.WeatherWorkScheduler
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            WeatherSimulatorTheme {
                val locationVm: LocationViewModel = hiltViewModel()
                LocationPermissionGate(locationVm)
                NotificationPermissionGate()
                AppNavigation()
            }
        }
    }
}

@Composable
private fun NotificationPermissionGate() {
    val context = LocalContext.current

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        LaunchedEffect(Unit) { WeatherWorkScheduler.schedule(context) }
        return
    }

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok ->
        granted = ok
        if (ok) WeatherWorkScheduler.schedule(context)
    }

    LaunchedEffect(Unit) {
        if (granted) WeatherWorkScheduler.schedule(context)
        else launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
