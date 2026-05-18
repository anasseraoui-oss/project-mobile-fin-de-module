package com.elearning.app.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.Spacing
import kotlinx.coroutines.delay

@Composable
fun SeancePlayerScreen(
    onNavigateBack: () -> Unit,
    viewModel: SeancePlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is SeancePlayerState.Loading -> FullScreenLoader()
                is SeancePlayerState.Error -> ErrorState(
                    message = (uiState as SeancePlayerState.Error).message,
                    onRetry = viewModel::loadSeance
                )
                is SeancePlayerState.Ready -> {
                    val state = uiState as SeancePlayerState.Ready
                    VideoPlayerContent(
                        streamUrl = state.streamUrl,
                        startPositionMs = state.lastProgressMs,
                        onProgressUpdate = viewModel::updateProgress
                    )
                }
            }

            // Overlay back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(Spacing.sm)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
        }
    }
}

@Composable
fun VideoPlayerContent(
    streamUrl: String,
    startPositionMs: Long,
    onProgressUpdate: (Long) -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(streamUrl)
            setMediaItem(mediaItem)
            seekTo(startPositionMs)
            prepare()
            playWhenReady = true
        }
    }

    // Background sync loop for tracking progress every 10 seconds
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
            // Save one last time when exiting
            onProgressUpdate(exoPlayer.currentPosition)
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
                keepScreenOn = true // Prevent screen from turning off
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}
