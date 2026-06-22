package com.elearning.app.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.FormationLevel
import com.elearning.app.presentation.components.EmptyState
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FormationCard
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.ELearningColors
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.FigmaSpacing
import java.util.UUID

@Composable
fun FavoritesScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    uiState: FavoritesUiState? = null,
    viewModel: FavoritesViewModel? = null
) {
    if (uiState != null) {
        FavoritesScreenState(uiState, onNavigateToDetail, modifier)
        return
    }

    val resolvedViewModel = viewModel ?: hiltViewModel()
    val observedUiState by resolvedViewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreenState(observedUiState, onNavigateToDetail, modifier)
}

@Composable
private fun FavoritesScreenState(
    uiState: FavoritesUiState,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        FavoritesUiState.Loading -> FullScreenLoader(modifier.background(ELearningColors.AppBackground))
        is FavoritesUiState.Error -> {
            val state = uiState as FavoritesUiState.Error
            ErrorState(
                message = state.message,
                onRetry = state.onRetry,
                modifier = modifier.background(ELearningColors.AppBackground)
            )
        }
        FavoritesUiState.Empty -> EmptyState(
            message = "Aucun favori pour le moment.",
            icon = Icons.Default.FavoriteBorder,
            modifier = modifier.background(ELearningColors.AppBackground)
        )
        is FavoritesUiState.Content -> {
            val state = uiState as FavoritesUiState.Content
            FavoritesContent(
                formations = state.formations,
                onNavigateToDetail = onNavigateToDetail,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun FavoritesContent(
    formations: List<Formation>,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Favoris",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = ELearningColors.TextPrimary
                )
                Text(
                    text = "${formations.size} formations sauvegardees",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ELearningColors.TextTertiary
                )
            }
        }
        if (formations.isEmpty()) {
            item {
                EmptyState(
                    message = "Aucun favori pour le moment.",
                    icon = Icons.Default.SearchOff,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(420.dp)
                )
            }
        } else {
            items(formations) { formation ->
                FormationCard(
                    formation = formation,
                    onClick = { onNavigateToDetail(formation.id.toString()) }
                )
            }
        }
    }
}

sealed class FavoritesUiState {
    data object Loading : FavoritesUiState()
    data object Empty : FavoritesUiState()
    data class Error(val message: String, val onRetry: () -> Unit = {}) : FavoritesUiState()
    data class Content(val formations: List<Formation>) : FavoritesUiState()
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    ELearningTheme {
        FavoritesScreen(
            onNavigateToDetail = {},
            // Use fake viewmodel or don't preview this directly, but previewing UI state 
        )
    }
}
