package com.example.weathersimulator.ui.screens.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersimulator.ui.viewmodel.AiViewModel
import com.example.weathersimulator.ui.viewmodel.ChatMessage
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSimulationScreen(
    onBack: () -> Unit,
    vm: AiViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var showEmptyPromptWarning by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }
    var selectedConversationToDelete by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentError = state.error
    val activeConversationTitle = remember(state.conversations, state.selectedConversationId) {
        state.conversations.firstOrNull { it.id == state.selectedConversationId }?.title
            ?: "Conversație nouă"
    }

    LaunchedEffect(state.messages.size, state.isLoading) {
        val totalItems = state.messages.size + if (state.isLoading) 1 else 0
        if (totalItems > 0) {
            listState.scrollToItem(totalItems - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F1D35),
                        Color(0xFF182A45),
                        Color(0xFF243852)
                    )
                )
            )
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color(0xFF10233E),
                    drawerTonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Conversații AI",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )

                        Button(
                            onClick = {
                                vm.newChat()
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3D6FA6),
                                contentColor = Color.White
                            )
                        ) {
                            Text("+ Conversație nouă")
                        }

                        OutlinedButton(
                            onClick = {
                                isDeleteMode = true
                                selectedConversationToDelete = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFBEE7FF)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF3D6FA6),
                                        Color(0xFF86C5FF)
                                    )
                                )
                            )
                        ) {
                            Text("Selectează conversația de șters")
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.conversations, key = { it.id }) { conversation ->
                                NavigationDrawerItem(
                                    label = { Text(conversation.title) },
                                    selected = if (isDeleteMode) {
                                        conversation.id == selectedConversationToDelete
                                    } else {
                                        conversation.id == state.selectedConversationId
                                    },
                                    onClick = {
                                        if (isDeleteMode) {
                                            selectedConversationToDelete = conversation.id
                                        } else {
                                            vm.selectConversation(conversation.id)
                                            scope.launch { drawerState.close() }
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = if (isDeleteMode) Color(0xFF5A2630) else Color(0xFF2A4C79),
                                        unselectedContainerColor = Color.Transparent,
                                        selectedTextColor = Color.White,
                                        unselectedTextColor = Color(0xFFDCEBFA),
                                        selectedIconColor = Color.White,
                                        unselectedIconColor = Color(0xFF9EC5E6)
                                    )
                                )
                            }
                        }

                        if (isDeleteMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        isDeleteMode = false
                                        selectedConversationToDelete = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF9FD3FF)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        Color(0xFF5FA8FF)
                                    )
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Anulează")
                                }

                                Button(
                                    onClick = {
                                        if (selectedConversationToDelete != null) {
                                            showDeleteConfirmDialog = true
                                        }
                                    },
                                    enabled = selectedConversationToDelete != null,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF8A2D3B),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Șterge")
                                }
                            }
                        }
                    }
                }
            },
            scrimColor = Color.Black.copy(alpha = 0.35f)
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Simulare meteo AI",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = activeConversationTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFBEE7FF)
                                )
                            }
                        },
                        navigationIcon = {
                            TextButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Text("Conversații", color = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        color = Color.Transparent,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding()
                                .navigationBarsPadding()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            currentError?.let { errorText ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3A2430)),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = errorText,
                                        color = Color(0xFFFFC0C0),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF132844)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    OutlinedTextField(
                                        value = state.prompt,
                                        onValueChange = {
                                            if (!state.isLoading) vm.onPromptChange(it)
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = !state.isLoading,
                                        shape = RoundedCornerShape(18.dp),
                                        label = { Text("Mesaj") },
                                        placeholder = { Text("Scrie întrebarea ta...") },
                                        minLines = 1,
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF86C5FF),
                                            unfocusedBorderColor = Color(0xFF35557A),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedLabelColor = Color(0xFFBEE7FF),
                                            unfocusedLabelColor = Color(0xFF9DB7D3),
                                            cursorColor = Color.White
                                        )
                                    )

                                    Spacer(Modifier.width(10.dp))

                                    FilledTonalButton(
                                        onClick = {
                                            if (state.prompt.isBlank()) {
                                                showEmptyPromptWarning = true
                                            } else {
                                                vm.send()
                                            }
                                        },
                                        enabled = !state.isLoading,
                                        modifier = Modifier.height(56.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFF3D6FA6),
                                            contentColor = Color.White,
                                            disabledContainerColor = Color(0xFF2C4E73),
                                            disabledContentColor = Color.White.copy(alpha = 0.65f)
                                        )
                                    ) {
                                        if (state.isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Filled.Send,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("Trimite")
                                        }
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
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3D6FA6)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF3D6FA6),
                                                Color(0xFF2C4E73)
                                            )
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color.White.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "AI",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "AI Weather Simulation",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Pune întrebări despre furtuni, parametri meteo sau scenarii de simulare.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFFDBF0FF)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.messages.isEmpty() && !state.isLoading) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF182A45)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Începe o conversație nouă",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Deschide lista de conversații, alege una existentă sau trimite primul mesaj.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFBEE7FF)
                                    )
                                }
                            }
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
                        containerColor = Color(0xFF182A45),
                        titleContentColor = Color.White,
                        textContentColor = Color(0xFFDBF0FF),
                        title = { Text("Prompt gol") },
                        text = { Text("Te rog introdu un text înainte să trimiți mesajul.") },
                        confirmButton = {
                            TextButton(onClick = { showEmptyPromptWarning = false }) {
                                Text("OK")
                            }
                        }
                    )
                }

                if (showDeleteConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirmDialog = false },
                        containerColor = Color(0xFF182A45),
                        titleContentColor = Color.White,
                        textContentColor = Color(0xFFDBF0FF),
                        title = { Text("Ștergi conversația?") },
                        text = { Text("Această conversație va fi eliminată definitiv.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    selectedConversationToDelete?.let { id ->
                                        vm.deleteConversation(id)
                                    }
                                    showDeleteConfirmDialog = false
                                    isDeleteMode = false
                                    selectedConversationToDelete = null
                                }
                            ) {
                                Text("Șterge", color = Color(0xFFFFB4B4))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDeleteConfirmDialog = false }
                            ) {
                                Text("Anulează")
                            }
                        }
                    )
                }
            }
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
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    Color(0xFF2A4C79)
                } else {
                    Color(0xFF1B3558)
                }
            ),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}