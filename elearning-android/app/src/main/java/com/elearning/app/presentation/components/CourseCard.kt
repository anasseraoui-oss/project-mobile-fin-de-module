package com.elearning.app.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elearning.app.domain.model.Seance
import com.elearning.app.domain.model.SeanceStatus
import com.elearning.app.domain.model.SeanceType
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing
import java.util.UUID

@Composable
fun CourseCard(
    courseTitle: String,
    courseIndex: Int,
    seances: List<Seance>,
    initiallyExpanded: Boolean = false,
    onSeanceClick: (Seance) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "chevron_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Module ${courseIndex + 1}: $courseTitle"
            },
        shape = Radius.md,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ── Header Row (always visible) ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Module number badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = Radius.full,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${courseIndex + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = courseTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${seances.size} séance${if (seances.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chevron with rotation animation
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Réduire" else "Développer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation)
                )
            }

            // ── Expanded Seance List ───────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(250)) + fadeIn(tween(200)),
                exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(tween(150))
            ) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    if (seances.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.xl),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucune séance disponible",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        seances.forEachIndexed { index, seance ->
                            SeanceItem(
                                seance = seance,
                                seanceIndex = index,
                                onClick = { onSeanceClick(seance) }
                            )
                            if (index < seances.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.xs))
                }
            }
        }
    }
}

// ─── SeanceItem ───────────────────────────────────────────────────────────────

@Composable
fun SeanceItem(
    seance: Seance,
    seanceIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ELearningTheme.extended
    val isLocked = seance.status == SeanceStatus.PLANIFIEE &&
            seanceIndex > 0 && !seance.isCompleted

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked, onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
            .semantics {
                contentDescription = "Séance ${seanceIndex + 1}: ${seance.title}"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Type Icon
        Surface(
            color = seance.type.containerColor(),
            shape = Radius.sm,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = seance.type.icon(),
                    contentDescription = null,
                    tint = seance.type.iconColor(),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = seance.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (seance.isCompleted) FontWeight.Normal else FontWeight.Medium,
                color = if (isLocked)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Duration
                val minutes = seance.durationSeconds / 60
                Text(
                    text = if (minutes > 0) "${minutes} min" else "< 1 min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Progress pill (if partially watched)
                if (seance.progressSeconds > 0 && !seance.isCompleted) {
                    val progressPct = (seance.progressSeconds * 100 / seance.durationSeconds.coerceAtLeast(1))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = Radius.full
                    ) {
                        Text(
                            text = "$progressPct%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.xxs)
                        )
                    }
                }
            }
        }

        // Status icon (right side)
        when {
            seance.isCompleted -> Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Complété",
                tint = extendedColors.success,
                modifier = Modifier.size(20.dp)
            )
            isLocked -> Icon(
                Icons.Default.Lock,
                contentDescription = "Verrouillé",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
            else -> Icon(
                Icons.Default.PlayCircleOutline,
                contentDescription = "Disponible",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── SeanceType Extensions ────────────────────────────────────────────────────

@Composable
private fun SeanceType.icon(): ImageVector = when (this) {
    SeanceType.VIDEO    -> Icons.Default.PlayCircle
    SeanceType.LIVE     -> Icons.Default.Videocam
    SeanceType.DOCUMENT -> Icons.Default.Description
    SeanceType.QUIZ     -> Icons.Default.Quiz
}

@Composable
private fun SeanceType.containerColor(): Color = when (this) {
    SeanceType.VIDEO    -> MaterialTheme.colorScheme.primaryContainer
    SeanceType.LIVE     -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
    SeanceType.DOCUMENT -> MaterialTheme.colorScheme.secondaryContainer
    SeanceType.QUIZ     -> MaterialTheme.colorScheme.tertiaryContainer
}

@Composable
private fun SeanceType.iconColor(): Color = when (this) {
    SeanceType.VIDEO    -> MaterialTheme.colorScheme.primary
    SeanceType.LIVE     -> MaterialTheme.colorScheme.error
    SeanceType.DOCUMENT -> MaterialTheme.colorScheme.secondary
    SeanceType.QUIZ     -> MaterialTheme.colorScheme.tertiary
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewSeances = listOf(
    Seance(
        id = previewUuid("course-card-seance-1"),
        courseId = previewUuid("course-card"),
        title = "Introduction à Jetpack Compose",
        description = null,
        type = SeanceType.VIDEO,
        status = SeanceStatus.EN_COURS,
        durationSeconds = 600,
        progressSeconds = 600,
        videoKey = null,
        meetingLink = null,
        scheduledAt = null,
        isCompleted = true,
        orderIndex = 1
    ),
    Seance(
        id = previewUuid("course-card-seance-2"),
        courseId = previewUuid("course-card"),
        title = "State Hoisting et Data Flow",
        description = null,
        type = SeanceType.VIDEO,
        status = SeanceStatus.EN_COURS,
        durationSeconds = 900,
        progressSeconds = 450,
        videoKey = null,
        meetingLink = null,
        scheduledAt = null,
        isCompleted = false,
        orderIndex = 2
    ),
    Seance(
        id = previewUuid("course-card-seance-3"),
        courseId = previewUuid("course-card"),
        title = "Quiz d'évaluation",
        description = null,
        type = SeanceType.QUIZ,
        status = SeanceStatus.PLANIFIEE,
        durationSeconds = 1200,
        progressSeconds = 0,
        videoKey = null,
        meetingLink = null,
        scheduledAt = null,
        isCompleted = false,
        orderIndex = 3
    )
)

private fun previewUuid(seed: String): UUID = UUID.nameUUIDFromBytes(seed.toByteArray())

@Preview(showBackground = true, name = "CourseCard Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "CourseCard Dark")
@Composable
private fun CourseCardPreview() {
    ELearningTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CourseCard(
                courseTitle = "Module 1 : Les bases",
                courseIndex = 0,
                seances = previewSeances,
                initiallyExpanded = true,
                onSeanceClick = {}
            )
        }
    }
}
