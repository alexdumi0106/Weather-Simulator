package com.example.weathersimulator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersimulator.data.repository.AiChatRepository
import com.example.weathersimulator.domain.usecase.GenerateAiResponseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.example.weathersimulator.data.local.ai.AiSettingsStore

@HiltViewModel
class AiViewModel @Inject constructor(
    private val generateAiResponse: GenerateAiResponseUseCase,
    private val chatRepository: AiChatRepository,
    private val aiSettingsStore: AiSettingsStore
) : ViewModel() {

    private val _state = MutableStateFlow(
        AiUiState(serverUrl = aiSettingsStore.getServerUrl())
    )
    val state: StateFlow<AiUiState> = _state

    private var messagesJob: Job? = null

    private fun currentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    }

    init {
        /*viewModelScope.launch {
            chatRepository.migrateGuestConversations(currentUserId())
        }*/

        viewModelScope.launch {
            chatRepository.getConversations(currentUserId()).collect { list ->
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
        aiSettingsStore.setServerUrl(value)
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
                        title = prompt.take(35),
                        userId = currentUserId()
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

    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversationId)

            if (_state.value.selectedConversationId == conversationId) {
                messagesJob?.cancel()
                _state.update {
                    it.copy(
                        selectedConversationId = null,
                        messages = emptyList(),
                        error = null
                    )
                }
            }
        }
    }
}