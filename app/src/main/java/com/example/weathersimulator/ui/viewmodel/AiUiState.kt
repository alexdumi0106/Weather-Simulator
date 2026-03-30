package com.example.weathersimulator.ui.viewmodel

data class ChatMessage(
    val id: Long,
    val text: String,
    val isFromUser: Boolean
)

data class AiUiState(
    val prompt: String = "",
    val serverUrl: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
