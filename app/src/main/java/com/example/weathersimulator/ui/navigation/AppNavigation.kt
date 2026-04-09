package com.example.weathersimulator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weathersimulator.ui.screens.auth.LoginScreen
import com.example.weathersimulator.ui.screens.auth.RegisterScreen
import com.example.weathersimulator.ui.screens.auth.ResetPasswordScreen
import com.example.weathersimulator.ui.screens.auth.ProfileScreen
import com.example.weathersimulator.ui.screens.main.MainScreen
import com.example.weathersimulator.ui.screens.WeatherSimulatorTheme
import com.example.weathersimulator.ui.screens.settings.SettingsScreen
import com.example.weathersimulator.ui.screens.simulator.SimulatorScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.weathersimulator.ui.screens.simulator.AiSimulationScreen
import com.example.weathersimulator.ui.screens.main.WeatherHistoryScreen
import com.example.weathersimulator.ui.screens.main.WeatherHistoryDayScreen
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.weathersimulator.ui.screens.main.WeatherViewModel


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val startDestination =
        if (FirebaseAuth.getInstance().currentUser != null) Routes.MAIN else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                LoginScreen(navController)
            }
        }

        composable(Routes.REGISTER) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                RegisterScreen(navController)
            }
        }

        composable(Routes.RESET_PASSWORD) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                ResetPasswordScreen(navController)
            }
        }

        composable(Routes.MAIN) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                MainScreen(navController)
            }
        }

        composable(Routes.WEATHER_HISTORY_ROUTE) {
            val activity = LocalContext.current as ComponentActivity
            val weatherVm: WeatherViewModel = hiltViewModel(activity)
            val state by weatherVm.state.collectAsState()

            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                WeatherHistoryScreen(
                    state = state,
                    onBackClick = {
                        weatherVm.setHistoryMode(false)
                        navController.popBackStack()
                    },
                    onLoadHistory = { weatherVm.loadHistorical() },
                    onMonthSelected = { monthKey ->
                        weatherVm.selectHistoryMonth(monthKey)
                    },
                    onOpenSelectedDay = {
                        navController.navigate(Routes.WEATHER_HISTORY_DAY_ROUTE)
                    }
                )
            }
        }

        composable(Routes.WEATHER_HISTORY_DAY_ROUTE) {
            val activity = LocalContext.current as ComponentActivity
            val weatherVm: WeatherViewModel = hiltViewModel(activity)
            val state by weatherVm.state.collectAsState()

            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                WeatherHistoryDayScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onDaySelected = { dayKey ->
                        weatherVm.selectHistoryDay(dayKey)
                    }
                )
            }
        }

        composable(Routes.SIMULATOR) {
            SimulatorScreen(navController)
        }

        composable(Routes.SETTINGS) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                SettingsScreen(navController)
            }
        }

        composable(Routes.PROFILE) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.AI_SIMULATION) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                AiSimulationScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
