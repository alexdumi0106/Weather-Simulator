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
import com.example.weathersimulator.ui.screens.settings.SettingsScreen
import com.example.weathersimulator.ui.screens.simulator.SimulatorScreen
import com.google.firebase.auth.FirebaseAuth

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
            LoginScreen(navController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController)
        }

        composable(Routes.RESET_PASSWORD) {
            ResetPasswordScreen(navController)
        }

        composable(Routes.MAIN) {
            MainScreen(navController)
        }

        composable(Routes.SIMULATOR) {
            SimulatorScreen(navController)
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(navController)
        }

        composable(Routes.PROFILE) {
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
}
