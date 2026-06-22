package com.elearning.app.presentation.quiz.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.repository.QuizHistoryItem
import com.elearning.app.domain.usecase.quiz.GetQuizHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizHistoryViewModel @Inject constructor(
    private val getQuizHistoryUseCase: GetQuizHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizHistoryUiState>(QuizHistoryUiState.Loading)
    val uiState: StateFlow<QuizHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        _uiState.value = QuizHistoryUiState.Loading
        viewModelScope.launch {
            runCatching { getQuizHistoryUseCase() }
                .onSuccess { attempts ->
                    _uiState.value = if (attempts.isEmpty()) {
                        QuizHistoryUiState.Empty
                    } else {
                        QuizHistoryUiState.Content(attempts)
                    }
                }
                .onFailure { error ->
                    _uiState.value = QuizHistoryUiState.Error(
                        message = error.message ?: "Impossible de charger l'historique des quiz"
                    )
                }
        }
    }
}

sealed class QuizHistoryUiState {
    data object Loading : QuizHistoryUiState()
    data object Empty : QuizHistoryUiState()
    data class Content(val attempts: List<QuizHistoryItem>) : QuizHistoryUiState()
    data class Error(val message: String) : QuizHistoryUiState()
}
