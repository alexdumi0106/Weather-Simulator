package com.example.weathersimulator.ui.screens.simulator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersimulator.ui.viewmodel.AiViewModel
import com.example.weathersimulator.ui.viewmodel.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSimulationScreen(
    onBack: () -> Unit,
    vm: AiViewModel = hiltViewModel<AiViewModel>()
) {
    val state by vm.state.collectAsState()
    var showEmptyPromptWarning by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.isLoading) {
        val totalItems = state.messages.size + if (state.isLoading) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Weather Simulation") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    state.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = state.prompt,
                            onValueChange = {
                                if (!state.isLoading) vm.onPromptChange(it)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !state.isLoading,
                            label = { Text("Mesaj") },
                            placeholder = { Text("Scrie intrebarea ta...") },
                            minLines = 1,
                            maxLines = 4
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (state.prompt.isBlank()) {
                                    showEmptyPromptWarning = true
                                } else {
                                    vm.send()
                                }
                            },
                            enabled = !state.isLoading,
                            modifier = Modifier.height(56.dp)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Trimite")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.messages.isEmpty() && !state.isLoading) {
                item {
                    Text(
                        text = "Incepe conversatia cu un mesaj.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(state.messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }

            if (state.isLoading) {
                item {
                    ChatBubble(
                        message = ChatMessage(
                            id = -1L,
                            text = "AI scrie...",
                            isFromUser = false
                        )
                    )
                }
            }
        }

        if (showEmptyPromptWarning) {
            AlertDialog(
                onDismissRequest = { showEmptyPromptWarning = false },
                title = { Text("Prompt gol") },
                text = { Text("Te rog introdu un text in prompt inainte sa trimiti mesajul.") },
                confirmButton = {
                    TextButton(onClick = { showEmptyPromptWarning = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
