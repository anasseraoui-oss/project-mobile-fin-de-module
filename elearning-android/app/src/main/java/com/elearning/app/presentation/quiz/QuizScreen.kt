package com.elearning.app.presentation.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.elearning.app.presentation.components.CertificateBadge
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeLeft by viewModel.timeLeftSeconds.collectAsState()
    val answers by viewModel.answers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Évaluation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.Close, null) }
                },
                actions = {
                    if (uiState is QuizState.Active) {
                        Surface(
                            color = if (timeLeft < 60) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            shape = Radius.full,
                            modifier = Modifier.padding(end = Spacing.md)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                Icon(Icons.Default.Timer, null, modifier = Modifier.size(16.dp))
                                val minutes = timeLeft / 60
                                val seconds = timeLeft % 60
                                Text("${minutes}:${seconds.toString().padStart(2, '0')}", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is QuizState.Loading -> FullScreenLoader()
                is QuizState.Submitting -> FullScreenLoader()
                is QuizState.Error -> ErrorState((uiState as QuizState.Error).message, viewModel::loadQuiz)
                is QuizState.Result -> {
                    val state = uiState as QuizState.Result
                    Box(modifier = Modifier.fillMaxSize().padding(Spacing.xl), contentAlignment = Alignment.Center) {
                        CertificateBadge(
                            learnerName = "Apprenant (Current User)", // Ideally fetched from AuthState
                            formationTitle = state.quizTitle,
                            score = state.result.score,
                            maxScore = state.result.maxScore,
                            certificateUrl = state.result.certificateUrl,
                            onDownload = { /* Open URL intent */ },
                            onVerify = { /* Open verify intent */ }
                        )
                    }
                }
                is QuizState.Active -> {
                    val quiz = (uiState as QuizState.Active).quiz
                    Column(Modifier.fillMaxSize()) {
                        LinearProgressIndicator(
                            progress = { answers.size.toFloat() / quiz.questions.size.coerceAtLeast(1) },
                            modifier = Modifier.fillMaxWidth().height(4.dp)
                        )
                        LazyColumn(
                            contentPadding = PaddingValues(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(quiz.questions) { index, question ->
                                Text("Question ${index + 1}/${quiz.questions.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(Spacing.xs))
                                Text(question.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(Spacing.md))

                                question.options.forEach { option ->
                                    val isSelected = answers[question.id] == option.id
                                    Surface(
                                        shape = Radius.md,
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth().clickable { viewModel.selectOption(question.id, option.id) }.padding(bottom = Spacing.sm)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(Spacing.md),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSelected, onClick = null)
                                            Spacer(Modifier.width(Spacing.md))
                                            Text(option.text, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Submit button pinned to bottom
                        Button(
                            onClick = viewModel::submitQuiz,
                            enabled = answers.size == quiz.questions.size,
                            modifier = Modifier.fillMaxWidth().padding(Spacing.lg).height(50.dp),
                            shape = Radius.md
                        ) {
                            Text("Soumettre mes réponses")
                        }
                    }
                }
            }
        }
    }
}
