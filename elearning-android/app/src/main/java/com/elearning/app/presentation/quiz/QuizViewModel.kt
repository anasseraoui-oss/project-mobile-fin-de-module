package com.elearning.app.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.repository.Quiz
import com.elearning.app.domain.repository.QuizRepository
import com.elearning.app.domain.repository.QuizResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val quizId: String = checkNotNull(savedStateHandle["quizId"])

    private val _uiState = MutableStateFlow<QuizState>(QuizState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _timeLeftSeconds = MutableStateFlow(0)
    val timeLeftSeconds = _timeLeftSeconds.asStateFlow()

    private val _answers = MutableStateFlow<Map<String, String>>(emptyMap())
    val answers = _answers.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadQuiz()
    }

    fun loadQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizState.Loading
            try {
                val quiz = repository.getQuiz(quizId)
                _uiState.value = QuizState.Active(quiz)
                _timeLeftSeconds.value = quiz.durationMinutes * 60
                startTimer()
            } catch (e: Exception) {
                _uiState.value = QuizState.Error(e.localizedMessage ?: "Erreur réseau")
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeftSeconds.value > 0 && _uiState.value is QuizState.Active) {
                delay(1000L)
                _timeLeftSeconds.value -= 1
                if (_timeLeftSeconds.value == 0) {
                    submitQuiz()
                }
            }
        }
    }

    fun selectOption(questionId: String, optionId: String) {
        _answers.update { currentMap ->
            currentMap.toMutableMap().apply { put(questionId, optionId) }
        }
    }

    fun submitQuiz() {
        val currentState = _uiState.value
        if (currentState !is QuizState.Active) return

        timerJob?.cancel()
        
        viewModelScope.launch {
            _uiState.value = QuizState.Submitting
            try {
                val result = repository.submitAnswers(
                    quizId = currentState.quiz.id,
                    attemptId = currentState.quiz.attemptId,
                    answers = _answers.value
                )
                _uiState.value = QuizState.Result(currentState.quiz.title, result)
            } catch (e: Exception) {
                _uiState.value = QuizState.Error("Erreur de soumission : ${e.localizedMessage}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

sealed class QuizState {
    object Loading : QuizState()
    data class Active(val quiz: Quiz) : QuizState()
    object Submitting : QuizState()
    data class Result(val quizTitle: String, val result: QuizResult) : QuizState()
    data class Error(val message: String) : QuizState()
}
