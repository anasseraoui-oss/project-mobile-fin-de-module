package com.elearning.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.Spacing

/**
 * SyncIndicator — A banner to show offline or syncing status.
 *
 * @param isOffline true if the app is currently disconnected
 * @param isSyncing true if the app is syncing data in background
 */
@Composable
fun SyncIndicator(
    isOffline: Boolean,
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    val extendedColors = ELearningTheme.extended

    AnimatedVisibility(
        visible = isOffline || isSyncing,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val bgColor = if (isOffline) extendedColors.offlineBanner else MaterialTheme.colorScheme.primaryContainer
        val contentColor = if (isOffline) extendedColors.onOfflineBanner else MaterialTheme.colorScheme.onPrimaryContainer
        val icon = if (isOffline) Icons.Default.CloudOff else Icons.Default.Sync
        val message = if (isOffline) "Mode hors ligne" else "Synchronisation en cours..."

        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = message,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
