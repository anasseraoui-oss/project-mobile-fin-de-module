package com.elearning.app.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.FormationLevel
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.Elevation
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing
import java.util.Locale
import java.util.UUID

@Composable
fun FormationCard(
    formation: Formation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ELearningTheme.extended

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(Elevation.medium, Radius.lg)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "Formation ${formation.title}, ${formation.organisation}"
            },
        shape = Radius.lg,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // managed by shadow
    ) {
        Column {
            // ── Thumbnail with overlays ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                SubcomposeAsyncImage(
                    model = formation.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        ShimmerBox(modifier = Modifier.fillMaxSize())
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                )

                // Gradient scrim at bottom for text contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                                startY = 60f
                            )
                        )
                )

                // Level badge (top-left)
                LevelBadge(
                    level = formation.level,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Spacing.sm)
                )

                // Enrolled badge (top-right)
                if (formation.isEnrolled) {
                    EnrolledBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Spacing.sm)
                    )
                }

                // Duration badge (bottom-right)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(Spacing.sm),
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = Radius.xs
                ) {
                    Text(
                        text = "${formation.durationHours}h",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.xxs)
                    )
                }
            }

            // ── Card Body ──────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                // Organisation
                Text(
                    text = formation.organisation.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Title
                Text(
                    text = formation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(Spacing.xxs))

                // Rating row
                if (formation.rating > 0f) {
                    RatingRow(
                        rating = formation.rating,
                        enrollmentCount = formation.enrollmentCount
                    )
                }

                // Progress bar (enrolled) or price (not enrolled)
                if (formation.isEnrolled) {
                    Spacer(Modifier.height(Spacing.xs))
                    ElearningProgressBar(
                        progress = formation.progressPercent / 100f,
                        label = "${formation.progressPercent}% complété",
                        showLabel = true
                    )
                } else {
                    Spacer(Modifier.height(Spacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (formation.price == 0.0) "Gratuit"
                            else "${formation.price.toInt()} ${formation.currency}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (formation.price == 0.0)
                                extendedColors.success
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${formation.courseCount} cours",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ─── Level Badge ──────────────────────────────────────────────────────────────

@Composable
fun LevelBadge(level: FormationLevel, modifier: Modifier = Modifier) {
    val extendedColors = ELearningTheme.extended
    val (text, color) = when (level) {
        FormationLevel.DEBUTANT      -> "Débutant" to extendedColors.levelBeginner
        FormationLevel.INTERMEDIAIRE -> "Intermédiaire" to extendedColors.levelIntermediate
        FormationLevel.AVANCE        -> "Avancé" to extendedColors.levelAdvanced
    }
    Surface(
        modifier = modifier,
        color = color,
        shape = Radius.xs
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
        )
    }
}

// ─── Enrolled Badge ───────────────────────────────────────────────────────────

@Composable
fun EnrolledBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        shape = Radius.xs
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = "Inscrit",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── Rating Row ───────────────────────────────────────────────────────────────

@Composable
fun RatingRow(rating: Float, enrollmentCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = String.format(Locale.getDefault(), "%.1f", rating),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "(${formatCount(enrollmentCount)})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> "${count / 1_000_000}M"
    count >= 1_000     -> "${count / 1_000}k"
    else               -> count.toString()
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewFormation = Formation(
    id = previewUuid("formation-card"),
    title = "Masterclass Architecture Android",
    description = "Clean Architecture avec Jetpack Compose",
    organisation = "Tech Academy",
    thumbnailUrl = null,
    level = FormationLevel.INTERMEDIAIRE,
    language = "Français",
    durationHours = 12,
    courseCount = 8,
    rating = 4.8f,
    enrollmentCount = 1500,
    price = 49.0,
    currency = "USD",
    isEnrolled = true,
    progressPercent = 45
)

private fun previewUuid(seed: String): UUID = UUID.nameUUIDFromBytes(seed.toByteArray())

@Preview(showBackground = true, name = "FormationCard Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "FormationCard Dark")
@Composable
private fun FormationCardPreview() {
    ELearningTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FormationCard(
                formation = previewFormation,
                onClick = {}
            )
        }
    }
}
