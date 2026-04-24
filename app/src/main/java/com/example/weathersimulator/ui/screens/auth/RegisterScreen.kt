package com.example.weathersimulator.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
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
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Înregistrare",
                color = AuthTitleColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Creează contul tău Weather Simulator AI",
                color = AuthSubtitleColor,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            AuthGlassCard {
                Column {
                    AuthInputField(
                        value = state.firstName,
                        onValueChange = viewModel::onFirstNameChange,
                        placeholder = "Nume"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AuthInputField(
                        value = state.lastName,
                        onValueChange = viewModel::onLastNameChange,
                        placeholder = "Prenume"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))

                    AuthInputField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        placeholder = "Confirmă parola",
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
                        text = "Creează cont",
                        onClick = { viewModel.register() },
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ai deja cont? Login",
                color = AuthLinkColor,
                modifier = Modifier.clickable { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (state.success) {
        LaunchedEffect(true) {
            navController.navigate(Routes.MAIN) {
                popUpTo(Routes.REGISTER) { inclusive = true }
            }
        }
    }
}
