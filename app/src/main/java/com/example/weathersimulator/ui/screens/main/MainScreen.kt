package com.example.weathersimulator.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weathersimulator.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.*
import com.example.weathersimulator.ui.sensors.location.LocationViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {

    val activity = LocalContext.current as ComponentActivity
    val locationVm: LocationViewModel = hiltViewModel(activity)
    val s = locationVm.state.collectAsState().value

    var showDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Weather Simulator AI") }
                )
                val label = s.placeName ?: "Determin locația..."
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { navController.navigate(Routes.SIMULATOR) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Weather Simulation")
            }

            Button(
                onClick = { navController.navigate(Routes.SETTINGS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings (Units)")
            }

            OutlinedButton(
                onClick = { navController.navigate(Routes.PROFILE) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Profile")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Logout") },
                    text = { Text("Are you sure you want to log out?") },
                    confirmButton = {
                        TextButton(onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.MAIN) { inclusive = true }
                            }
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

        }
    }
}
