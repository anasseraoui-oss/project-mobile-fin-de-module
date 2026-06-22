package com.elearning.app.presentation.instructor

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.model.PedagogicalResource
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.model.Seance
import com.elearning.app.domain.repository.SeanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class SeanceEditorViewModel @Inject constructor(
    private val repository: SeanceRepository,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val seanceId: String = checkNotNull(savedStateHandle["seanceId"])

    private val _state = MutableStateFlow(SeanceEditorState(seanceId = seanceId))
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val seance = repository.getSeanceById(seanceId)
                val resources = repository.getResources(seanceId)
                seance to resources
            }.onSuccess { (seance, resources) ->
                _state.update {
                    it.copy(
                        loading = false,
                        seance = seance,
                        resources = resources,
                        textContent = seance.description.orEmpty(),
                        title = seance.title,
                        durationMinutes = (seance.durationSeconds / 60).coerceAtLeast(0).toString(),
                        type = seance.type.name,
                        orderIndex = seance.orderIndex.toString(),
                        status = seance.status.name
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(loading = false, error = error.localizedMessage ?: "Impossible de charger la seance")
                }
            }
        }
    }

    fun updateTextContent(value: String) {
        _state.update { it.copy(textContent = value, saved = false) }
    }
    fun updateTitle(value: String) = _state.update { it.copy(title = value, saved = false) }
    fun updateDuration(value: String) = _state.update { it.copy(durationMinutes = value, saved = false) }
    fun updateType(value: String) = _state.update { it.copy(type = value, saved = false) }
    fun updateOrderIndex(value: String) = _state.update { it.copy(orderIndex = value, saved = false) }
    fun updateStatus(value: String) = _state.update { it.copy(status = value, saved = false) }
    fun clearOpenUrl() = _state.update { it.copy(openUrl = null) }

    fun saveDetails() {
        val current = state.value
        val backendType = if (current.type == "LIVE") "LIVE" else "ENREGISTREE"
        val durationSeconds = current.durationMinutes.toIntOrNull()?.let { it * 60 }
        val order = current.orderIndex.toIntOrNull() ?: current.seance?.orderIndex ?: 0
        viewModelScope.launch {
            _state.update { it.copy(detailsSaving = true, error = null, saved = false) }
            when (val result = repository.updateSeance(
                seanceId = seanceId,
                title = current.title.trim(),
                description = current.textContent,
                type = backendType,
                durationSeconds = durationSeconds,
                orderIndex = order,
                status = current.status
            )) {
                is Result.Success -> _state.update {
                    it.copy(detailsSaving = false, saved = true, seance = result.data)
                }
                is Result.Error -> _state.update {
                    it.copy(detailsSaving = false, error = result.message ?: "Sauvegarde seance indisponible")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun saveTextContent() {
        viewModelScope.launch {
            _state.update { it.copy(textSaving = true, error = null, saved = false) }
            when (val result = repository.updateTextContent(seanceId, state.value.textContent)) {
                is Result.Success -> _state.update { it.copy(textSaving = false, saved = true) }
                is Result.Error -> _state.update {
                    it.copy(textSaving = false, error = result.message ?: "Endpoint texte indisponible")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun uploadVideo(uriString: String) {
        val part = multipartFromUri(uriString, "video") ?: run {
            _state.update { it.copy(error = "Impossible de lire cette video") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(videoUploading = true, error = null) }
            when (val result = repository.uploadVideo(seanceId, part)) {
                is Result.Success -> {
                    _state.update { it.copy(videoUploading = false, saved = true) }
                    refresh()
                }
                is Result.Error -> _state.update {
                    it.copy(videoUploading = false, error = result.message ?: "Upload video refuse par le backend")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun deleteVideo() {
        viewModelScope.launch {
            _state.update { it.copy(videoUploading = true, error = null) }
            when (val result = repository.deleteVideo(seanceId)) {
                is Result.Success -> {
                    _state.update { it.copy(videoUploading = false, saved = true) }
                    refresh()
                }
                is Result.Error -> _state.update {
                    it.copy(videoUploading = false, error = result.message ?: "Suppression video indisponible")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun uploadResource(uriString: String) {
        val uri = Uri.parse(uriString)
        val title = displayName(uri) ?: "Ressource"
        val part = multipartFromUri(uriString, "file") ?: run {
            _state.update { it.copy(error = "Impossible de lire ce fichier") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(resourceUploading = true, error = null) }
            when (val result = repository.uploadResource(seanceId, title, part)) {
                is Result.Success -> _state.update {
                    it.copy(
                        resourceUploading = false,
                        saved = true,
                        resources = it.resources + result.data
                    )
                }
                is Result.Error -> _state.update {
                    it.copy(resourceUploading = false, error = result.message ?: "Upload ressource indisponible")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun replaceResource(resourceId: String, uriString: String) {
        val uri = Uri.parse(uriString)
        val title = displayName(uri) ?: "Ressource"
        val part = multipartFromUri(uriString, "file") ?: run {
            _state.update { it.copy(error = "Impossible de lire ce fichier") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(resourceUploading = true, error = null) }
            when (val result = repository.replaceResource(resourceId, title, part)) {
                is Result.Success -> _state.update {
                    it.copy(
                        resourceUploading = false,
                        saved = true,
                        resources = it.resources.map { resource ->
                            if (resource.id.toString() == resourceId) result.data else resource
                        }
                    )
                }
                is Result.Error -> _state.update {
                    it.copy(resourceUploading = false, error = result.message ?: "Remplacement ressource indisponible")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun openResource(resourceId: String) {
        viewModelScope.launch {
            runCatching { repository.getResourceDownloadUrl(resourceId) }
                .onSuccess { url -> _state.update { it.copy(openUrl = url) } }
                .onFailure { error -> _state.update { it.copy(error = error.localizedMessage ?: "Ressource indisponible") } }
        }
    }

    fun deleteResource(resourceId: String) {
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            when (val result = repository.deleteResource(resourceId)) {
                is Result.Success -> _state.update {
                    it.copy(resources = it.resources.filterNot { resource -> resource.id.toString() == resourceId })
                }
                is Result.Error -> _state.update {
                    it.copy(error = result.message ?: "Suppression ressource indisponible")
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun multipartFromUri(uriString: String, partName: String): MultipartBody.Part? {
        val uri = Uri.parse(uriString)
        val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val fileName = displayName(uri) ?: "$partName.bin"
        val mimeType = appContext.contentResolver.getType(uri) ?: "application/octet-stream"
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, fileName, body)
    }

    private fun displayName(uri: Uri): String? {
        return appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
    }
}

data class SeanceEditorState(
    val seanceId: String,
    val seance: Seance? = null,
    val resources: List<PedagogicalResource> = emptyList(),
    val textContent: String = "",
    val title: String = "",
    val durationMinutes: String = "",
    val type: String = "VIDEO",
    val orderIndex: String = "0",
    val status: String = "PLANIFIEE",
    val loading: Boolean = false,
    val detailsSaving: Boolean = false,
    val textSaving: Boolean = false,
    val videoUploading: Boolean = false,
    val resourceUploading: Boolean = false,
    val saved: Boolean = false,
    val openUrl: String? = null,
    val error: String? = null
)
