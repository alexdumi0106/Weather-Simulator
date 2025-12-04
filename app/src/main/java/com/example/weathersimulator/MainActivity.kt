package com.example.weathersimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.weathersimulator.ui.navigation.AppNavigation
import com.example.weathersimulator.ui.screens.WeatherSimulatorTheme
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            WeatherSimulatorTheme {
                AppNavigation()
            }
        }
    }
}
