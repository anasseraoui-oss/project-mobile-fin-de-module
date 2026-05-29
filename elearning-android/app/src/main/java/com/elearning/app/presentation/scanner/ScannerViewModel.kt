package com.elearning.app.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerState>(ScannerState.Scanning)
    val uiState = _uiState.asStateFlow()

    fun onQrCodeScanned(qrContent: String) {
        if (_uiState.value !is ScannerState.Scanning) return

        viewModelScope.launch {
            _uiState.value = ScannerState.Processing
            _uiState.value = ScannerState.Error(
                "Validation de presence indisponible : endpoint backend non connecte pour $qrContent."
            )
        }
    }

    fun resetScanner() {
        _uiState.value = ScannerState.Scanning
    }
}

sealed class ScannerState {
    object Scanning : ScannerState()
    object Processing : ScannerState()
    data class Success(val message: String) : ScannerState()
    data class Error(val message: String) : ScannerState()
}
