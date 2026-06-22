package com.elearning.app.presentation.certificates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elearning.app.core.config.NetworkConfig
import com.elearning.app.presentation.components.CertificateBadge
import com.elearning.app.presentation.components.EmptyState
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.ELearningColors
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.FigmaSpacing

@Composable
fun CertificatesScreen(
    modifier: Modifier = Modifier,
    uiState: CertificatesUiState? = null,
    viewModel: CertificatesViewModel? = null
) {
    if (uiState != null) {
        CertificatesScreenState(uiState, modifier)
        return
    }

    val resolvedViewModel = viewModel ?: hiltViewModel()
    val observedUiState by resolvedViewModel.uiState.collectAsStateWithLifecycle()
    CertificatesScreenState(observedUiState, modifier)
}

@Composable
private fun CertificatesScreenState(
    uiState: CertificatesUiState,
    modifier: Modifier = Modifier
) {
    when (val state = uiState) {
        CertificatesUiState.Loading -> FullScreenLoader(modifier.background(ELearningColors.AppBackground))
        is CertificatesUiState.Error -> ErrorState(
            message = state.message,
            onRetry = state.onRetry,
            modifier = modifier.background(ELearningColors.AppBackground)
        )
        CertificatesUiState.Empty -> EmptyState(
            message = "Aucun certificat obtenu pour le moment.",
            icon = Icons.Default.EmojiEvents,
            modifier = modifier.background(ELearningColors.AppBackground)
        )
        is CertificatesUiState.Content -> CertificatesContent(
            certificates = state.certificates,
            modifier = modifier
        )
    }
}

@Composable
private fun CertificatesContent(
    certificates: List<CertificateUiModel>,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ELearningColors.AppBackground),
        contentPadding = PaddingValues(
            start = FigmaSpacing.pageHorizontal,
            end = FigmaSpacing.pageHorizontal,
            top = 44.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Certificats",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = ELearningColors.TextPrimary
                )
                Text(
                    text = "${certificates.size} certificats disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ELearningColors.TextTertiary
                )
            }
        }
        if (certificates.isEmpty()) {
            item {
                EmptyState(
                    message = "Aucun certificat obtenu pour le moment.",
                    icon = Icons.Default.EmojiEvents,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(420.dp)
                )
            }
        } else {
            items(certificates) { certificate ->
                CertificateBadge(
                    learnerName = certificate.learnerName,
                    formationTitle = certificate.formationTitle,
                    score = certificate.score,
                    maxScore = certificate.maxScore,
                    certificateUrl = certificate.certificateUrl,
                    onDownload = {
                        certificate.certificateUrl?.let(uriHandler::openUri)
                    },
                    onVerify = {
                        certificate.verificationCode?.let {
                            uriHandler.openUri("${NetworkConfig.RESOURCE_SERVER_URL}verify/$it")
                        }
                    }
                )
            }
        }
    }
}

sealed class CertificatesUiState {
    data object Loading : CertificatesUiState()
    data object Empty : CertificatesUiState()
    data class Error(val message: String, val onRetry: () -> Unit = {}) : CertificatesUiState()
    data class Content(val certificates: List<CertificateUiModel>) : CertificatesUiState()
}

data class CertificateUiModel(
    val id: String,
    val learnerName: String,
    val formationTitle: String,
    val score: Int,
    val maxScore: Int,
    val certificateUrl: String?,
    val verificationCode: String?
)

@Preview(showBackground = true)
@Composable
private fun CertificatesScreenPreview() {
    ELearningTheme {
        CertificatesContent(certificates = emptyList())
    }
}
