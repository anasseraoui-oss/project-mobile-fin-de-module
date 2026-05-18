package com.elearning.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing

/**
 * CertificateBadge — Award-style component displayed on the quiz ResultScreen.
 *
 * Features:
 *  - Shimmer sweep animation across the gold/amber card surface
 *  - Animated scale-in entrance
 *  - Formation name, learner name, score display
 *  - "Télécharger le PDF" CTA button
 *  - "Vérifier en ligne" secondary action
 *
 * Edge cases:
 *  - [certificateUrl] = null → disables download button, shows "Génération en cours..."
 *  - Very long formation name → max 2 lines with ellipsis
 */
@Composable
fun CertificateBadge(
    learnerName: String,
    formationTitle: String,
    score: Int,
    maxScore: Int,
    certificateUrl: String?,
    onDownload: () -> Unit,
    onVerify: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ELearningTheme.extended

    // ── Entrance scale animation ──────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cert_scale"
    )
    LaunchedEffect(Unit) { visible = true }

    // ── Shimmer sweep ─────────────────────────────────────────────────────
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.28f),
            Color.Transparent
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 300f, 300f)
    )

    // ── Gold gradient background ──────────────────────────────────────────
    val goldGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFB45309),   // dark amber
            Color(0xFFD97706),
            Color(0xFFF59E0B),   // main amber
            Color(0xFFFBBF24),   // light gold
            Color(0xFFD97706),
            Color(0xFFB45309)
        )
    )

    Box(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = Radius.xl,
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(goldGradient)
            ) {
                // Shimmer overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(shimmerBrush)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // Trophy icon
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = "Trophée",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )

                    Text(
                        text = "CERTIFICAT DE RÉUSSITE",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(
                            2f, androidx.compose.ui.unit.TextUnitType.Sp
                        )
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.4f))

                    Text(
                        text = learnerName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "a complété avec succès",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Text(
                        text = formationTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Score chip
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = Radius.full
                    ) {
                        Text(
                            text = "Score : $score / $maxScore",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.4f))

                    // ── Action Buttons ─────────────────────────────────────
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Download PDF
                        Button(
                            onClick = onDownload,
                            enabled = !certificateUrl.isNullOrBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = Radius.md,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFFD97706),
                                disabledContainerColor = Color.White.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                text = if (certificateUrl.isNullOrBlank())
                                    "Génération en cours…"
                                else
                                    "Télécharger le certificat PDF",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Verify online
                        OutlinedButton(
                            onClick = onVerify,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = Radius.md,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Vérifier en ligne")
                        }
                    }
                }
            }
        }
    }
}
