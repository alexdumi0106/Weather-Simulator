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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weathersimulator.R
import com.example.weathersimulator.ui.viewmodel.AuthViewModel

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(110.dp))

            Box(
                modifier = Modifier
                    .size(92.dp)
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
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Resetare parolă",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Introdu email-ul pentru resetarea parolei",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(34.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.22f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    AuthInputField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthPrimaryButton(
                        text = "Trimite email pentru resetare",
                        onClick = { viewModel.resetPassword(email) },
                        enabled = !state.isLoading
                    )

                    if (state.isLoading) {
                        Spacer(modifier = Modifier.height(14.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = Color.White,
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

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Înapoi la login",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }
    }
}
