package com.example.weathersimulator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weathersimulator.ui.screens.simulator.SimulatorScreen
import com.example.weathersimulator.ui.screens.auth.LoginScreen
import com.example.weathersimulator.ui.screens.auth.RegisterScreen
import com.example.weathersimulator.ui.screens.auth.ResetPasswordScreen
import com.example.weathersimulator.ui.screens.auth.ProfileScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val startDestination =
        if (FirebaseAuth.getInstance().currentUser != null) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        composable("resetPassword") {
            ResetPasswordScreen(navController)
        }

        composable("home") {
            SimulatorScreen(navController)
        }

        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
