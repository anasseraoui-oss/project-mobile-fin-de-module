package com.elearning.app.presentation.instructor

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elearning.app.domain.model.PedagogicalResource
import com.elearning.app.domain.model.Seance
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing
import java.util.Locale

private val SeanceBlue = Color(0xFF3F51B5)

@Composable
fun SeanceEditorRoute(
    onNavigateBack: () -> Unit,
    viewModel: SeanceEditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val initialError = state.error

    when {
        state.loading && state.seance == null -> FullScreenLoader(Modifier.background(Color(0xFFF5F5F5)))
        initialError != null && state.seance == null -> ErrorState(
            message = initialError,
            onRetry = viewModel::refresh,
            modifier = Modifier.background(Color(0xFFF5F5F5))
        )
        else -> SeanceEditorScreen(
            state = state,
            onBack = onNavigateBack,
            onTextChange = viewModel::updateTextContent,
            onTitleChange = viewModel::updateTitle,
            onDurationChange = viewModel::updateDuration,
            onTypeChange = viewModel::updateType,
            onOrderChange = viewModel::updateOrderIndex,
            onStatusChange = viewModel::updateStatus,
            onSaveDetails = viewModel::saveDetails,
            onSaveText = viewModel::saveTextContent,
            onUploadVideo = viewModel::uploadVideo,
            onDeleteVideo = viewModel::deleteVideo,
            onUploadResource = viewModel::uploadResource,
            onReplaceResource = viewModel::replaceResource,
            onOpenResource = viewModel::openResource,
            onOpenUrlConsumed = viewModel::clearOpenUrl,
            onDeleteResource = viewModel::deleteResource
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeanceEditorScreen(
    state: SeanceEditorState,
    onBack: () -> Unit,
    onTextChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onOrderChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSaveDetails: () -> Unit,
    onSaveText: () -> Unit,
    onUploadVideo: (String) -> Unit,
    onDeleteVideo: () -> Unit,
    onUploadResource: (String) -> Unit,
    onReplaceResource: (String, String) -> Unit,
    onOpenResource: (String) -> Unit,
    onOpenUrlConsumed: () -> Unit,
    onDeleteResource: (String) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var replaceResourceId by remember { mutableStateOf<String?>(null) }
    val videoPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let { onUploadVideo(it.toString()) }
    }
    val filePicker = rememberLauncherForActivityResult(GetContent()) { uri ->
        uri?.let { onUploadResource(it.toString()) }
    }
    val replaceFilePicker = rememberLauncherForActivityResult(GetContent()) { uri ->
        val target = replaceResourceId
        if (uri != null && target != null) onReplaceResource(target, uri.toString())
        replaceResourceId = null
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) snackbarHostState.showSnackbar("Modifications sauvegardees")
    }
    LaunchedEffect(state.openUrl) {
        state.openUrl?.let { url ->
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.onFailure {
                snackbarHostState.showSnackbar("Impossible d'ouvrir la ressource")
            }
            onOpenUrlConsumed()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Editeur de seance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item {
                state.seance?.let { SeanceSummaryCard(it) }
            }
            item {
                SeanceDetailsEditor(
                    state = state,
                    onTitleChange = onTitleChange,
                    onDurationChange = onDurationChange,
                    onTypeChange = onTypeChange,
                    onOrderChange = onOrderChange,
                    onStatusChange = onStatusChange,
                    onSave = onSaveDetails
                )
            }
            item {
                VideoUploadPanel(
                    seance = state.seance,
                    uploading = state.videoUploading,
                    onUpload = { videoPicker.launch(PickVisualMediaRequest(PickVisualMedia.VideoOnly)) },
                    onDelete = onDeleteVideo
                )
            }
            item {
                TextContentEditor(
                    content = state.textContent,
                    saving = state.textSaving,
                    onChange = onTextChange,
                    onSave = onSaveText
                )
            }
            item {
                ResourceUploadButton(
                    uploading = state.resourceUploading,
                    onClick = { filePicker.launch("*/*") }
                )
            }
            item {
                Text("Ressources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (state.resources.isEmpty()) {
                item {
                    EmptyResourceCard()
                }
            } else {
                items(state.resources, key = { it.id }) { resource ->
                    AttachmentListItem(
                        resource = resource,
                        onOpen = {
                            val directUrl = resource.fileUrl
                            if (!directUrl.isNullOrBlank()) {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(directUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            } else {
                                onOpenResource(resource.id.toString())
                            }
                        },
                        onReplace = {
                            replaceResourceId = resource.id.toString()
                            replaceFilePicker.launch("*/*")
                        },
                        onDelete = { onDeleteResource(resource.id.toString()) }
                    )
                }
            }
            item { Spacer(Modifier.height(Spacing.xxl)) }
        }
    }
}

@Composable
private fun SeanceDetailsEditor(
    state: SeanceEditorState,
    onTitleChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onOrderChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(shape = Radius.lg, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Informations", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Titre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(
                    value = state.durationMinutes,
                    onValueChange = onDurationChange,
                    label = { Text("Minutes") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.orderIndex,
                    onValueChange = onOrderChange,
                    label = { Text("Ordre") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf("VIDEO", "DOCUMENT", "QUIZ", "LIVE").forEach { type ->
                    androidx.compose.material3.FilterChip(
                        selected = state.type == type,
                        onClick = { onTypeChange(type) },
                        label = { Text(type) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf("PLANIFIEE", "EN_COURS", "TERMINEE").forEach { status ->
                    androidx.compose.material3.FilterChip(
                        selected = state.status == status,
                        onClick = { onStatusChange(status) },
                        label = { Text(status) }
                    )
                }
            }
            Button(
                onClick = onSave,
                enabled = !state.detailsSaving && state.title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SeanceBlue),
                shape = Radius.md,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(Spacing.sm))
                Text(if (state.detailsSaving) "Sauvegarde..." else "Sauvegarder les informations")
            }
        }
    }
}

@Composable
private fun SeanceSummaryCard(seance: Seance) {
    Card(shape = Radius.lg, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(seance.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text("${seance.type.name} - ${seance.status.name} - ${seance.durationSeconds / 60} min", color = Color.Gray)
        }
    }
}

@Composable
private fun VideoUploadPanel(seance: Seance?, uploading: Boolean, onUpload: () -> Unit, onDelete: () -> Unit) {
    Card(shape = Radius.lg, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoFile, contentDescription = null, tint = SeanceBlue)
                Spacer(Modifier.width(Spacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Video de la lecon", fontWeight = FontWeight.Bold)
                    Text(
                        if (seance?.videoKey.isNullOrBlank()) "Aucune video attachee" else "Video disponible",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            if (uploading) LinearProgressIndicator(color = SeanceBlue, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = onUpload,
                enabled = !uploading,
                colors = ButtonDefaults.buttonColors(containerColor = SeanceBlue),
                shape = Radius.md,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uploading) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(Modifier.width(Spacing.sm))
                Text("Remplacer la video")
            }
            if (!seance?.videoKey.isNullOrBlank()) {
                Button(
                    onClick = onDelete,
                    enabled = !uploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = Radius.md,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(Spacing.sm))
                    Text("Supprimer la video")
                }
            }
            Text(
                "Le backend actuel peut refuser l'upload si la seance n'est pas TERMINEE.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun TextContentEditor(
    content: String,
    saving: Boolean,
    onChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(shape = Radius.lg, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = SeanceBlue)
                Spacer(Modifier.width(Spacing.sm))
                Text("Contenu texte / Markdown", fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
                value = content,
                onValueChange = onChange,
                minLines = 8,
                placeholder = { Text("Notes, resume, consignes ou contenu markdown...") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onSave,
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = SeanceBlue),
                shape = Radius.md,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(Spacing.sm))
                Text(if (saving) "Sauvegarde..." else "Sauvegarder le texte")
            }
        }
    }
}

@Composable
private fun ResourceUploadButton(uploading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !uploading,
        colors = ButtonDefaults.buttonColors(containerColor = SeanceBlue),
        shape = Radius.lg,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Icon(Icons.Default.AttachFile, contentDescription = null)
        Spacer(Modifier.width(Spacing.sm))
        Text(if (uploading) "Upload en cours..." else "Ajouter une ressource")
    }
}

@Composable
private fun AttachmentListItem(
    resource: PedagogicalResource,
    onOpen: () -> Unit,
    onReplace: () -> Unit,
    onDelete: () -> Unit
) {
    Card(shape = Radius.md, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = null, tint = SeanceBlue)
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(resource.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${resource.mimeType} - ${formatResourceSize(resource.sizeBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onOpen) {
                Icon(Icons.Default.OpenInNew, contentDescription = "Ouvrir", tint = SeanceBlue)
            }
            IconButton(onClick = onReplace) {
                Icon(Icons.Default.Upload, contentDescription = "Remplacer", tint = SeanceBlue)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
private fun EmptyResourceCard() {
    Surface(shape = Radius.lg, color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(Spacing.xl), contentAlignment = Alignment.Center) {
            Text("Aucune ressource ajoutee", color = Color.Gray)
        }
    }
}

private fun formatResourceSize(bytes: Long): String {
    val mb = bytes / 1024f / 1024f
    return if (mb >= 1f) String.format(Locale.getDefault(), "%.1f Mo", mb) else "${bytes / 1024} Ko"
}
