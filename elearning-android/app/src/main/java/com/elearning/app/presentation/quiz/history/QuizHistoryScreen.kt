package com.elearning.app.presentation.quiz.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elearning.app.domain.repository.QuizHistoryItem
import com.elearning.app.presentation.components.EmptyState
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.ELearningColors
import com.elearning.app.presentation.theme.FigmaSpacing
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing

@Composable
fun QuizHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ELearningColors.AppBackground)
    ) {
        QuizHistoryHeader(onNavigateBack)
        when (val state = uiState) {
            QuizHistoryUiState.Loading -> FullScreenLoader()
            QuizHistoryUiState.Empty -> EmptyState(message = "Aucune tentative de quiz pour le moment.", icon = Icons.Default.History)
            is QuizHistoryUiState.Error -> ErrorState(message = state.message, onRetry = viewModel::loadHistory)
            is QuizHistoryUiState.Content -> QuizHistoryContent(state.attempts)
        }
    }
}

@Composable
private fun QuizHistoryHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 8.dp, end = FigmaSpacing.pageHorizontal, top = 6.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = ELearningColors.TextPrimary)
        }
        Column {
            Text(
                text = "Historique des quiz",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = ELearningColors.TextPrimary
            )
            Text(
                text = "Scores et tentatives terminees",
                style = MaterialTheme.typography.bodyMedium,
                color = ELearningColors.TextTertiary
            )
        }
    }
}

@Composable
private fun QuizHistoryContent(attempts: List<QuizHistoryItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = FigmaSpacing.pageHorizontal,
            end = FigmaSpacing.pageHorizontal,
            top = 12.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(attempts, key = { it.attemptId }) { attempt ->
            QuizHistoryCard(attempt)
        }
    }
}

@Composable
private fun QuizHistoryCard(attempt: QuizHistoryItem) {
    val statusColor = if (attempt.passed) Color(0xFF16A34A) else Color(0xFFDC2626)
    val statusIcon = if (attempt.passed) Icons.Default.CheckCircle else Icons.Default.HighlightOff
    val statusLabel = if (attempt.passed) "Reussi" else "Echoue"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = ELearningColors.CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = statusColor.copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(22.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attempt.quizTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ELearningColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    attempt.formationTitle?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = ELearningColors.TextTertiary)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text("Score ${attempt.score}%") })
                AssistChip(onClick = {}, label = { Text(statusLabel) })
                AssistChip(onClick = {}, label = { Text("Essai ${attempt.attemptNumber}") })
            }

            Text(
                text = attempt.submittedAt ?: "Date non disponible",
                style = MaterialTheme.typography.labelMedium,
                color = ELearningColors.TextTertiary
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (attempt.certificateAvailable) Color(0xFFD97706) else ELearningColors.TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (attempt.certificateAvailable) "Certificat disponible" else "Certificat non disponible",
                    style = MaterialTheme.typography.bodySmall,
                    color = ELearningColors.TextSecondary
                )
            }
        }
    }
}
