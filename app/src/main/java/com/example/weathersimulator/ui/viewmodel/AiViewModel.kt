package com.example.weathersimulator.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersimulator.domain.usecase.GenerateAiResponseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.weathersimulator.data.repository.AiChatRepository

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

    init {
        viewModelScope.launch {
            chatRepository.getMessages().collect { list ->
                _state.update {
                    it.copy(
                        messages = list.map {
                            ChatMessage(
                                id = it.id,
                                text = it.text,
                                isFromUser = it.isFromUser
                            )
                        }
                    )
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

    fun send() {
        val prompt = _state.value.prompt.trim()
        val url = _state.value.serverUrl.trim().let {
            if (it.endsWith("/")) it else "$it/"
        }
        if (prompt.isEmpty()) return

        viewModelScope.launch {
            chatRepository.insertMessage(prompt, true)

            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    prompt = ""
                )
            }

            try {
                val ans = generateAiResponse(prompt, url)
                chatRepository.insertMessage(ans, false)

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
}
