package com.example.weathersimulator.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state = viewModel.state.collectAsState()

    Column(...) {

        TextField(...)
        TextField(...)

        Button(onClick = { viewModel.login(email, password) }) {
            Text("Login")
        }

        // Navigare după succes
        if (state.value.success) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }

        if (state.value.error != null) {
            Text("Error: ${state.value.error}", color = Color.Red)
        }

        Text(
            "Don't have an account? Register",
            modifier = Modifier.clickable {
                navController.navigate("register")
            }
        )
    }
}


