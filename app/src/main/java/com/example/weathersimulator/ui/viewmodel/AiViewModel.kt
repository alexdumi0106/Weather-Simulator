package com.example.weathersimulator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersimulator.domain.usecase.GenerateAiResponseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val generateAiResponse: GenerateAiResponseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AiUiState())
    val state: StateFlow<AiUiState> = _state

    fun onPromptChange(value: String) {
        _state.update { it.copy(prompt = value) }
    }

    fun send() {
        val prompt = _state.value.prompt.trim()
        if (prompt.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val ans = generateAiResponse(prompt)
                _state.update { it.copy(isLoading = false, answer = ans) }
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
