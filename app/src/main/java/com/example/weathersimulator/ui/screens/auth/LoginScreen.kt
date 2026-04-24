package com.example.weathersimulator.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weathersimulator.ui.navigation.Routes
import com.example.weathersimulator.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                color = AuthTitleColor,
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Accesează aplicația Weather Simulator AI",
                color = AuthSubtitleColor,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(26.dp))

            AuthGlassCard {
                Column {
                    AuthInputField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        placeholder = "Email"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AuthInputField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = "Parolă",
                        visualTransformation = PasswordVisualTransformation()
                    )

                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AuthPrimaryButton(
                        text = "Login",
                        onClick = { viewModel.login() },
                        enabled = !state.isLoading
                    )

                    if (state.isLoading) {
                        Spacer(modifier = Modifier.height(14.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = AuthAccentColor,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Nu ai cont? Creează unul",
                color = AuthLinkColor,
                modifier = Modifier.clickable { navController.navigate("register") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Ai uitat parola?",
                color = AuthLinkColor,
                modifier = Modifier.clickable { navController.navigate("resetPassword") }
            )
        }
    }

    if (state.success) {
        LaunchedEffect(true) {
            navController.navigate(Routes.MAIN) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }
}
