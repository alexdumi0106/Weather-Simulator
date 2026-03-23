package com.example.weathersimulator.ui.screens.simulator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersimulator.ui.viewmodel.AiViewModel

@Composable
fun AiSimulationScreen(
    onBack: () -> Unit,
    vm: AiViewModel = hiltViewModel<AiViewModel>()
) {
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("AI Weather Simulation", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onBack) { Text("Back") }
        }

        OutlinedTextField(
            value = state.serverUrl,
            onValueChange = vm::onServerUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Server URL") },
            placeholder = { Text("ex: http://192.168.100.80:8000/") },
            singleLine = true,
            supportingText = { Text("IP-ul PC-ului pe aceeași rețea Wi-Fi ca telefonul") }
        )

        OutlinedTextField(
            value = state.prompt,
            onValueChange = vm::onPromptChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Describe scenario / ask the AI") },
            minLines = 3
        )

        Button(
            onClick = vm::send,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Simulating...")
            } else {
                Text("Start AI Simulation")
            }
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (state.answer.isNotBlank()) {
            Divider()
            Text("AI Output:", style = MaterialTheme.typography.titleMedium)
            Text(state.answer)
        }
    }
}
