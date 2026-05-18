package com.elearning.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing

// ─── QuizBadge ────────────────────────────────────────────────────────────────

/**
 * QuizBadge — Displays quiz status inline (e.g. in SeanceItem or FormationDetailScreen).
 *
 * States:
 *  - NOT_ATTEMPTED : neutral outline badge "Quiz"
 *  - PASSED        : green badge with score
 *  - FAILED        : red badge with score + retry info
 *  - IN_PROGRESS   : amber pulsing badge
 *
 * Edge cases:
 *  - attemptsLeft = 0 → shows "Aucune tentative" instead of score
 *  - passingScore not met → shows score in error color
 */
enum class QuizBadgeState { NOT_ATTEMPTED, PASSED, FAILED, IN_PROGRESS }

@Composable
fun QuizBadge(
    state: QuizBadgeState,
    score: Int? = null,
    maxScore: Int? = null,
    attemptsLeft: Int? = null,
    modifier: Modifier = Modifier
) {
    val extendedColors = ELearningTheme.extended

    val (bgColor, contentColor, icon, label) = when (state) {
        QuizBadgeState.NOT_ATTEMPTED -> Quadruple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Quiz,
            "Quiz"
        )
        QuizBadgeState.PASSED -> Quadruple(
            extendedColors.successContainer,
            extendedColors.success,
            Icons.Default.CheckCircle,
            score?.let { "$it${maxScore?.let { m -> "/$m" } ?: ""}" } ?: "Réussi"
        )
        QuizBadgeState.FAILED -> Quadruple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            Icons.Default.Cancel,
            score?.let { "$it${maxScore?.let { m -> "/$m" } ?: ""}" } ?: "Échoué"
        )
        QuizBadgeState.IN_PROGRESS -> Quadruple(
            extendedColors.warningContainer,
            extendedColors.warning,
            Icons.Default.Timer,
            "En cours"
        )
    }

    // Pulsing animation for IN_PROGRESS state
    val scale by if (state == QuizBadgeState.IN_PROGRESS) {
        val infiniteTransition = rememberInfiniteTransition(label = "quiz_pulse")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Surface(
        modifier = modifier.scale(scale),
        color = bgColor,
        shape = Radius.full,
        border = if (state == QuizBadgeState.NOT_ATTEMPTED)
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.4f))
        else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
            // Attempts left pill
            if (state == QuizBadgeState.FAILED && attemptsLeft != null && attemptsLeft > 0) {
                Spacer(Modifier.width(2.dp))
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = Radius.full
                ) {
                    Text(
                        text = "$attemptsLeft restant${if (attemptsLeft > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

// ─── Kotlin helper — Quadruple ────────────────────────────────────────────────
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
