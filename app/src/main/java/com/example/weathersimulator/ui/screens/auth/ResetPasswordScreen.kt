package com.example.weathersimulator.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weathersimulator.ui.viewmodel.AuthViewModel
import androidx.compose.ui.graphics.Color

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
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
                text = "Resetare parolă",
                color = AuthTitleColor,
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Introdu email-ul pentru resetarea parolei",
                color = AuthSubtitleColor,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            AuthGlassCard {
                Column {
                    AuthInputField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AuthPrimaryButton(
                        text = "Trimite email pentru resetare",
                        onClick = { viewModel.resetPassword(email) },
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

                    if (state.success) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Email trimis cu succes!",
                            color = Color(0xFF9DF0AE)
                        )
                    }

                    state.error?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Înapoi la login",
                color = AuthLinkColor,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }
    }
}
