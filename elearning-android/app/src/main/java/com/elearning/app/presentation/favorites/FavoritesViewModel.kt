package com.elearning.app.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        _uiState.value = FavoritesUiState.Loading
        viewModelScope.launch {
            when (val result = repository.getFavorites()) {
                is Result.Success -> {
                    val formations = result.data
                    if (formations.isEmpty()) {
                        _uiState.value = FavoritesUiState.Empty
                    } else {
                        _uiState.value = FavoritesUiState.Content(formations)
                    }
                }
                is Result.Error -> {
                    _uiState.value = FavoritesUiState.Error(
                        message = result.message ?: result.exception.message ?: "Une erreur inattendue s'est produite lors de la connexion au serveur.",
                        onRetry = { loadFavorites() }
                    )
                }
                Result.Loading -> {
                    _uiState.value = FavoritesUiState.Loading
                }
            }
        }
    }
}
