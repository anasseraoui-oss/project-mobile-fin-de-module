package com.elearning.app.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elearning.app.domain.model.AppNotification
import com.elearning.app.domain.model.NotificationType
import com.elearning.app.presentation.components.EmptyState
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.ELearningColors
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.FigmaSpacing
import com.elearning.app.presentation.theme.Radius

@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNotificationClick: (String?) -> Unit,
    modifier: Modifier = Modifier,
    uiState: NotificationsUiState? = null,
    viewModel: NotificationsViewModel? = null
) {
    if (uiState != null) {
        NotificationsScreenState(uiState, onNavigateBack, onNotificationClick, modifier)
        return
    }

    val resolvedViewModel = viewModel ?: hiltViewModel()
    val observedUiState by resolvedViewModel.uiState.collectAsStateWithLifecycle()
    NotificationsScreenState(observedUiState, onNavigateBack, onNotificationClick, modifier)
}

@Composable
private fun NotificationsScreenState(
    uiState: NotificationsUiState,
    onNavigateBack: () -> Unit,
    onNotificationClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ELearningColors.AppBackground)
    ) {
        NotificationsHeader(onNavigateBack = onNavigateBack)
        when (val state = uiState) {
            NotificationsUiState.Loading -> FullScreenLoader()
            is NotificationsUiState.Error -> ErrorState(message = state.message, onRetry = state.onRetry)
            NotificationsUiState.Empty -> EmptyState(
                message = "Aucune notification pour le moment.",
                icon = Icons.Default.NotificationsOff
            )
            is NotificationsUiState.Content -> NotificationsContent(
                notifications = state.notifications,
                onNotificationClick = onNotificationClick
            )
        }
    }
}

@Composable
private fun NotificationsHeader(onNavigateBack: () -> Unit) {
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
                text = "Notifications",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = ELearningColors.TextPrimary
            )
            Text(
                text = "Restez a jour sur vos formations",
                style = MaterialTheme.typography.bodyMedium,
                color = ELearningColors.TextTertiary
            )
        }
    }
}

@Composable
private fun NotificationsContent(
    notifications: List<AppNotification>,
    onNotificationClick: (String?) -> Unit
) {
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
        if (notifications.isEmpty()) {
            item {
                EmptyState(
                    message = "Aucune notification pour le moment.",
                    icon = Icons.Default.NotificationsOff,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(420.dp)
                )
            }
        } else {
            items(notifications) { notification ->
                NotificationRow(
                    notification = notification,
                    onClick = { onNotificationClick(notification.deepLink) }
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: AppNotification,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Radius.xl,
        color = ELearningColors.CardSurface,
        shadowElevation = if (notification.isRead) 1.dp else 3.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = notification.type.containerColor(),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = notification.type.icon(),
                        contentDescription = null,
                        tint = notification.type.iconColor(),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ELearningColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Badge(containerColor = ELearningColors.BrandBlue)
                    }
                }
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ELearningColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notification.createdAt,
                    style = MaterialTheme.typography.labelMedium,
                    color = ELearningColors.TextTertiary
                )
            }
        }
    }
}

sealed class NotificationsUiState {
    data object Loading : NotificationsUiState()
    data object Empty : NotificationsUiState()
    data class Error(val message: String, val onRetry: () -> Unit = {}) : NotificationsUiState()
    data class Content(val notifications: List<AppNotification>) : NotificationsUiState()
}

private fun NotificationType.icon() = when (this) {
    NotificationType.COURS -> Icons.Default.PlayCircle
    NotificationType.QUIZ -> Icons.Default.Quiz
    NotificationType.CERTIFICAT -> Icons.Default.EmojiEvents
    NotificationType.SYSTEM -> Icons.Default.Notifications
}

private fun NotificationType.containerColor() = when (this) {
    NotificationType.COURS -> Color(0xFFEFF6FF)
    NotificationType.QUIZ -> Color(0xFFFFF7ED)
    NotificationType.CERTIFICAT -> Color(0xFFFEF3C7)
    NotificationType.SYSTEM -> Color(0xFFF3F4F6)
}

private fun NotificationType.iconColor() = when (this) {
    NotificationType.COURS -> ELearningColors.BrandBlue
    NotificationType.QUIZ -> Color(0xFFEA580C)
    NotificationType.CERTIFICAT -> Color(0xFFD97706)
    NotificationType.SYSTEM -> ELearningColors.TextSecondary
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    ELearningTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ELearningColors.AppBackground)
        ) {
            NotificationsHeader(onNavigateBack = {})
            NotificationsContent(notifications = emptyList(), onNotificationClick = {})
        }
    }
}
