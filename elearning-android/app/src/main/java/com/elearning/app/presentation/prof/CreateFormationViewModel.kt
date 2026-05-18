package com.elearning.app.presentation.prof

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateFormationViewModel @Inject constructor(
    // Injection du repository Professeur pour les appels API (POST /api/v1/formations)
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    private val _uiState = MutableStateFlow<CreateFormationState>(CreateFormationState.Idle)
    val uiState = _uiState.asStateFlow()

    fun updateTitle(t: String) { _title.value = t }
    fun updateDescription(d: String) { _description.value = d }
    fun updatePrice(p: String) { _price.value = p }

    fun submitFormation() {
        if (_title.value.isBlank() || _description.value.isBlank()) {
            _uiState.value = CreateFormationState.Error("Veuillez remplir tous les champs")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = CreateFormationState.Loading
            try {
                // Appel API réel POST /api/v1/formations via repository
                kotlinx.coroutines.delay(1000) // Simulation temps réseau
                _uiState.value = CreateFormationState.Success
            } catch (e: Exception) {
                _uiState.value = CreateFormationState.Error(e.localizedMessage ?: "Erreur lors de la création")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = CreateFormationState.Idle
    }
}

sealed class CreateFormationState {
    object Idle : CreateFormationState()
    object Loading : CreateFormationState()
    object Success : CreateFormationState()
    data class Error(val message: String) : CreateFormationState()
}
