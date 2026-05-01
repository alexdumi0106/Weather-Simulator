package com.example.weathersimulator.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersimulator.data.repository.AiChatRepository
import com.example.weathersimulator.domain.usecase.GenerateAiResponseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PREFS_NAME = "ai_settings"
private const val KEY_SERVER_URL = "server_url"
private const val DEFAULT_URL = "http://192.168.100.80:8000/"

@HiltViewModel
class AiViewModel @Inject constructor(
    private val generateAiResponse: GenerateAiResponseUseCase,
    private val chatRepository: AiChatRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        AiUiState(serverUrl = prefs.getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL)
    )
    val state: StateFlow<AiUiState> = _state

    private var messagesJob: Job? = null

    init {
        viewModelScope.launch {
            chatRepository.getConversations().collect { list ->
                val conversations = list.map {
                    AiConversationUi(
                        id = it.id,
                        title = it.title
                    )
                }

                _state.update {
                    it.copy(conversations = conversations)
                }

                if (_state.value.selectedConversationId == null && conversations.isNotEmpty()) {
                    selectConversation(conversations.first().id)
                }
            }
        }
    }

    fun onPromptChange(value: String) {
        _state.update { it.copy(prompt = value) }
    }

    fun onServerUrlChange(value: String) {
        _state.update { it.copy(serverUrl = value) }
        prefs.edit().putString(KEY_SERVER_URL, value).apply()
    }

    fun selectConversation(conversationId: Long) {
        messagesJob?.cancel()

        _state.update {
            it.copy(
                selectedConversationId = conversationId,
                messages = emptyList(),
                error = null
            )
        }

        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(conversationId).collect { list ->
                _state.update {
                    it.copy(
                        messages = list.map { msg ->
                            ChatMessage(
                                id = msg.id,
                                text = msg.text,
                                isFromUser = msg.isFromUser
                            )
                        }
                    )
                }
            }
        }
    }

    fun newChat() {
        messagesJob?.cancel()
        _state.update {
            it.copy(
                selectedConversationId = null,
                messages = emptyList(),
                prompt = "",
                error = null
            )
        }
    }

    fun send() {
        val prompt = _state.value.prompt.trim()
        val url = _state.value.serverUrl.trim().let {
            if (it.endsWith("/")) it else "$it/"
        }

        if (prompt.isEmpty()) return

        viewModelScope.launch {
            val conversationId = _state.value.selectedConversationId
                ?: chatRepository.createConversation(
                    title = prompt.take(35)
                ).also { newId ->
                    selectConversation(newId)
                }

            chatRepository.insertMessage(
                conversationId = conversationId,
                text = prompt,
                isUser = true
            )

            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    prompt = ""
                )
            }

            try {
                val ans = generateAiResponse(prompt, url)

                chatRepository.insertMessage(
                    conversationId = conversationId,
                    text = ans,
                    isUser = false
                )

                _state.update {
                    it.copy(isLoading = false)
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "AI request failed"
                    )
                }
            }
        }
    }

    fun clearCurrentChat() {
        val conversationId = _state.value.selectedConversationId ?: return

        viewModelScope.launch {
            chatRepository.clearConversation(conversationId)
        }
    }
}