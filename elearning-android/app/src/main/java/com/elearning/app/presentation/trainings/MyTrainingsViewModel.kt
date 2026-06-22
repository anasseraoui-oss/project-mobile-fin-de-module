package com.elearning.app.presentation.trainings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.repository.FormationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyTrainingsViewModel @Inject constructor(
    private val repository: FormationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyTrainingsUiState>(MyTrainingsUiState.Loading)
    val uiState: StateFlow<MyTrainingsUiState> = _uiState.asStateFlow()

    init {
        observeLocalTrainings()
        loadTrainings()
    }

    private fun observeLocalTrainings() {
        viewModelScope.launch {
            repository.observeEnrolledFormations().collectLatest { formations ->
                if (_uiState.value is MyTrainingsUiState.Loading && formations.isEmpty()) return@collectLatest
                _uiState.value = buildState(formations)
            }
        }
    }

    fun loadTrainings() {
        _uiState.value = MyTrainingsUiState.Loading
        viewModelScope.launch {
            when (val result = repository.refreshEnrolledFormations()) {
                is Result.Success -> {
                    _uiState.value = buildState(result.data)
                }
                is Result.Error -> {
                    _uiState.value = MyTrainingsUiState.Error(
                        message = result.message ?: result.exception.message ?: "Impossible de charger vos formations.",
                        onRetry = { loadTrainings() }
                    )
                }
                Result.Loading -> _uiState.value = MyTrainingsUiState.Loading
            }
        }
    }

    private suspend fun buildState(formations: List<com.elearning.app.domain.model.Formation>): MyTrainingsUiState {
        val trainings = formations.mapIndexed { index, formation ->
            val courses = runCatching {
                repository.getCoursesForFormation(formation.id.toString())
            }.getOrElse { emptyList() }
            TrainingProgress(
                formation = formation,
                courses = courses,
                totalTime = "${formation.durationHours}h",
                initiallyExpanded = index == 0
            )
        }
        return if (trainings.isEmpty()) {
            MyTrainingsUiState.Empty
        } else {
            MyTrainingsUiState.Content(trainings)
        }
    }
}

sealed class MyTrainingsUiState {
    data object Loading : MyTrainingsUiState()
    data object Empty : MyTrainingsUiState()
    data class Error(val message: String, val onRetry: () -> Unit = {}) : MyTrainingsUiState()
    data class Content(val trainings: List<TrainingProgress>) : MyTrainingsUiState()
}
