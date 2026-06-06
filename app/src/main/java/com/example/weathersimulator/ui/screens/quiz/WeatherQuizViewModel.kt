package com.example.weathersimulator.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersimulator.data.repository.WeatherQuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WeatherQuizViewModel @Inject constructor(
    private val repository: WeatherQuizRepository
) : ViewModel() {
    private val _state = MutableStateFlow(WeatherQuizUiState())
    val state: StateFlow<WeatherQuizUiState> = _state.asStateFlow()

    fun chooseQuestionCount(count: Int) {
        _state.update {
            it.copy(requestedQuestionCount = count)
        }
    }

    fun startQuiz(count: Int = _state.value.requestedQuestionCount) {
        _state.update {
            it.copy(
                phase = WeatherQuizPhase.Loading,
                requestedQuestionCount = count,
                questions = emptyList(),
                currentQuestionIndex = 0,
                selectedAnswers = emptyMap(),
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val questionSet = repository.loadRandomQuestions(count)
                _state.update {
                    if (questionSet.questions.isEmpty()) {
                        it.copy(
                            phase = WeatherQuizPhase.Setup,
                            error = "Nu am gasit intrebari pentru quiz."
                        )
                    } else {
                        it.copy(
                            phase = WeatherQuizPhase.Playing,
                            questions = questionSet.questions,
                            loadedFromFallback = questionSet.loadedFromFallback
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        phase = WeatherQuizPhase.Setup,
                        error = "Nu am putut incarca intrebarile. Incearca din nou."
                    )
                }
            }
        }
    }

    fun selectAnswer(answerIndex: Int) {
        val question = _state.value.currentQuestion ?: return
        if (question.id in _state.value.selectedAnswers) return

        _state.update { state ->
            state.copy(
                selectedAnswers = state.selectedAnswers + (question.id to answerIndex)
            )
        }
    }

    fun goToNextQuestion() {
        _state.update { state ->
            when {
                state.currentQuestion == null -> state
                !state.isCurrentQuestionAnswered -> state
                state.isLastQuestion -> state.copy(phase = WeatherQuizPhase.Finished)
                else -> state.copy(currentQuestionIndex = state.currentQuestionIndex + 1)
            }
        }
    }

    fun finishQuiz() {
        _state.update {
            if (it.questions.isEmpty()) it else it.copy(phase = WeatherQuizPhase.Finished)
        }
    }

    fun resetQuiz() {
        _state.value = WeatherQuizUiState(
            requestedQuestionCount = _state.value.requestedQuestionCount
        )
    }
}
