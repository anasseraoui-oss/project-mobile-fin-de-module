package com.elearning.app.presentation.instructor

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.elearning.app.domain.model.FormationCategory
import com.elearning.app.domain.model.FormationLevel
import com.elearning.app.presentation.theme.FigmaSpacing
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing
import java.util.Locale

private val EditorBlue = Color(0xFF3F51B5)
private val EditorSurface = Color(0xFFF5F5F5)

@Composable
fun FormationEditorRoute(
    onNavigateBack: () -> Unit,
    onEditSeanceContent: (String) -> Unit,
    viewModel: FormationEditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, state.formationId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshCurriculum()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    FormationEditorScreen(
        state = state,
        onBack = onNavigateBack,
        onStepSelected = viewModel::goToStep,
        onPreviousStep = viewModel::previousStep,
        onPrimaryAction = viewModel::primaryAction,
        onTitleChange = viewModel::updateTitle,
        onDescriptionChange = viewModel::updateDescription,
        onLanguageChange = viewModel::updateLanguage,
        onLevelChange = viewModel::updateLevel,
        onPriceChange = viewModel::updatePrice,
        onCertifiedChange = viewModel::updateCertified,
        onPrerequisiteInputChange = viewModel::updatePrerequisiteInput,
        onAddPrerequisite = viewModel::addPrerequisite,
        onRemovePrerequisite = viewModel::removePrerequisite,
        onCategorySelected = viewModel::selectCategory,
        onThumbnailPicked = viewModel::updateThumbnailUri,
        onPreviewVideoPicked = viewModel::updatePreviewVideoUri,
        onPreviewVideoUrlChange = viewModel::updatePreviewVideoUrl,
        onNewCourseTitleChange = viewModel::updateNewCourseTitle,
        onAddCourse = viewModel::addCourse,
        onOpenCourseEditor = viewModel::openCourseEditor,
        onDismissCourseEditor = viewModel::dismissCourseEditor,
        onEditCourseTitleChange = viewModel::updateEditCourseTitle,
        onEditCourseDescriptionChange = viewModel::updateEditCourseDescription,
        onSaveCourseEdit = viewModel::saveCourseEdit,
        onRequestDeleteCourse = viewModel::requestDeleteCourse,
        onConfirmDeleteCourse = viewModel::deleteCourse,
        onDismissDeleteCourse = viewModel::dismissDeleteCourseConfirm,
        onToggleCourse = viewModel::toggleCourse,
        onMoveCourse = viewModel::moveCourse,
        onOpenSeanceSheet = viewModel::openSeanceSheet,
        onOpenSeanceEditor = viewModel::openSeanceEditor,
        onDismissSeanceEditor = viewModel::dismissSeanceEditor,
        onEditSeanceTitleChange = viewModel::updateEditSeanceTitle,
        onEditSeanceDurationChange = viewModel::updateEditSeanceDuration,
        onEditSeanceTypeChange = viewModel::updateEditSeanceType,
        onEditSeanceStatusChange = viewModel::updateEditSeanceStatus,
        onSaveSeanceEdit = viewModel::saveSeanceEdit,
        onRequestDeleteSeance = viewModel::requestDeleteSeance,
        onConfirmDeleteSeance = viewModel::deleteSeance,
        onDismissDeleteSeance = viewModel::dismissDeleteSeanceConfirm,
        onEditSeanceContent = onEditSeanceContent,
        onDismissSeanceSheet = viewModel::dismissSeanceSheet,
        onNewSeanceTitleChange = viewModel::updateNewSeanceTitle,
        onNewSeanceDurationChange = viewModel::updateNewSeanceDuration,
        onNewSeanceTypeChange = viewModel::updateNewSeanceType,
        onAddSeance = viewModel::addSeance,
        onPriceModeChange = viewModel::updatePriceMode,
        onVisibilityChange = viewModel::updateVisibility,
        onAvailabilityModeChange = viewModel::updateAvailabilityMode,
        onAvailabilityDateChange = viewModel::updateAvailabilityDate,
        onPublishAnyway = { viewModel.publishFormation(forceWarnings = true) },
        onDismissPublishWarning = viewModel::dismissPublishWarning,
        onDismissPublishSuccess = viewModel::dismissPublishSuccess,
        onSave = { viewModel.saveDraft() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FormationEditorScreen(
    state: FormationEditorState,
    onBack: () -> Unit,
    onStepSelected: (Int) -> Unit,
    onPreviousStep: () -> Unit,
    onPrimaryAction: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onLevelChange: (FormationLevel) -> Unit,
    onPriceChange: (String) -> Unit,
    onCertifiedChange: (Boolean) -> Unit,
    onPrerequisiteInputChange: (String) -> Unit,
    onAddPrerequisite: () -> Unit,
    onRemovePrerequisite: (String) -> Unit,
    onCategorySelected: (FormationCategory) -> Unit,
    onThumbnailPicked: (String?) -> Unit,
    onPreviewVideoPicked: (String?) -> Unit,
    onPreviewVideoUrlChange: (String) -> Unit,
    onNewCourseTitleChange: (String) -> Unit,
    onAddCourse: () -> Unit,
    onOpenCourseEditor: (String) -> Unit,
    onDismissCourseEditor: () -> Unit,
    onEditCourseTitleChange: (String) -> Unit,
    onEditCourseDescriptionChange: (String) -> Unit,
    onSaveCourseEdit: () -> Unit,
    onRequestDeleteCourse: (String) -> Unit,
    onConfirmDeleteCourse: () -> Unit,
    onDismissDeleteCourse: () -> Unit,
    onToggleCourse: (String) -> Unit,
    onMoveCourse: (String, Int) -> Unit,
    onOpenSeanceSheet: (String) -> Unit,
    onOpenSeanceEditor: (String, String) -> Unit,
    onDismissSeanceEditor: () -> Unit,
    onEditSeanceTitleChange: (String) -> Unit,
    onEditSeanceDurationChange: (String) -> Unit,
    onEditSeanceTypeChange: (String) -> Unit,
    onEditSeanceStatusChange: (String) -> Unit,
    onSaveSeanceEdit: () -> Unit,
    onRequestDeleteSeance: (String, String) -> Unit,
    onConfirmDeleteSeance: () -> Unit,
    onDismissDeleteSeance: () -> Unit,
    onEditSeanceContent: (String) -> Unit,
    onDismissSeanceSheet: () -> Unit,
    onNewSeanceTitleChange: (String) -> Unit,
    onNewSeanceDurationChange: (String) -> Unit,
    onNewSeanceTypeChange: (String) -> Unit,
    onAddSeance: () -> Unit,
    onPriceModeChange: (String) -> Unit,
    onVisibilityChange: (String) -> Unit,
    onAvailabilityModeChange: (String) -> Unit,
    onAvailabilityDateChange: (String) -> Unit,
    onPublishAnyway: () -> Unit,
    onDismissPublishWarning: () -> Unit,
    onDismissPublishSuccess: () -> Unit,
    onSave: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showCategories by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) snackbarHostState.showSnackbar("Brouillon sauvegarde")
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = EditorSurface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (state.formationId == null) "Nouvelle formation" else "Modifier la formation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = onSave, enabled = !state.loading) {
                        Icon(Icons.Default.Check, contentDescription = "Sauvegarder", tint = EditorBlue)
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = EditorSurface, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    if (state.currentStep > 0) {
                        OutlinedButton(
                            onClick = onPreviousStep,
                            enabled = !state.loading,
                            shape = Radius.lg,
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Retour")
                        }
                    }
                    Button(
                        onClick = onPrimaryAction,
                        enabled = !state.loading && !state.publishing,
                        shape = Radius.lg,
                        colors = ButtonDefaults.buttonColors(containerColor = EditorBlue),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(primaryButtonText(state.currentStep), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FigmaSpacing.pageHorizontal, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            EditorStepHeader(currentStep = state.currentStep, onStepSelected = onStepSelected)

            when (state.currentStep) {
                0 -> GeneralInfoStep(
                    state = state,
                    onTitleChange = onTitleChange,
                    onDescriptionChange = onDescriptionChange,
                    onLanguageChange = onLanguageChange,
                    onLevelChange = onLevelChange,
                    onPriceChange = onPriceChange,
                    onCertifiedChange = onCertifiedChange,
                    onPrerequisiteInputChange = onPrerequisiteInputChange,
                    onAddPrerequisite = onAddPrerequisite,
                    onRemovePrerequisite = onRemovePrerequisite,
                    onShowCategories = { showCategories = true }
                )
                1 -> FormationMediaStep(
                    state = state,
                    onThumbnailPicked = onThumbnailPicked,
                    onPreviewVideoPicked = onPreviewVideoPicked,
                    onPreviewVideoUrlChange = onPreviewVideoUrlChange
                )
                2 -> CurriculumBuilderStep(
                    state = state,
                    onNewCourseTitleChange = onNewCourseTitleChange,
                    onAddCourse = onAddCourse,
                    onOpenCourseEditor = onOpenCourseEditor,
                    onRequestDeleteCourse = onRequestDeleteCourse,
                    onToggleCourse = onToggleCourse,
                    onMoveCourse = onMoveCourse,
                    onOpenSeanceSheet = onOpenSeanceSheet,
                    onOpenSeanceEditor = onOpenSeanceEditor,
                    onRequestDeleteSeance = onRequestDeleteSeance,
                    onEditSeanceContent = onEditSeanceContent
                )
                else -> PublishSettingsStep(
                    state = state,
                    onSave = onSave,
                    onPriceModeChange = onPriceModeChange,
                    onPriceChange = onPriceChange,
                    onVisibilityChange = onVisibilityChange,
                    onAvailabilityModeChange = onAvailabilityModeChange,
                    onAvailabilityDateChange = onAvailabilityDateChange
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    if (showCategories) {
        ModalBottomSheet(onDismissRequest = { showCategories = false }) {
            CategorySheet(
                state = state,
                onCategorySelected = {
                    onCategorySelected(it)
                    showCategories = false
                }
            )
        }
    }

    if (state.showSeanceSheet) {
        AddSeanceBottomSheet(
            state = state,
            onDismiss = onDismissSeanceSheet,
            onTitleChange = onNewSeanceTitleChange,
            onDurationChange = onNewSeanceDurationChange,
            onTypeChange = onNewSeanceTypeChange,
            onAdd = onAddSeance
        )
    }

    if (state.editingCourseKey != null) {
        EditCourseDialog(
            state = state,
            onDismiss = onDismissCourseEditor,
            onTitleChange = onEditCourseTitleChange,
            onDescriptionChange = onEditCourseDescriptionChange,
            onSave = onSaveCourseEdit
        )
    }

    if (state.editingSeanceId != null) {
        EditSeanceBottomSheet(
            state = state,
            onDismiss = onDismissSeanceEditor,
            onTitleChange = onEditSeanceTitleChange,
            onDurationChange = onEditSeanceDurationChange,
            onTypeChange = onEditSeanceTypeChange,
            onStatusChange = onEditSeanceStatusChange,
            onSave = onSaveSeanceEdit
        )
    }

    state.coursePendingDelete?.let {
        DestructiveConfirmDialog(
            title = "Supprimer le module ?",
            message = "Toutes les seances et ressources associees seront supprimees.",
            confirmText = "Supprimer",
            onConfirm = onConfirmDeleteCourse,
            onDismiss = onDismissDeleteCourse
        )
    }

    state.seancePendingDelete?.let {
        DestructiveConfirmDialog(
            title = "Supprimer la seance ?",
            message = "La video, les fichiers et la progression associes seront supprimes.",
            confirmText = "Supprimer",
            onConfirm = onConfirmDeleteSeance,
            onDismiss = onDismissDeleteSeance
        )
    }

    if (state.showPublishWarningConfirm) {
        DestructiveConfirmDialog(
            title = "Publier avec avertissements ?",
            message = publicationWarnings(state).joinToString(separator = "\n"),
            confirmText = "Publier quand meme",
            onConfirm = onPublishAnyway,
            onDismiss = onDismissPublishWarning
        )
    }

    if (state.publishSuccess) {
        PublishSuccessDialog(onDismiss = onDismissPublishSuccess, onBack = onBack)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeneralInfoStep(
    state: FormationEditorState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onLevelChange: (FormationLevel) -> Unit,
    onPriceChange: (String) -> Unit,
    onCertifiedChange: (Boolean) -> Unit,
    onPrerequisiteInputChange: (String) -> Unit,
    onAddPrerequisite: () -> Unit,
    onRemovePrerequisite: (String) -> Unit,
    onShowCategories: () -> Unit
) {
    OutlinedTextField(
        value = state.title,
        onValueChange = onTitleChange,
        label = { Text("Titre de la formation") },
        placeholder = { Text("Ex: Maitriser React en 30 jours") },
        supportingText = { Text("${state.title.length}/80") },
        isError = state.title.isBlank() && state.error != null,
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = state.description,
        onValueChange = onDescriptionChange,
        label = { Text("Description") },
        placeholder = { Text("Decrivez ce que les apprenants vont apprendre") },
        minLines = 4,
        isError = state.description.isBlank() && state.error != null,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedButton(
        onClick = onShowCategories,
        shape = Radius.md,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(state.categoryTitle.ifBlank { "Choisir une categorie" }, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
    }

    Text("Niveau", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        FormationLevel.entries.forEach { level ->
            FilterChip(
                selected = state.level == level,
                onClick = { onLevelChange(level) },
                label = { Text(level.label()) }
            )
        }
    }

    OutlinedTextField(
        value = state.language,
        onValueChange = onLanguageChange,
        label = { Text("Langue") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = state.price,
        onValueChange = onPriceChange,
        label = { Text("Prix (0 pour gratuit)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Formation certifiante", fontWeight = FontWeight.Bold)
            Text("Active un parcours avec certificat", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(checked = state.certified, onCheckedChange = onCertifiedChange)
    }

    Text("Prerequis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = state.prerequisiteInput,
            onValueChange = onPrerequisiteInputChange,
            label = { Text("Ajouter un prerequis") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onAddPrerequisite) { Text("Ajouter") }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        state.prerequisites.forEach { prerequisite ->
            AssistChip(
                onClick = {},
                label = { Text(prerequisite) },
                trailingIcon = {
                    IconButton(onClick = { onRemovePrerequisite(prerequisite) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Supprimer", modifier = Modifier.size(16.dp))
                    }
                }
            )
        }
    }
}

@Composable
private fun FormationMediaStep(
    state: FormationEditorState,
    onThumbnailPicked: (String?) -> Unit,
    onPreviewVideoPicked: (String?) -> Unit,
    onPreviewVideoUrlChange: (String) -> Unit
) {
    val context = LocalContext.current
    var mediaError by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        mediaError = validatePickedMedia(context, uri, maxBytes = 5L * 1024L * 1024L, label = "image")
        if (mediaError == null) onThumbnailPicked(uri?.toString())
    }
    val videoPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        mediaError = validatePickedMedia(context, uri, maxBytes = 500L * 1024L * 1024L, label = "video")
        if (mediaError == null) onPreviewVideoPicked(uri?.toString())
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        if (state.formationId == null) {
            InlineNotice("Sauvegardez les informations generales pour attacher les medias a ce brouillon.")
        }
        mediaError?.let { InlineNotice(it) }

        MediaDropzone(
            title = "Image de couverture",
            subtitle = "Format recommande 16:9, JPG ou PNG",
            selectedUri = state.thumbnailUri,
            onPick = { imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
            onClear = { onThumbnailPicked(null) },
            imagePreview = true
        )

        MediaDropzone(
            title = "Video de presentation",
            subtitle = "MP4 recommande, maximum 500 Mo cote backend",
            selectedUri = state.previewVideoUri,
            onPick = { videoPicker.launch(PickVisualMediaRequest(PickVisualMedia.VideoOnly)) },
            onClear = { onPreviewVideoPicked(null) },
            imagePreview = false
        )

        OutlinedTextField(
            value = state.previewVideoUrl,
            onValueChange = onPreviewVideoUrlChange,
            label = { Text("Lien video externe") },
            placeholder = { Text("https://...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        state.uploadProgress?.let { progress ->
            UploadProgressBar(progress = progress)
        }
    }
}

@Composable
private fun MediaDropzone(
    title: String,
    subtitle: String,
    selectedUri: String?,
    onPick: () -> Unit,
    onClear: () -> Unit,
    imagePreview: Boolean
) {
    val context = LocalContext.current
    val fileLabel = remember(selectedUri) { selectedUri?.let { mediaDisplayLabel(context, it) } }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = Radius.lg,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                if (selectedUri != null) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Close, contentDescription = "Retirer")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clip(Radius.md)
                    .background(Color(0xFFF1F3F8))
                    .border(1.dp, Color(0xFFD6DAE6), Radius.md)
                    .clickable(onClick = onPick),
                contentAlignment = Alignment.Center
            ) {
                if (selectedUri != null && imagePreview) {
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    ImageCropGuidelineOverlay()
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Icon(
                            if (imagePreview) Icons.Default.Add else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = EditorBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(if (selectedUri == null) "Choisir un fichier" else "Fichier selectionne", fontWeight = FontWeight.Bold)
                        fileLabel?.let {
                            Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageCropGuidelineOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg)
            .border(1.dp, Color.White.copy(alpha = 0.75f), Radius.md)
    )
}

@Composable
private fun UploadProgressBar(progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            color = EditorBlue,
            trackColor = Color(0xFFE0E0E0),
            modifier = Modifier.fillMaxWidth()
        )
        Text("${(progress * 100).toInt()}% envoye", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
private fun CurriculumBuilderStep(
    state: FormationEditorState,
    onNewCourseTitleChange: (String) -> Unit,
    onAddCourse: () -> Unit,
    onOpenCourseEditor: (String) -> Unit,
    onRequestDeleteCourse: (String) -> Unit,
    onToggleCourse: (String) -> Unit,
    onMoveCourse: (String, Int) -> Unit,
    onOpenSeanceSheet: (String) -> Unit,
    onOpenSeanceEditor: (String, String) -> Unit,
    onRequestDeleteSeance: (String, String) -> Unit,
    onEditSeanceContent: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        if (state.formationId == null) {
            InlineNotice("Sauvegardez les informations generales avant d'ajouter des modules.")
        }

        CurriculumSummaryCounter(courses = state.courses)

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.newCourseTitle,
                onValueChange = onNewCourseTitleChange,
                label = { Text("Nouveau module") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onAddCourse,
                enabled = !state.curriculumLoading && state.formationId != null && state.newCourseTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EditorBlue),
                shape = Radius.md,
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        if (state.curriculumLoading) {
            LinearProgressIndicator(color = EditorBlue, modifier = Modifier.fillMaxWidth())
        }

        if (state.courses.isEmpty() && !state.curriculumLoading) {
            EmptyCurriculum()
        } else {
            state.courses.forEach { course ->
                EditableCourseAccordion(
                    course = course,
                    canMoveUp = course.orderIndex > 0,
                    canMoveDown = course.orderIndex < state.courses.lastIndex,
                    onToggle = { onToggleCourse(course.key) },
                    onMoveUp = { onMoveCourse(course.key, -1) },
                    onMoveDown = { onMoveCourse(course.key, 1) },
                    onEditCourse = { onOpenCourseEditor(course.key) },
                    onDeleteCourse = { onRequestDeleteCourse(course.key) },
                    onAddSeance = { onOpenSeanceSheet(course.key) },
                    onEditSeance = { seanceId -> onOpenSeanceEditor(course.key, seanceId) },
                    onDeleteSeance = { seanceId -> onRequestDeleteSeance(course.key, seanceId) },
                    onEditSeanceContent = onEditSeanceContent
                )
            }
        }
    }
}

@Composable
private fun CurriculumSummaryCounter(courses: List<EditableCourseState>) {
    val seanceCount = courses.sumOf { it.seances.size }
    val minutes = courses.sumOf { course -> course.seances.sumOf { (it.durationSeconds ?: 0) / 60 } }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(Color.White)
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryItem(label = "Modules", value = courses.size.toString())
        SummaryItem(label = "Seances", value = seanceCount.toString())
        SummaryItem(label = "Duree", value = "${minutes}min")
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EditorBlue)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
private fun EditableCourseAccordion(
    course: EditableCourseState,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEditCourse: () -> Unit,
    onDeleteCourse: () -> Unit,
    onAddSeance: () -> Unit,
    onEditSeance: (String) -> Unit,
    onDeleteSeance: (String) -> Unit,
    onEditSeanceContent: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = Radius.lg,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${course.seances.size} seance(s)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Monter")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Descendre")
                }
                IconButton(onClick = onEditCourse) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier module")
                }
                IconButton(onClick = onDeleteCourse) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer module", tint = Color(0xFFD32F2F))
                }
                Icon(
                    if (course.expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (course.expanded) {
                Column(
                    modifier = Modifier.padding(start = Spacing.md, end = Spacing.md, bottom = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    course.seances.forEach { seance ->
                        EditableSeanceRow(
                            seance = seance,
                            onOpenContent = { seance.id?.let(onEditSeanceContent) },
                            onEdit = { seance.id?.let(onEditSeance) },
                            onDelete = { seance.id?.let(onDeleteSeance) }
                        )
                    }
                    OutlinedButton(
                        onClick = onAddSeance,
                        shape = Radius.md,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Ajouter une seance")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableSeanceRow(
    seance: EditableSeanceState,
    onOpenContent: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .background(Color(0xFFF7F8FC))
            .clickable(onClick = onOpenContent)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(EditorBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EditorBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(seance.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${seance.type.label()} - ${(seance.durationSeconds ?: 0) / 60} min - ${seance.status}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Modifier seance", tint = EditorBlue)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Supprimer seance", tint = Color(0xFFD32F2F))
        }
    }
}

@Composable
private fun EditCourseDialog(
    state: FormationEditorState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le module") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedTextField(
                    value = state.editCourseTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Titre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.editCourseDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = state.editCourseTitle.isNotBlank()) { Text("Sauvegarder") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditSeanceBottomSheet(
    state: FormationEditorState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSave: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Modifier la seance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = state.editSeanceTitle,
                onValueChange = onTitleChange,
                label = { Text("Titre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.editSeanceDurationMinutes,
                onValueChange = onDurationChange,
                label = { Text("Duree en minutes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Type", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf("VIDEO", "DOCUMENT", "QUIZ", "LIVE").forEach { type ->
                    FilterChip(selected = state.editSeanceType == type, onClick = { onTypeChange(type) }, label = { Text(type.label()) })
                }
            }
            Text("Statut", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf("PLANIFIEE", "EN_COURS", "TERMINEE", "CONTENU_DISPONIBLE").forEach { status ->
                    FilterChip(selected = state.editSeanceStatus == status, onClick = { onStatusChange(status) }, label = { Text(status.label()) })
                }
            }
            Button(
                onClick = onSave,
                enabled = state.editSeanceTitle.isNotBlank() && !state.curriculumLoading,
                colors = ButtonDefaults.buttonColors(containerColor = EditorBlue),
                shape = Radius.lg,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("Sauvegarder", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddSeanceBottomSheet(
    state: FormationEditorState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Ajouter une seance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = state.newSeanceTitle,
                onValueChange = onTitleChange,
                label = { Text("Titre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.newSeanceDurationMinutes,
                onValueChange = onDurationChange,
                label = { Text("Duree en minutes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf("VIDEO", "DOCUMENT", "QUIZ", "LIVE").forEach { type ->
                    FilterChip(
                        selected = state.newSeanceType == type,
                        onClick = { onTypeChange(type) },
                        label = { Text(type.label()) }
                    )
                }
            }
            Button(
                onClick = onAdd,
                enabled = state.newSeanceTitle.isNotBlank() && !state.curriculumLoading,
                colors = ButtonDefaults.buttonColors(containerColor = EditorBlue),
                shape = Radius.lg,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Ajouter", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun PublishSettingsStep(
    state: FormationEditorState,
    onSave: () -> Unit,
    onPriceModeChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onVisibilityChange: (String) -> Unit,
    onAvailabilityModeChange: (String) -> Unit,
    onAvailabilityDateChange: (String) -> Unit
) {
    val blockers = publicationBlockers(state)
    val warnings = publicationWarnings(state)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        CurriculumSummaryCounter(courses = state.courses)
        PriceModeToggle(
            priceMode = state.priceMode,
            price = state.price,
            onPriceModeChange = onPriceModeChange,
            onPriceChange = onPriceChange
        )
        VisibilityRadioPills(selected = state.visibility, onSelected = onVisibilityChange)
        AvailabilityDatePicker(
            mode = state.availabilityMode,
            date = state.availabilityDate,
            onModeChange = onAvailabilityModeChange,
            onDateChange = onAvailabilityDateChange
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = Radius.lg,
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("Checklist publication", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                PublicationChecklist(state = state)
                blockers.forEach { ChecklistMessage(text = it, blocking = true) }
                warnings.forEach { ChecklistMessage(text = it, blocking = false) }
                OutlinedButton(onClick = onSave, shape = Radius.md, modifier = Modifier.fillMaxWidth()) {
                    Text("Sauvegarder les modifications")
                }
            }
        }
    }
}

@Composable
private fun PriceModeToggle(
    priceMode: String,
    price: String,
    onPriceModeChange: (String) -> Unit,
    onPriceChange: (String) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = Radius.lg) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Prix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf("FREE" to "Gratuit", "PAID" to "Payant").forEach { (value, label) ->
                    FilterChip(selected = priceMode == value, onClick = { onPriceModeChange(value) }, label = { Text(label) })
                }
            }
            if (priceMode == "PAID") {
                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text("Montant en MAD") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisibilityRadioPills(selected: String, onSelected: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = Radius.lg) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Visibilite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf("PUBLIC" to "Publique", "PRIVATE" to "Privee", "INVITATION" to "Invitation").forEach { (value, label) ->
                    FilterChip(selected = selected == value, onClick = { onSelected(value) }, label = { Text(label) })
                }
            }
        }
    }
}

@Composable
private fun AvailabilityDatePicker(
    mode: String,
    date: String,
    onModeChange: (String) -> Unit,
    onDateChange: (String) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = Radius.lg) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Disponibilite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf("NOW" to "Maintenant", "SCHEDULED" to "Planifiee").forEach { (value, label) ->
                    FilterChip(selected = mode == value, onClick = { onModeChange(value) }, label = { Text(label) })
                }
            }
            if (mode == "SCHEDULED") {
                OutlinedTextField(
                    value = date,
                    onValueChange = onDateChange,
                    label = { Text("Date de publication") },
                    placeholder = { Text("2026-06-20") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PublicationChecklist(state: FormationEditorState) {
    ChecklistRow(done = state.title.isNotBlank() && state.description.isNotBlank(), label = "Titre et description renseignes")
    ChecklistRow(done = state.thumbnailUri != null, label = "Image de couverture ajoutee")
    ChecklistRow(done = state.courses.isNotEmpty(), label = "Programme structure en modules")
    ChecklistRow(done = state.courses.isNotEmpty() && state.courses.all { it.seances.isNotEmpty() }, label = "Chaque module a des seances")
}

@Composable
private fun ChecklistMessage(text: String, blocking: Boolean) {
    Text(
        text = if (blocking) "Bloquant: $text" else "Avertissement: $text",
        color = if (blocking) Color(0xFFD32F2F) else Color(0xFFFF9800),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ChecklistRow(done: Boolean, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (done) Color(0xFF2E7D32) else Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (done) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Text(label, color = if (done) Color(0xFF2E7D32) else Color.Gray)
    }
}

@Composable
private fun PublishSuccessDialog(onDismiss: () -> Unit, onBack: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Formation publiee") },
        text = { Text("La formation est maintenant disponible selon vos reglages de publication.") },
        confirmButton = {
            TextButton(onClick = onBack) { Text("Retour au tableau de bord") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Voir la page publique") }
        }
    )
}

@Composable
private fun DestructiveConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText, color = Color(0xFFD32F2F)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun EmptyCurriculum() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(Color.White)
            .padding(Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Text("Ajoutez votre premier module pour construire le programme.", color = Color.Gray)
    }
}

@Composable
private fun InlineNotice(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .background(Color(0xFFE8EAF6))
            .padding(Spacing.md),
        color = EditorBlue,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun CategorySheet(
    state: FormationEditorState,
    onCategorySelected: (FormationCategory) -> Unit
) {
    Column(Modifier.padding(bottom = Spacing.xl)) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
        )
        state.categories.forEach { category ->
            ListItem(
                headlineContent = { Text(category.title) },
                supportingContent = { Text("${category.formationsCount} formations") },
                trailingContent = {
                    if (category.id == state.categoryId) Icon(Icons.Default.Check, contentDescription = null, tint = EditorBlue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = Spacing.sm)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0))
            )
        }
    }
}

@Composable
private fun EditorStepHeader(currentStep: Int, onStepSelected: (Int) -> Unit) {
    val labels = listOf("Infos", "Medias", "Programme", "Publication")
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            labels.forEachIndexed { index, label ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(Radius.md)
                        .clickable { onStepSelected(index) },
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    LinearProgressIndicator(
                        progress = { if (index <= currentStep) 1f else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = EditorBlue,
                        trackColor = Color(0xFFE0E0E0)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index == currentStep) EditorBlue else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Text("Etape ${currentStep + 1} sur 4 - ${labels[currentStep]}", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
    }
}

private fun primaryButtonText(step: Int): String = when (step) {
    0 -> "Sauvegarder et continuer"
    1 -> "Continuer vers programme"
    2 -> "Continuer vers publication"
    else -> "Publier la formation"
}

private fun FormationLevel.label(): String = when (this) {
    FormationLevel.DEBUTANT -> "Debutant"
    FormationLevel.INTERMEDIAIRE -> "Intermediaire"
    FormationLevel.AVANCE -> "Avance"
}

private fun String.label(): String = when (this) {
    "VIDEO" -> "Video"
    "DOCUMENT" -> "Document"
    "QUIZ" -> "Quiz"
    "LIVE" -> "Live"
    else -> this.lowercase().replaceFirstChar { it.titlecase() }
}

private fun validatePickedMedia(context: Context, uri: Uri?, maxBytes: Long, label: String): String? {
    if (uri == null) return null
    val size = queryMediaSize(context, uri) ?: return null
    return if (size > maxBytes) {
        "Le fichier $label depasse la limite de ${maxBytes / 1024 / 1024} Mo."
    } else {
        null
    }
}

private fun mediaDisplayLabel(context: Context, uriString: String): String {
    val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return uriString
    val name = queryMediaName(context, uri) ?: uri.lastPathSegment ?: "Fichier selectionne"
    val size = queryMediaSize(context, uri)
    return if (size != null) "$name - ${formatBytes(size)}" else name
}

private fun queryMediaName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
}

private fun queryMediaSize(context: Context, uri: Uri): Long? {
    return context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        ?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (index >= 0 && cursor.moveToFirst()) cursor.getLong(index) else null
        }
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes / 1024f / 1024f
    return if (mb >= 1f) String.format(Locale.getDefault(), "%.1f Mo", mb) else "${bytes / 1024} Ko"
}
