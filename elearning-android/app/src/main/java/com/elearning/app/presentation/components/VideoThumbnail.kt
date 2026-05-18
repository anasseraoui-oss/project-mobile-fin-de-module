package com.elearning.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing

/**
 * VideoThumbnail — Thumbnail with play button overlay and metadata badges.
 *
 * Shows:
 * - Coil-loaded thumbnail (with shimmer loading and gradient error fallback)
 * - Centered translucent play button
 * - Duration badge (bottom-right)
 * - Offline indicator (top-left, shown when [isOfflineAvailable] = true)
 * - "LIVE" badge when [isLive] = true
 *
 * Edge cases:
 *  - thumbnailUrl null/blank → gradient placeholder with video icon
 *  - durationSeconds = 0 → badge hidden
 */
@Composable
fun VideoThumbnail(
    thumbnailUrl: String?,
    durationSeconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOfflineAvailable: Boolean = false,
    isLive: Boolean = false,
    cornerRadius: Dp = 12.dp
) {
    val extendedColors = ELearningTheme.extended

    Box(
        modifier = modifier
            .clip(Radius.md)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // ── Thumbnail ──────────────────────────────────────────────────────
        SubcomposeAsyncImage(
            model = thumbnailUrl,
            contentDescription = "Miniature vidéo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        )

        // ── Dark scrim ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )

        // ── Play Button ────────────────────────────────────────────────────
        if (!isLive) {
            Surface(
                shape = Radius.full,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Lire la vidéo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // ── LIVE badge ─────────────────────────────────────────────────────
        if (isLive) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.error,
                shape = Radius.xs
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    // Pulsing red dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.White, shape = Radius.full)
                    )
                    Text(
                        "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // ── Offline Available badge (top-left) ─────────────────────────────
        if (isOfflineAvailable) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Spacing.sm),
                color = extendedColors.success.copy(alpha = 0.9f),
                shape = Radius.xs
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    Icon(
                        Icons.Default.OfflinePin,
                        contentDescription = "Disponible hors ligne",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        "Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ── Duration badge (bottom-right) ─────────────────────────────────
        if (durationSeconds > 0 && !isLive) {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            val durationText = if (minutes > 0) "${minutes}:${seconds.toString().padStart(2, '0')}"
            else "0:${seconds.toString().padStart(2, '0')}"

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.sm),
                color = Color.Black.copy(alpha = 0.7f),
                shape = Radius.xs
            ) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.xxs)
                )
            }
        }
    }
}
