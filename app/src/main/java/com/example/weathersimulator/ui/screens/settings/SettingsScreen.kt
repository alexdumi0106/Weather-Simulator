package com.example.weathersimulator.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weathersimulator.ui.navigation.Routes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersimulator.ui.viewmodel.AuthViewModel
import com.example.weathersimulator.ui.screens.auth.AuthBackground
import com.example.weathersimulator.ui.screens.auth.AuthGlassCard
import com.example.weathersimulator.ui.screens.auth.AuthPrimaryButton
import com.example.weathersimulator.ui.screens.auth.AuthTitleColor
import com.example.weathersimulator.ui.screens.auth.AuthSubtitleColor
import com.example.weathersimulator.ui.screens.auth.AuthFieldTextColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val state = viewModel.state.collectAsState().value
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF496A93),
                        Color(0xFF36577E),
                        Color(0xFF29496B)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Înapoi",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Text(
                text = "Setări",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(36.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                )
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Profil",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = state.user?.name
                            ?.takeIf { it.isNotBlank() }
                            ?: "Utilizator necunoscut",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = state.user?.email ?: "Email indisponibil",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            showLogoutDialog = true
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF183A5D)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                    ) {

                        Text(
                            text = "Deconectare",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            containerColor = Color(0xFF17314C),
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Confirmare deconectare",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Ești sigur că dorești să te deconectezi de la aplicație?",
                    color = Color(0xFFBEE7FF),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()

                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                ) {
                    Text(
                        text = "Da, deconectează-mă",
                        color = Color(0xFFFFD6D6),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text(
                        text = "Anulează",
                        color = Color(0xFFBEE7FF),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}

@Composable
private fun <T> UnitSection(
    title: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        options.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label)
                RadioButton(
                    selected = (value == selected),
                    onClick = { onSelect(value) }
                )
            }
        }
        Divider()
    }
}
