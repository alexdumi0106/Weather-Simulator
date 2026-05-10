package com.example.weathersimulator.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weathersimulator.R
import com.example.weathersimulator.ui.navigation.Routes
import com.example.weathersimulator.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.loginscreen_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xCC003B7A),
                            Color(0x66006DB3),
                            Color(0xCC002B5C)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(110.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(22.dp, CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFFFFD166),
                                Color(0xFF6EC6FF)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sun),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Bine ai venit!",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Conectează-te pentru a accesa Weather Simulator AI!",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(34.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.22f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    AuthInputField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        placeholder = "Email"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AuthInputField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = "Parolă",
                        visualTransformation = PasswordVisualTransformation()
                    )

                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = state.error!!,
                            color = Color(0xFFFFD6D6),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    AuthPrimaryButton(
                        text = if (state.isLoading) "Se conectează..." else "Login",
                        onClick = { viewModel.login() },
                        enabled = !state.isLoading
                    )

                    if (state.isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Nu ai cont? Creează unul!",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Ai uitat parola?",
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 17.sp,
                modifier = Modifier.clickable {
                    navController.navigate("resetPassword")
                }
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