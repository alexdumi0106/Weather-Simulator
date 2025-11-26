package com.example.weathersimulator.ui.navigation

import androidx.compose.runtime.Composable
import com.example.weathersimulator.ui.screens.simulator.SimulatorScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import com.example.weathersimulator.ui.screens.auth.LoginScreen
import com.example.weathersimulator.ui.screens.auth.RegisterScreen
import com.example.weathersimulator.ui.screens.auth.ResetPasswordScreen
import com.example.weathersimulator.ui.screens.auth.ProfileScreen
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (FirebaseAuth.getInstance().currentUser != null)
            "home"
        else
            "login"
    ) {

        composable("login") {
            LoginScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        /*composable("resetPassword") {
            ResetPasswordScreen(navController)
        }

        composable("home") {
            SimulatorScreen(navController)
        }

        composable("profile") {
            ProfileScreen(navController)
        }*/
    }
}
