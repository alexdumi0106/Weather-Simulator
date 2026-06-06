package com.example.weathersimulator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weathersimulator.ui.screens.auth.LoginScreen
import com.example.weathersimulator.ui.screens.auth.RegisterScreen
import com.example.weathersimulator.ui.screens.auth.ResetPasswordScreen
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
import com.example.weathersimulator.ui.screens.skyanalyzer.SkyAnalyzerScreen
import com.example.weathersimulator.ui.screens.outfit.OutfitRecommendationScreen
import com.example.weathersimulator.ui.screens.nature.NatureImpactScreen
import com.example.weathersimulator.ui.screens.games.cloudcatcher.CloudCatcherScreen
import com.example.weathersimulator.ui.screens.games.WeatherGamesScreen
import com.example.weathersimulator.ui.screens.games.memory.WeatherMemoryScreen
import com.example.weathersimulator.ui.screens.quiz.WeatherQuizScreen


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
                    archiveCities = weatherVm.archiveCities,
                    onBackClick = {
                        weatherVm.exitHistoryMode()
                        navController.popBackStack()
                    },
                    onLoadHistory = { 
                        weatherVm.loadHistorical() 
                    },
                    onCitySelected = { 
                        weatherVm.selectArchiveCity(it) 
                    },
                    onSourceSelected = {
                        weatherVm.selectArchiveSource(it)
                    },
                    onMonthSelected = { 
                        weatherVm.selectHistoryMonth(it) 
                    },
                    onOpenSelectedDay = {
                        navController.navigate(Routes.WEATHER_HISTORY_DAY_ROUTE)
                    },
                    onArchiveCitySearchQueryChange = weatherVm::updateArchiveCitySearchQuery,
                    onSearchArchiveCity = weatherVm::searchArchiveCity,
                    onArchiveSearchCitySelected = weatherVm::selectArchiveCityFromSearch
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
                    },
                    onGenerateAiDescription = weatherVm::requestHistoricalDayAiDescription,
                    onGenerateClimateComparison = weatherVm::requestClimateComparison
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

        composable(Routes.AI_SIMULATION) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                AiSimulationScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.SKY_ANALYZER) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                SkyAnalyzerScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.OUTFIT_AI) {
            val activity = LocalContext.current as ComponentActivity
            val weatherVm: WeatherViewModel = hiltViewModel(activity)
            val state by weatherVm.state.collectAsState()

            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                OutfitRecommendationScreen(
                    state = state,
                    cityName = state.selectedCityName,
                    onBack = { navController.popBackStack() },
                    onGenerateClick = weatherVm::generateOutfitRecommendation
                )
            }
        }

        composable(Routes.NATURE_IMPACT) {
            val activity = LocalContext.current as ComponentActivity
            val weatherVm: WeatherViewModel = hiltViewModel(activity)
            val state by weatherVm.state.collectAsState()

            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                NatureImpactScreen(
                    state = state,
                    cityName = state.selectedCityName,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.WEATHER_QUIZ) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                WeatherQuizScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.WEATHER_GAMES) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                WeatherGamesScreen(
                    onBack = { navController.popBackStack() },
                    onCloudCatcherClick = { navController.navigate(Routes.CLOUD_CATCHER) },
                    onWeatherMemoryClick = { navController.navigate(Routes.WEATHER_MEMORY) }
                )
            }
        }

        composable(Routes.CLOUD_CATCHER) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                CloudCatcherScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.WEATHER_MEMORY) {
            WeatherSimulatorTheme(darkTheme = false, dynamicColor = false) {
                WeatherMemoryScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
