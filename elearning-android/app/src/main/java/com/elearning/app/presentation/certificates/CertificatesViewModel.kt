package com.elearning.app.presentation.certificates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.repository.CertificateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CertificatesViewModel @Inject constructor(
    private val repository: CertificateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CertificatesUiState>(CertificatesUiState.Loading)
    val uiState: StateFlow<CertificatesUiState> = _uiState.asStateFlow()

    init {
        loadCertificates()
    }

    fun loadCertificates() {
        _uiState.value = CertificatesUiState.Loading
        viewModelScope.launch {
            when (val result = repository.getCertificates()) {
                is Result.Success -> {
                    val certificates = result.data.map {
                        CertificateUiModel(
                            id = it.id,
                            learnerName = it.learnerName,
                            formationTitle = it.formationTitle,
                            score = it.score,
                            maxScore = it.maxScore,
                            certificateUrl = it.certificateUrl,
                            verificationCode = it.verificationCode
                        )
                    }
                    _uiState.value = if (certificates.isEmpty()) {
                        CertificatesUiState.Empty
                    } else {
                        CertificatesUiState.Content(certificates)
                    }
                }
                is Result.Error -> {
                    _uiState.value = CertificatesUiState.Error(
                        message = result.message ?: result.exception.message ?: "Impossible de charger les certificats.",
                        onRetry = { loadCertificates() }
                    )
                }
                Result.Loading -> _uiState.value = CertificatesUiState.Loading
            }
        }
    }
}
