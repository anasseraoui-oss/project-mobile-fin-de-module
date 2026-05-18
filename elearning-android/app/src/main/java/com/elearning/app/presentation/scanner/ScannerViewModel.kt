package com.elearning.app.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    // private val repository: ScannerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerState>(ScannerState.Scanning)
    val uiState = _uiState.asStateFlow()

    fun onQrCodeScanned(qrContent: String) {
        if (_uiState.value !is ScannerState.Scanning) return // Prevent double scans

        viewModelScope.launch {
            _uiState.value = ScannerState.Processing
            try {
                // TODO: repository.markPresence(qrContent)
                kotlinx.coroutines.delay(1000) // Mock API call POST /seances/{qrContent}/presence
                _uiState.value = ScannerState.Success("Présence validée avec succès !")
            } catch (e: Exception) {
                _uiState.value = ScannerState.Error(e.localizedMessage ?: "Code QR invalide ou réseau indisponible")
            }
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
