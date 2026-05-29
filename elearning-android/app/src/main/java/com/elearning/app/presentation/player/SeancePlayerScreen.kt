package com.elearning.app.presentation.player

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.elearning.app.domain.model.PedagogicalResource
import com.elearning.app.domain.model.Seance
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.ELearningColors
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.FigmaSpacing
import com.elearning.app.presentation.theme.Radius
import kotlinx.coroutines.delay

@Composable
fun SeancePlayerScreen(
    onNavigateBack: () -> Unit,
    viewModel: SeancePlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val headerTitle = (uiState as? SeancePlayerState.Ready)?.seance?.title ?: "Lecteur"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ELearningColors.CardSurface,
        topBar = {
            PlayerHeader(
                title = headerTitle,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is SeancePlayerState.Loading -> FullScreenLoader(modifier = Modifier.padding(paddingValues))
            is SeancePlayerState.Error -> ErrorState(
                message = (uiState as SeancePlayerState.Error).message,
                onRetry = viewModel::loadSeance,
                modifier = Modifier.padding(paddingValues)
            )
            is SeancePlayerState.Ready -> {
                val state = uiState as SeancePlayerState.Ready
                val uriHandler = LocalUriHandler.current
                PlayerReadyContent(
                    seance = state.seance,
                    streamUrl = state.streamUrl,
                    resources = state.resources,
                    startPositionMs = state.lastProgressMs,
                    onProgressUpdate = viewModel::updateProgress,
                    onResourceClick = { resource ->
                        viewModel.resolveResourceUrl(resource) { uriHandler.openUri(it) }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun PlayerHeader(title: String, onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = ELearningColors.TextPrimary)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ELearningColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = ELearningColors.TextSecondary)
        }
    }
}

@Composable
private fun PlayerReadyContent(
    seance: Seance,
    streamUrl: String,
    resources: List<PedagogicalResource>,
    startPositionMs: Long,
    onProgressUpdate: (Long) -> Unit,
    onResourceClick: (PedagogicalResource) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(PlayerTab.Lessons) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ELearningColors.CardSurface)
    ) {
        VideoPlayerContent(
            streamUrl = streamUrl,
            startPositionMs = startPositionMs,
            onProgressUpdate = onProgressUpdate,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FigmaSpacing.pageHorizontal, vertical = 20.dp)
        ) {
            Text(
                text = seance.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ELearningColors.TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                PlayerMeta(Icons.Default.Schedule, seance.durationLabel(), ELearningColors.TextTertiary)
                PlayerMeta(Icons.Default.CheckCircle, seance.progressLabel(), ELearningColors.BrandBlue)
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                PlayerTab.entries.forEach { tab ->
                    PlayerTabButton(
                        tab = tab,
                        active = activeTab == tab,
                        onClick = { activeTab = tab }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFC))
        ) {
            when (activeTab) {
                PlayerTab.Lessons -> LessonsTab()
                PlayerTab.Files -> FilesTab(resources = resources, onResourceClick = onResourceClick)
                PlayerTab.Discussions -> DiscussionsTab()
            }
        }
    }
}

@Composable
fun VideoPlayerContent(
    streamUrl: String,
    startPositionMs: Long,
    onProgressUpdate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val exoPlayer = remember(streamUrl) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(streamUrl)
            setMediaItem(mediaItem)
            seekTo(startPositionMs)
            prepare()
            playWhenReady = false
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(10_000)
            if (exoPlayer.isPlaying) {
                onProgressUpdate(exoPlayer.currentPosition)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onProgressUpdate(exoPlayer.currentPosition)
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
                keepScreenOn = true
            }
        },
        modifier = modifier
    )
}

@Composable
private fun LessonsTab() {
    PlayerEmptyState(
        icon = Icons.Default.List,
        title = "Curriculum indisponible.",
        description = "Les lecons associees seront affichees ici des que l'API curriculum sera connectee."
    )
}

@Composable
private fun FilesTab(
    resources: List<PedagogicalResource>,
    onResourceClick: (PedagogicalResource) -> Unit
) {
    if (resources.isEmpty()) {
        PlayerEmptyState(
            icon = Icons.Default.Download,
            title = "Aucun fichier pour cette lecon.",
            description = "Les supports seront affiches ici des que l'API les expose."
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(FigmaSpacing.pageHorizontal),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(resources, key = { it.id }) { resource ->
            ResourceRow(resource = resource, onClick = { onResourceClick(resource) })
        }
    }
}

@Composable
private fun ResourceRow(resource: PedagogicalResource, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = resource.isDownloadable, onClick = onClick),
        shape = Radius.md,
        color = Color.White,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = Color(0xFFEFF6FF), modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = ELearningColors.BrandBlue)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ELearningColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${resource.mimeType} - ${resource.sizeBytes.formatBytes()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ELearningColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.Download, contentDescription = "Telecharger", tint = ELearningColors.TextSecondary)
        }
    }
}

@Composable
private fun DiscussionsTab() {
    PlayerEmptyState(
        icon = Icons.Default.MoreHoriz,
        title = "Aucune question pour cette lecon.",
        description = "Les discussions seront disponibles quand le module Q&A sera connecte.",
        actionLabel = "Poser une question"
    )
}

@Composable
private fun PlayerEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(FigmaSpacing.pageHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = Color(0xFFF3F4F6), modifier = Modifier.size(64.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFF9CA3AF))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ELearningColors.TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = ELearningColors.TextTertiary)
        if (actionLabel != null) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onAction,
                shape = Radius.md,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = ELearningColors.BrandBlue)
            ) {
                Text(actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlayerTabButton(tab: PlayerTab, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = tab.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (active) ELearningColors.BrandBlue else Color(0xFF9CA3AF)
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(36.dp)
                .clip(CircleShape)
                .background(if (active) ELearningColors.BrandBlue else Color.Transparent)
        )
    }
}

@Composable
private fun PlayerMeta(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = color)
    }
}

private enum class PlayerTab(val label: String) {
    Lessons("Lecons"),
    Files("Fichiers"),
    Discussions("Q&A")
}

private fun Seance.durationLabel(): String {
    val minutes = (durationSeconds / 60).coerceAtLeast(1)
    return "$minutes min"
}

private fun Seance.progressLabel(): String {
    if (durationSeconds <= 0) return "0% complete"
    val percent = (progressSeconds * 100 / durationSeconds).coerceIn(0, 100)
    return "$percent% complete"
}

private fun Long.formatBytes(): String {
    if (this <= 0L) return "taille inconnue"
    val kb = this / 1024.0
    if (kb < 1024) return "${kb.toInt()} Ko"
    return "${(kb / 1024).toInt()} Mo"
}

@Preview(showBackground = true)
@Composable
private fun PlayerHeaderPreview() {
    ELearningTheme {
        PlayerHeader(title = "React - Les fondamentaux", onNavigateBack = {})
    }
}
