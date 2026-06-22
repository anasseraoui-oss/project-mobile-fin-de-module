package com.elearning.app.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        _uiState.value = NotificationsUiState.Loading
        viewModelScope.launch {
            when (val result = repository.getNotifications()) {
                is Result.Success -> {
                    _uiState.value = if (result.data.isEmpty()) {
                        NotificationsUiState.Empty
                    } else {
                        NotificationsUiState.Content(result.data)
                    }
                }
                is Result.Error -> {
                    _uiState.value = NotificationsUiState.Error(
                        message = result.message ?: result.exception.message ?: "Impossible de charger les notifications.",
                        onRetry = { loadNotifications() }
                    )
                }
                Result.Loading -> _uiState.value = NotificationsUiState.Loading
            }
        }
    }
}
