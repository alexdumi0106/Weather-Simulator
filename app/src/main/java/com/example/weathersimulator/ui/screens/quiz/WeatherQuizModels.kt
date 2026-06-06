package com.example.weathersimulator.ui.screens.quiz

import com.example.weathersimulator.data.repository.WeatherQuizQuestion

enum class WeatherQuizPhase {
    Setup,
    Loading,
    Playing,
    Finished
}

data class WeatherQuizUiState(
    val phase: WeatherQuizPhase = WeatherQuizPhase.Setup,
    val requestedQuestionCount: Int = 5,
    val questions: List<WeatherQuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<String, Int> = emptyMap(),
    val loadedFromFallback: Boolean = false,
    val error: String? = null
) {
    val currentQuestion: WeatherQuizQuestion?
        get() = questions.getOrNull(currentQuestionIndex)

    val answeredCount: Int
        get() = selectedAnswers.size

    val score: Int
        get() = questions.count { question ->
            selectedAnswers[question.id] == question.correctIndex
        }

    val isCurrentQuestionAnswered: Boolean
        get() {
            val questionId = currentQuestion?.id ?: return false
            return questionId in selectedAnswers
        }

    val isLastQuestion: Boolean
        get() = currentQuestionIndex == questions.lastIndex

    val wrongAnswers: List<WeatherQuizQuestion>
        get() = questions.filter { question ->
            val selectedIndex = selectedAnswers[question.id]
            selectedIndex != null && selectedIndex != question.correctIndex
        }

    fun selectedAnswerFor(question: WeatherQuizQuestion): Int? {
        return selectedAnswers[question.id]
    }
}
