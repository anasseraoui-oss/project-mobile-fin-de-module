package com.elearning.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing

/**
 * ElearningProgressBar — animated linear progress with adaptive color thresholds.
 *
 * Color logic:
 *  - 0..33%   → Warning (amber)
 *  - 34..66%  → Secondary (cyan)
 *  - 67..100% → Success (green)
 *
 * Edge cases:
 *  - progress < 0f → clamped to 0
 *  - progress > 1f → clamped to 1
 *  - showLabel = false → no text rendered
 */
@Composable
fun ElearningProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    showLabel: Boolean = false,
    height: Dp = 6.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val extendedColors = ELearningTheme.extended
    val clampedProgress = progress.coerceIn(0f, 1f)

    val progressColor = when {
        clampedProgress < 0.34f -> extendedColors.warning
        clampedProgress < 0.67f -> MaterialTheme.colorScheme.secondary
        else                    -> extendedColors.success
    }

    // Animate progress changes
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )

    val semanticDesc = label ?: "${(clampedProgress * 100).toInt()}% de progression"

    Column(
        modifier = modifier.semantics { contentDescription = semanticDesc },
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
    ) {
        if (showLabel && label != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(clampedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = progressColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(Radius.full),
            color = progressColor,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * CircularProgressBadge — compact circular indicator used on quiz/certificate screens.
 *
 * @param progress 0f..1f
 * @param label    center text (score, %, etc.)
 */
@Composable
fun CircularProgressBadge(
    progress: Float,
    label: String,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val extendedColors = ELearningTheme.extended
    val clampedProgress = progress.coerceIn(0f, 1f)
    val color = when {
        clampedProgress < 0.5f -> MaterialTheme.colorScheme.error
        clampedProgress < 0.75f -> extendedColors.warning
        else -> extendedColors.success
    }
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "circular_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicatorCustom(
            progress = animatedProgress,
            color = color,
            strokeWidth = strokeWidth,
            size = size
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun CircularProgressIndicatorCustom(
    progress: Float,
    color: Color,
    strokeWidth: Dp,
    size: Dp
) {
    androidx.compose.material3.CircularProgressIndicator(
        progress = { progress },
        modifier = Modifier.size(size),
        color = color,
        strokeWidth = strokeWidth,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round
    )
}
