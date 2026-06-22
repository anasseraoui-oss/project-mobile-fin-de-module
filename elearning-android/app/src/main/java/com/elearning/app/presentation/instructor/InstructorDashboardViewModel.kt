package com.elearning.app.presentation.instructor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.model.InstructorDashboard
import com.elearning.app.domain.model.InstructorFormationSummary
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.repository.InstructorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstructorDashboardViewModel @Inject constructor(
    private val repository: InstructorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InstructorDashboardUiState>(InstructorDashboardUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var currentStatus: String? = null

    init {
        refresh()
    }

    fun refresh(status: String? = currentStatus) {
        viewModelScope.launch {
            currentStatus = status
            _uiState.value = InstructorDashboardUiState.Loading
            val dashboardResult = repository.getDashboard()
            val formationsResult = repository.getMyFormations(status)
            _uiState.value = when {
                dashboardResult is Result.Success && formationsResult is Result.Success -> {
                    InstructorDashboardUiState.Success(dashboardResult.data, formationsResult.data, status)
                }
                dashboardResult is Result.Error -> InstructorDashboardUiState.Error(
                    dashboardResult.message ?: "Erreur de chargement du tableau de bord"
                )
                formationsResult is Result.Error -> InstructorDashboardUiState.Error(
                    formationsResult.message ?: "Erreur de chargement des formations"
                )
                else -> InstructorDashboardUiState.Error("Erreur inconnue")
            }
        }
    }

    fun archiveFormation(id: String) {
        viewModelScope.launch {
            when (val result = repository.archiveFormation(id)) {
                is Result.Success -> refresh(currentStatus)
                is Result.Error -> _uiState.value = InstructorDashboardUiState.Error(
                    result.message ?: "Erreur lors de l'archivage"
                )
                else -> Unit
            }
        }
    }

    fun deleteFormation(id: String) {
        viewModelScope.launch {
            when (val result = repository.deleteFormation(id)) {
                is Result.Success -> refresh(currentStatus)
                is Result.Error -> _uiState.value = InstructorDashboardUiState.Error(
                    result.message ?: "Erreur lors de la suppression"
                )
                else -> Unit
            }
        }
    }
}

sealed class InstructorDashboardUiState {
    data object Loading : InstructorDashboardUiState()
    data class Success(
        val dashboard: InstructorDashboard,
        val formations: List<InstructorFormationSummary>,
        val selectedStatus: String?
    ) : InstructorDashboardUiState()
    data class Error(val message: String) : InstructorDashboardUiState()
}
