package com.example.weathersimulator.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersimulator.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val state = viewModel.state.collectAsState().value

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Profil",
                color = AuthTitleColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Detaliile contului tău",
                color = AuthSubtitleColor,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthGlassCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Conectat ca:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AuthFieldTextColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = state.user?.name ?: "Unknown user",
                        style = MaterialTheme.typography.titleMedium,
                        color = AuthFieldTextColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = state.user?.email ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuthFieldTextColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthPrimaryButton(
                        text = "Logout",
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Înapoi",
                color = AuthLinkColor,
                modifier = Modifier.clickable { onBack() }
            )
        }
    }
}

