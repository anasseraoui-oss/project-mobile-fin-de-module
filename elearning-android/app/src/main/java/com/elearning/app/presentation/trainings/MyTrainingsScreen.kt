package com.elearning.app.presentation.trainings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.elearning.app.domain.model.Course
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.Seance
import com.elearning.app.domain.model.SeanceStatus
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.ELearningColors
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.FigmaSpacing
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.AnimDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyTrainingsScreen(
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToCatalogue: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyTrainingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        MyTrainingsUiState.Loading -> FullScreenLoader(modifier.background(ELearningColors.AppBackground))
        MyTrainingsUiState.Empty -> MyTrainingsEmptyState(
            onBrowseCatalogue = onNavigateToCatalogue,
            modifier = modifier.background(ELearningColors.AppBackground)
        )
        is MyTrainingsUiState.Error -> ErrorState(
            message = state.message,
            onRetry = state.onRetry,
            modifier = modifier.background(ELearningColors.AppBackground)
        )
        is MyTrainingsUiState.Content -> MyTrainingsContent(
            trainings = state.trainings,
            onNavigateToPlayer = onNavigateToPlayer,
            modifier = modifier
        )
    }
}

@Composable
private fun MyTrainingsEmptyState(
    onBrowseCatalogue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(FigmaSpacing.pageHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.School,
            contentDescription = null,
            tint = ELearningColors.TextTertiary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Parcourez le catalogue pour commencer une formation.",
            style = MaterialTheme.typography.titleMedium,
            color = ELearningColors.TextPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onBrowseCatalogue,
            shape = Radius.md,
            colors = ButtonDefaults.buttonColors(containerColor = ELearningColors.BrandBlue)
        ) {
            Text("Parcourir le catalogue", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MyTrainingsContent(
    trainings: List<TrainingProgress>,
    onNavigateToPlayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ELearningColors.AppBackground),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ELearningColors.CardSurface)
                    .padding(
                        start = FigmaSpacing.pageHorizontal,
                        end = FigmaSpacing.pageHorizontal,
                        top = 44.dp,
                        bottom = 24.dp
                    )
            ) {
                Text(
                    text = "Mes formations",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = ELearningColors.TextPrimary
                )
                Text(
                    text = "Vous avez ${trainings.size} formations en cours",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = ELearningColors.TextTertiary
                )
            }
        }

        items(trainings) { training ->
            MyTrainingCard(
                training = training,
                onNavigateToPlayer = onNavigateToPlayer,
                modifier = Modifier.padding(horizontal = FigmaSpacing.pageHorizontal)
            )
        }
    }
}

@Composable
fun MyTrainingCard(
    training: TrainingProgress,
    onNavigateToPlayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(training.initiallyExpanded) }
    val animatedProgress by animateFloatAsState(
        targetValue = training.formation.progressPercent / 100f,
        animationSpec = tween(AnimDuration.xSlow),
        label = "myTrainingProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = ELearningColors.CardSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(
                if (expanded) ELearningColors.BrandBlue.copy(alpha = 0.3f) else ELearningColors.BorderSubtle
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 6.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .clip(Radius.lg)
                    .background(Color(0xFFE5E7EB))
            ) {
                AsyncImage(
                    model = training.formation.thumbnailUrl,
                    contentDescription = training.formation.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (expanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ELearningColors.BrandBlue.copy(alpha = 0.24f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = training.formation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ELearningColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        color = ELearningColors.BrandBlue,
                        trackColor = Color(0xFFE5E7EB),
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "${training.formation.progressPercent}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ELearningColors.BrandBlue
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    TrainingMeta(Icons.Default.FolderOpen, "${training.courses.size} modules")
                    TrainingMeta(Icons.Default.Schedule, training.totalTime)
                }
            }

            Surface(shape = CircleShape, color = Color(0xFFF9FAFB), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Reduire" else "Developper",
                        tint = ELearningColors.TextTertiary
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HorizontalDivider(color = ELearningColors.BorderSubtle)
                training.courses.forEachIndexed { index, course ->
                    TrainingModuleAccordion(
                        course = course,
                        initiallyExpanded = index == 0,
                        onNavigateToPlayer = onNavigateToPlayer
                    )
                }
            }
        }
    }
}

@Composable
fun TrainingModuleAccordion(
    course: Course,
    initiallyExpanded: Boolean,
    onNavigateToPlayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val completed = course.seances.count { it.isCompleted }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Radius.lg,
        color = Color(0xFFF9FAFB),
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = ELearningColors.BrandBlue, modifier = Modifier.size(18.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ELearningColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$completed/${course.seances.size} seances completees",
                        style = MaterialTheme.typography.labelMedium,
                        color = ELearningColors.TextTertiary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ELearningColors.TextTertiary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                    course.seances.forEachIndexed { index, seance ->
                        val locked = seance.status == SeanceStatus.PLANIFIEE &&
                            !seance.isCompleted &&
                            index > completed
                        TrainingSessionRow(
                            seance = seance,
                            isCurrent = !seance.isCompleted &&
                                !locked &&
                                (seance.status == SeanceStatus.EN_COURS || index == completed),
                            locked = locked,
                            onClick = { onNavigateToPlayer(seance.id.toString()) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TrainingSessionRow(
    seance: Seance,
    isCurrent: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rowColor = if (isCurrent) ELearningColors.CardSurface else Color.Transparent
    val textColor = when {
        locked -> ELearningColors.TextTertiary
        isCurrent -> ELearningColors.TextPrimary
        else -> ELearningColors.TextSecondary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .background(rowColor)
            .clickable(enabled = !locked, onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when {
            seance.isCompleted -> Icons.Default.CheckCircle
            locked -> Icons.Default.Lock
            isCurrent -> Icons.Default.PlayArrow
            else -> Icons.Default.RadioButtonUnchecked
        }
        val iconColor = when {
            seance.isCompleted -> Color(0xFF22C55E)
            locked -> Color(0xFF9CA3AF)
            else -> ELearningColors.BrandBlue
        }
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = seance.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${(seance.durationSeconds / 60).coerceAtLeast(1)} min",
                style = MaterialTheme.typography.labelSmall,
                color = ELearningColors.TextTertiary
            )
        }
        if (!locked && !seance.isCompleted) {
            Icon(Icons.Default.PlayCircle, contentDescription = "Lire", tint = if (isCurrent) ELearningColors.BrandBlue else Color(0xFF9CA3AF), modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun TrainingMeta(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ELearningColors.TextTertiary, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = ELearningColors.TextTertiary)
    }
}

data class TrainingProgress(
    val formation: Formation,
    val courses: List<Course>,
    val totalTime: String,
    val initiallyExpanded: Boolean = false
)

@Preview(showBackground = true)
@Composable
private fun MyTrainingsScreenPreview() {
    ELearningTheme {
        MyTrainingsContent(trainings = emptyList(), onNavigateToPlayer = {})
    }
}
