package com.elearning.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.FormationCategory
import com.elearning.app.domain.repository.FormationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FormationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val categories = when (val categoryResult = repository.getCategories()) {
                    is com.elearning.app.domain.model.Result.Success -> categoryResult.data
                    else -> emptyList()
                }
                when (val result = repository.getRecommendedFormations()) {
                    is com.elearning.app.domain.model.Result.Success -> {
                        _uiState.value = HomeUiState.Success(
                            recommendedFormations = result.data,
                            categories = categories
                        )
                    }
                    is com.elearning.app.domain.model.Result.Error -> {
                        _uiState.value = HomeUiState.Error(result.message ?: "Erreur inconnue")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Erreur réseau")
            }
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(
        val recommendedFormations: List<Formation>,
        val categories: List<FormationCategory>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
