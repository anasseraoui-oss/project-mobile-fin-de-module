package com.elearning.app.presentation.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.model.PedagogicalResource
import com.elearning.app.domain.model.Seance
import com.elearning.app.domain.repository.SeanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeancePlayerViewModel @Inject constructor(
    private val repository: SeanceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val seanceId: String = checkNotNull(savedStateHandle["seanceId"])

    private val _uiState = MutableStateFlow<SeancePlayerState>(SeancePlayerState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadSeance()
    }

    fun loadSeance() {
        viewModelScope.launch {
            _uiState.value = SeancePlayerState.Loading
            try {
                // 1. Get Seance details for initial progress
                val seance = repository.getSeanceById(seanceId)
                
                // 2. Offline First Strategy: Check local cache
                val localPath = repository.getLocalVideoPath(seanceId)
                
                // 3. Get remote presigned URL if no local cache
                val streamUrl = localPath ?: repository.getStreamUrl(seanceId)
                val resources = runCatching { repository.getResources(seanceId) }.getOrDefault(emptyList())

                _uiState.value = SeancePlayerState.Ready(
                    seance = seance,
                    streamUrl = streamUrl,
                    resources = resources,
                    lastProgressMs = seance.progressSeconds * 1000L
                )
            } catch (e: Exception) {
                _uiState.value = SeancePlayerState.Error(e.localizedMessage ?: "Erreur de chargement de la vidéo")
            }
        }
    }

    fun updateProgress(progressMs: Long) {
        viewModelScope.launch {
            try {
                val progressSeconds = (progressMs / 1000).toInt()
                repository.saveProgress(seanceId, progressSeconds)
            } catch (e: Exception) {
                // Silent failure - do not interrupt playback for background tracking errors
            }
        }
    }

    fun resolveResourceUrl(resource: PedagogicalResource, onResolved: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val url = resource.fileUrl ?: repository.getResourceDownloadUrl(resource.id.toString())
                onResolved(url)
            } catch (_: Exception) {
                // Download errors should not interrupt video playback.
            }
        }
    }
}

sealed class SeancePlayerState {
    object Loading : SeancePlayerState()
    data class Ready(
        val seance: Seance,
        val streamUrl: String,
        val resources: List<PedagogicalResource>,
        val lastProgressMs: Long
    ) : SeancePlayerState()
    data class Error(val message: String) : SeancePlayerState()
}
