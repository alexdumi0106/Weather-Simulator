package com.example.weathersimulator.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val state = viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        TextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") })

        Button(
            onClick = {
                if (password == confirmPassword) {
                    viewModel.register(email, password)
                }
            }
        ) {
            Text("Create account")
        }

        if (state.value.success) {
            navController.navigate("home") {
                popUpTo("register") { inclusive = true }
            }
        }

        if (state.value.error != null) {
            Text("Error: ${state.value.error}", color = Color.Red)
        }
    }
}

