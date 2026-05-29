package com.elearning.app.presentation.formation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.model.Course
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.repository.FormationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormationDetailViewModel @Inject constructor(
    private val repository: FormationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val formationId: String = checkNotNull(savedStateHandle["formationId"])

    private val _uiState = MutableStateFlow<FormationDetailState>(FormationDetailState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isEnrolling = MutableStateFlow(false)
    val isEnrolling = _isEnrolling.asStateFlow()

    init {
        loadFormationDetails()
    }

    fun loadFormationDetails() {
        viewModelScope.launch {
            _uiState.value = FormationDetailState.Loading
            try {
                val formation = repository.getFormationById(formationId)
                val courses = repository.getCoursesForFormation(formationId)
                
                _uiState.value = FormationDetailState.Success(formation, courses)
            } catch (e: Exception) {
                _uiState.value = FormationDetailState.Error(e.localizedMessage ?: "Erreur réseau inconnue")
            }
        }
    }

    fun enroll() {
        viewModelScope.launch {
            _isEnrolling.value = true
            try {
                repository.enrollInFormation(formationId)
                loadFormationDetails() // Refresh to show enrolled state
            } catch (e: Exception) {
                // Should show a snackbar ideally via a SharedFlow event
            } finally {
                _isEnrolling.value = false
            }
        }
    }
}

sealed class FormationDetailState {
    object Loading : FormationDetailState()
    data class Success(
        val formation: Formation,
        val courses: List<Course>
    ) : FormationDetailState()
    data class Error(val message: String) : FormationDetailState()
}
