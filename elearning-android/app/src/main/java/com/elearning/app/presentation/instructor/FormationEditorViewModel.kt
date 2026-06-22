package com.elearning.app.presentation.instructor

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.domain.model.FormationCategory
import com.elearning.app.domain.model.FormationDraftRequest
import com.elearning.app.domain.model.FormationLevel
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.model.SeanceType
import com.elearning.app.domain.repository.FormationRepository
import com.elearning.app.domain.repository.InstructorRepository
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
class FormationEditorViewModel @Inject constructor(
    private val instructorRepository: InstructorRepository,
    private val formationRepository: FormationRepository,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialFormationId: String? = savedStateHandle["formationId"]

    private val _state = MutableStateFlow(FormationEditorState(formationId = initialFormationId))
    val state = _state.asStateFlow()

    init {
        loadCategories()
        initialFormationId?.let {
            loadFormation(it)
            loadCurriculum(it)
        }
    }

    fun goToStep(index: Int) {
        _state.update { it.copy(currentStep = index.coerceIn(0, 3)) }
    }

    fun previousStep() {
        _state.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun primaryAction() {
        if (state.value.currentStep == 0) {
            saveDraft(advanceOnSuccess = true)
        } else if (state.value.currentStep == 3) {
            publishFormation()
        } else {
            _state.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3)) }
        }
    }

    fun updateTitle(value: String) {
        if (value.length <= 80) _state.update { it.copy(title = value, saved = false) }
    }

    fun updateDescription(value: String) = _state.update { it.copy(description = value, saved = false) }
    fun updateLanguage(value: String) = _state.update { it.copy(language = value, saved = false) }
    fun updateLevel(value: FormationLevel) = _state.update { it.copy(level = value, saved = false) }
    fun updatePrice(value: String) = _state.update { it.copy(price = value, saved = false) }
    fun updateCertified(value: Boolean) = _state.update { it.copy(certified = value, saved = false) }
    fun updatePrerequisiteInput(value: String) = _state.update { it.copy(prerequisiteInput = value) }
    fun selectCategory(category: FormationCategory) = _state.update {
        it.copy(categoryId = category.id, categoryTitle = category.title, saved = false)
    }

    fun addPrerequisite() {
        val current = state.value
        val value = current.prerequisiteInput.trim()
        if (value.isNotBlank() && value !in current.prerequisites) {
            _state.update { it.copy(prerequisites = it.prerequisites + value, prerequisiteInput = "", saved = false) }
        }
    }

    fun removePrerequisite(value: String) {
        _state.update { it.copy(prerequisites = it.prerequisites - value, saved = false) }
    }

    fun updateThumbnailUri(value: String?) = _state.update { it.copy(thumbnailUri = value, saved = false) }
    fun updatePreviewVideoUri(value: String?) = _state.update { it.copy(previewVideoUri = value, saved = false) }
    fun updatePreviewVideoUrl(value: String) = _state.update { it.copy(previewVideoUrl = value, saved = false) }
    fun updatePriceMode(value: String) = _state.update {
        it.copy(priceMode = value, price = if (value == "FREE") "0" else it.price, saved = false)
    }
    fun updateVisibility(value: String) = _state.update { it.copy(visibility = value, saved = false) }
    fun updateAvailabilityMode(value: String) = _state.update { it.copy(availabilityMode = value, saved = false) }
    fun updateAvailabilityDate(value: String) = _state.update { it.copy(availabilityDate = value, saved = false) }
    fun dismissPublishSuccess() = _state.update { it.copy(publishSuccess = false) }
    fun dismissPublishWarning() = _state.update { it.copy(showPublishWarningConfirm = false) }
    fun dismissDeleteCourseConfirm() = _state.update { it.copy(coursePendingDelete = null) }
    fun dismissDeleteSeanceConfirm() = _state.update { it.copy(seancePendingDelete = null) }

    fun updateNewCourseTitle(value: String) = _state.update { it.copy(newCourseTitle = value) }
    fun updateNewSeanceTitle(value: String) = _state.update { it.copy(newSeanceTitle = value) }
    fun updateNewSeanceDuration(value: String) = _state.update { it.copy(newSeanceDurationMinutes = value) }
    fun updateNewSeanceType(value: String) = _state.update { it.copy(newSeanceType = value) }
    fun updateEditCourseTitle(value: String) = _state.update { it.copy(editCourseTitle = value) }
    fun updateEditCourseDescription(value: String) = _state.update { it.copy(editCourseDescription = value) }
    fun updateEditSeanceTitle(value: String) = _state.update { it.copy(editSeanceTitle = value) }
    fun updateEditSeanceDuration(value: String) = _state.update { it.copy(editSeanceDurationMinutes = value) }
    fun updateEditSeanceType(value: String) = _state.update { it.copy(editSeanceType = value) }
    fun updateEditSeanceStatus(value: String) = _state.update { it.copy(editSeanceStatus = value) }

    fun toggleCourse(courseKey: String) {
        _state.update {
            it.copy(courses = it.courses.map { course ->
                if (course.key == courseKey) course.copy(expanded = !course.expanded) else course
            })
        }
    }

    fun openSeanceSheet(courseKey: String) {
        _state.update {
            it.copy(
                targetCourseKey = courseKey,
                showSeanceSheet = true,
                newSeanceTitle = "",
                newSeanceDurationMinutes = "",
                newSeanceType = "VIDEO"
            )
        }
    }

    fun dismissSeanceSheet() {
        _state.update { it.copy(showSeanceSheet = false, targetCourseKey = null) }
    }

    fun moveCourse(courseKey: String, direction: Int) {
        _state.update { current ->
            val mutable = current.courses.toMutableList()
            val from = mutable.indexOfFirst { it.key == courseKey }
            val to = (from + direction).coerceIn(0, mutable.lastIndex.coerceAtLeast(0))
            if (from >= 0 && from != to) {
                val item = mutable.removeAt(from)
                mutable.add(to, item)
            }
            current.copy(courses = mutable.mapIndexed { index, course -> course.copy(orderIndex = index) })
        }
    }

    fun addCourse() {
        val current = state.value
        val formationId = current.formationId
        val title = current.newCourseTitle.trim()
        if (formationId.isNullOrBlank()) {
            _state.update { it.copy(error = "Sauvegardez d'abord les informations generales") }
            return
        }
        if (title.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(curriculumLoading = true, error = null) }
            when (val result = instructorRepository.createCourse(formationId, title, null, current.courses.size)) {
                is Result.Success -> _state.update {
                    it.copy(
                        curriculumLoading = false,
                        newCourseTitle = "",
                        courses = it.courses + EditableCourseState(
                            id = result.data.id.toString(),
                            key = result.data.id.toString(),
                            title = result.data.title,
                            description = result.data.description.orEmpty(),
                            orderIndex = result.data.orderIndex,
                            expanded = true
                        )
                    )
                }
                is Result.Error -> _state.update {
                    it.copy(curriculumLoading = false, error = result.message ?: "Impossible d'ajouter ce module")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun openCourseEditor(courseKey: String) {
        val course = state.value.courses.firstOrNull { it.key == courseKey } ?: return
        _state.update {
            it.copy(
                editingCourseKey = course.key,
                editCourseTitle = course.title,
                editCourseDescription = course.description
            )
        }
    }

    fun dismissCourseEditor() {
        _state.update { it.copy(editingCourseKey = null, editCourseTitle = "", editCourseDescription = "") }
    }

    fun saveCourseEdit() {
        val current = state.value
        val formationId = current.formationId ?: return
        val course = current.courses.firstOrNull { it.key == current.editingCourseKey } ?: return
        val title = current.editCourseTitle.trim()
        if (title.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(curriculumLoading = true, error = null) }
            when (val result = instructorRepository.updateCourse(
                formationId = formationId,
                courseId = course.id.orEmpty(),
                title = title,
                description = current.editCourseDescription.trim().ifBlank { null },
                orderIndex = course.orderIndex
            )) {
                is Result.Success -> _state.update {
                    it.copy(
                        curriculumLoading = false,
                        editingCourseKey = null,
                        courses = it.courses.map { editable ->
                            if (editable.key == course.key) editable.copy(
                                title = result.data.title,
                                description = result.data.description.orEmpty(),
                                orderIndex = result.data.orderIndex
                            ) else editable
                        }
                    )
                }
                is Result.Error -> _state.update { it.copy(curriculumLoading = false, error = result.message ?: "Impossible de modifier ce module") }
                Result.Loading -> Unit
            }
        }
    }

    fun requestDeleteCourse(courseKey: String) {
        _state.update { it.copy(coursePendingDelete = courseKey) }
    }

    fun deleteCourse() {
        val current = state.value
        val key = current.coursePendingDelete ?: return
        val course = current.courses.firstOrNull { it.key == key } ?: return
        val courseId = course.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(curriculumLoading = true, error = null) }
            when (val result = instructorRepository.deleteCourse(courseId)) {
                is Result.Success -> _state.update {
                    it.copy(
                        curriculumLoading = false,
                        coursePendingDelete = null,
                        courses = it.courses.filterNot { editable -> editable.key == key }
                            .mapIndexed { index, editable -> editable.copy(orderIndex = index) }
                    )
                }
                is Result.Error -> _state.update { it.copy(curriculumLoading = false, error = result.message ?: "Impossible de supprimer ce module") }
                Result.Loading -> Unit
            }
        }
    }

    fun addSeance() {
        val current = state.value
        val course = current.courses.firstOrNull { it.key == current.targetCourseKey }
        val courseId = course?.id
        val title = current.newSeanceTitle.trim()
        if (courseId.isNullOrBlank() || title.isBlank()) return

        val orderIndex = course.seances.size
        val durationSeconds = current.newSeanceDurationMinutes.toIntOrNull()?.let { it * 60 }
        val backendType = if (current.newSeanceType == "LIVE") "LIVE" else "ENREGISTREE"

        viewModelScope.launch {
            _state.update { it.copy(curriculumLoading = true, error = null) }
            when (val result = instructorRepository.createSeance(courseId, title, backendType, durationSeconds, orderIndex)) {
                is Result.Success -> _state.update {
                    it.copy(
                        curriculumLoading = false,
                        showSeanceSheet = false,
                        targetCourseKey = null,
                        newSeanceTitle = "",
                        newSeanceDurationMinutes = "",
                        courses = it.courses.map { editableCourse ->
                            if (editableCourse.key == course.key) {
                                editableCourse.copy(
                                    expanded = true,
                                    seances = editableCourse.seances + EditableSeanceState(
                                        id = result.data.id.toString(),
                                        title = result.data.title,
                                        type = current.newSeanceType,
                                        durationSeconds = result.data.durationSeconds,
                                        orderIndex = result.data.orderIndex
                                    )
                                )
                            } else {
                                editableCourse
                            }
                        }
                    )
                }
                is Result.Error -> _state.update {
                    it.copy(curriculumLoading = false, error = result.message ?: "Impossible d'ajouter cette seance")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun openSeanceEditor(courseKey: String, seanceId: String) {
        val seance = state.value.courses.firstOrNull { it.key == courseKey }?.seances?.firstOrNull { it.id == seanceId } ?: return
        _state.update {
            it.copy(
                editingSeanceCourseKey = courseKey,
                editingSeanceId = seance.id,
                editSeanceTitle = seance.title,
                editSeanceDurationMinutes = ((seance.durationSeconds ?: 0) / 60).toString(),
                editSeanceType = seance.type,
                editSeanceStatus = seance.status
            )
        }
    }

    fun dismissSeanceEditor() {
        _state.update {
            it.copy(
                editingSeanceCourseKey = null,
                editingSeanceId = null,
                editSeanceTitle = "",
                editSeanceDurationMinutes = "",
                editSeanceType = "VIDEO",
                editSeanceStatus = "PLANIFIEE"
            )
        }
    }

    fun saveSeanceEdit() {
        val current = state.value
        val course = current.courses.firstOrNull { it.key == current.editingSeanceCourseKey } ?: return
        val seance = course.seances.firstOrNull { it.id == current.editingSeanceId } ?: return
        val seanceId = seance.id ?: return
        val title = current.editSeanceTitle.trim()
        if (title.isBlank()) return
        val durationSeconds = current.editSeanceDurationMinutes.toIntOrNull()?.let { it * 60 }
        val backendType = if (current.editSeanceType == "LIVE") "LIVE" else "ENREGISTREE"
        viewModelScope.launch {
            _state.update { it.copy(curriculumLoading = true, error = null) }
            when (val result = instructorRepository.updateSeance(
                seanceId = seanceId,
                title = title,
                description = null,
                type = backendType,
                durationSeconds = durationSeconds,
                orderIndex = seance.orderIndex,
                status = current.editSeanceStatus
            )) {
                is Result.Success -> _state.update {
                    it.copy(
                        curriculumLoading = false,
                        editingSeanceCourseKey = null,
                        editingSeanceId = null,
                        courses = it.courses.map { editableCourse ->
                            if (editableCourse.key == course.key) {
                                editableCourse.copy(seances = editableCourse.seances.map { editableSeance ->
                                    if (editableSeance.id == seanceId) editableSeance.copy(
                                        title = result.data.title,
                                        type = current.editSeanceType,
                                        durationSeconds = result.data.durationSeconds,
                                        orderIndex = result.data.orderIndex,
                                        status = current.editSeanceStatus
                                    ) else editableSeance
                                })
                            } else editableCourse
                        }
                    )
                }
                is Result.Error -> _state.update { it.copy(curriculumLoading = false, error = result.message ?: "Impossible de modifier cette seance") }
                Result.Loading -> Unit
            }
        }
    }

    fun requestDeleteSeance(courseKey: String, seanceId: String) {
        _state.update { it.copy(seancePendingDelete = courseKey to seanceId) }
    }

    fun deleteSeance() {
        val pending = state.value.seancePendingDelete ?: return
        viewModelScope.launch {
            _state.update { it.copy(curriculumLoading = true, error = null) }
            when (val result = instructorRepository.deleteSeance(pending.second)) {
                is Result.Success -> _state.update {
                    it.copy(
                        curriculumLoading = false,
                        seancePendingDelete = null,
                        courses = it.courses.map { course ->
                            if (course.key == pending.first) {
                                course.copy(seances = course.seances.filterNot { seance -> seance.id == pending.second }
                                    .mapIndexed { index, seance -> seance.copy(orderIndex = index) })
                            } else course
                        }
                    )
                }
                is Result.Error -> _state.update { it.copy(curriculumLoading = false, error = result.message ?: "Impossible de supprimer cette seance") }
                Result.Loading -> Unit
            }
        }
    }

    fun saveDraft(advanceOnSuccess: Boolean = false) {
        val current = state.value
        val price = current.price.replace(',', '.').toDoubleOrNull() ?: 0.0
        if (current.title.isBlank() || current.description.isBlank()) {
            _state.update { it.copy(error = "Veuillez renseigner le titre et la description") }
            return
        }

        val request = FormationDraftRequest(
            title = current.title.trim(),
            description = current.description.trim(),
            level = current.level,
            language = current.language.trim().ifBlank { "Francais" },
            price = price,
            categoryId = current.categoryId,
            prerequisites = current.prerequisites,
            certified = current.certified
        )

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, saved = false) }
            val result = current.formationId?.let { id ->
                instructorRepository.updateFormation(id, request)
            } ?: instructorRepository.createFormationDraft(request)

            when (result) {
                is Result.Success -> {
                    val newId = if (result.data is com.elearning.app.domain.model.Formation) {
                        result.data.id.toString()
                    } else {
                        current.formationId
                    }
                    _state.update {
                        it.copy(
                            formationId = newId,
                            loading = false,
                            saved = true,
                            currentStep = if (advanceOnSuccess) 1 else it.currentStep
                        )
                    }
                    uploadCoverIfNeeded(newId, current.thumbnailUri)
                    if (newId != null && current.courses.isEmpty()) loadCurriculum(newId)
                }
                is Result.Error -> _state.update {
                    it.copy(loading = false, error = result.message ?: "Erreur lors de la sauvegarde")
                }
                Result.Loading -> Unit
            }
        }
    }

    fun publishFormation(forceWarnings: Boolean = false) {
        val current = state.value
        val formationId = current.formationId
        if (formationId.isNullOrBlank()) {
            _state.update { it.copy(error = "Sauvegardez le brouillon avant publication") }
            return
        }
        val blockers = publicationBlockers(current)
        if (blockers.isNotEmpty()) {
            _state.update { it.copy(error = blockers.first()) }
            return
        }
        if (!forceWarnings && publicationWarnings(current).isNotEmpty()) {
            _state.update { it.copy(showPublishWarningConfirm = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(publishing = true, error = null, showPublishWarningConfirm = false) }
            when (val result = instructorRepository.publishFormation(formationId)) {
                is Result.Success -> _state.update {
                    it.copy(publishing = false, publishSuccess = true, saved = true)
                }
                is Result.Error -> _state.update {
                    it.copy(publishing = false, error = result.message ?: "Publication refusee par le backend")
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = formationRepository.getCategories()) {
                is Result.Success -> _state.update {
                    it.copy(
                        categories = result.data,
                        categoryTitle = result.data.firstOrNull { category -> category.id == it.categoryId }?.title ?: it.categoryTitle
                    )
                }
                else -> Unit
            }
        }
    }

    private fun loadFormation(id: String) {
        viewModelScope.launch {
            runCatching { formationRepository.getFormationById(id) }
                .onSuccess { formation ->
                    _state.update {
                        it.copy(
                            title = formation.title,
                            description = formation.description,
                            level = formation.level,
                            language = formation.language,
                            price = formation.price.toString(),
                            thumbnailUri = formation.thumbnailUrl,
                            categoryId = formation.categoryId,
                            categoryTitle = it.categories.firstOrNull { category -> category.id == formation.categoryId }?.title.orEmpty(),
                            prerequisites = formation.prerequisites,
                            certified = formation.certified
                        )
                    }
                }
        }
    }

    fun refreshCurriculum() {
        state.value.formationId?.let { loadCurriculum(it) }
    }

    private fun loadCurriculum(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(curriculumLoading = true) }
            runCatching { formationRepository.getCoursesForFormation(id) }
                .onSuccess { courses ->
                    _state.update {
                        it.copy(
                            curriculumLoading = false,
                            courses = courses.sortedBy { course -> course.orderIndex }.map { course ->
                                EditableCourseState(
                                    id = course.id.toString(),
                                    key = course.id.toString(),
                                    title = course.title,
                                    description = course.description.orEmpty(),
                                    orderIndex = course.orderIndex,
                                    expanded = true,
                                    seances = course.seances.sortedBy { seance -> seance.orderIndex }.map { seance ->
                                        EditableSeanceState(
                                            id = seance.id.toString(),
                                            title = seance.title,
                                            type = when (seance.type) {
                                                SeanceType.LIVE -> "LIVE"
                                                SeanceType.DOCUMENT -> "DOCUMENT"
                                                SeanceType.QUIZ -> "QUIZ"
                                                SeanceType.VIDEO -> "VIDEO"
                                            },
                                            durationSeconds = seance.durationSeconds,
                                            orderIndex = seance.orderIndex,
                                            status = seance.status.name
                                        )
                                    }
                                )
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(curriculumLoading = false, error = error.localizedMessage ?: "Impossible de charger le programme")
                    }
                }
        }
    }

    private suspend fun uploadCoverIfNeeded(formationId: String?, thumbnailUri: String?) {
        if (formationId.isNullOrBlank() || thumbnailUri.isNullOrBlank()) return
        val uri = runCatching { Uri.parse(thumbnailUri) }.getOrNull() ?: return
        if (uri.scheme !in listOf("content", "file")) return
        val part = multipartFromUri(uri, "thumbnail") ?: run {
            _state.update { it.copy(error = "Impossible de lire l'image de couverture") }
            return
        }
        when (val result = instructorRepository.updateFormationCover(formationId, part)) {
            is Result.Success -> _state.update { it.copy(thumbnailUri = result.data.thumbnailUrl, saved = true) }
            is Result.Error -> _state.update { it.copy(error = result.message ?: "Image de couverture non sauvegardee") }
            Result.Loading -> Unit
        }
    }

    private fun multipartFromUri(uri: Uri, partName: String): MultipartBody.Part? {
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

fun publicationBlockers(state: FormationEditorState): List<String> = buildList {
    if (state.title.isBlank() || state.description.isBlank()) add("Titre et description obligatoires")
    if (state.courses.isEmpty()) add("Ajoutez au moins un module")
    if (state.courses.any { it.seances.isEmpty() }) add("Chaque module doit contenir au moins une seance")
}

fun publicationWarnings(state: FormationEditorState): List<String> = buildList {
    if (state.thumbnailUri == null) add("Image de couverture manquante")
    if (state.priceMode == "PAID" && state.price.replace(',', '.').toDoubleOrNull().let { it == null || it <= 0.0 }) {
        add("Prix payant non renseigne")
    }
    if (state.availabilityMode == "SCHEDULED" && state.availabilityDate.isBlank()) add("Date programmee non renseignee")
}

data class FormationEditorState(
    val formationId: String? = null,
    val currentStep: Int = 0,
    val title: String = "",
    val description: String = "",
    val categoryId: String? = null,
    val categoryTitle: String = "",
    val categories: List<FormationCategory> = emptyList(),
    val level: FormationLevel = FormationLevel.DEBUTANT,
    val language: String = "Francais",
    val price: String = "0",
    val prerequisites: List<String> = emptyList(),
    val prerequisiteInput: String = "",
    val certified: Boolean = false,
    val priceMode: String = "FREE",
    val visibility: String = "PUBLIC",
    val availabilityMode: String = "NOW",
    val availabilityDate: String = "",
    val thumbnailUri: String? = null,
    val previewVideoUri: String? = null,
    val previewVideoUrl: String = "",
    val uploadProgress: Float? = null,
    val courses: List<EditableCourseState> = emptyList(),
    val curriculumLoading: Boolean = false,
    val newCourseTitle: String = "",
    val showSeanceSheet: Boolean = false,
    val targetCourseKey: String? = null,
    val newSeanceTitle: String = "",
    val newSeanceDurationMinutes: String = "",
    val newSeanceType: String = "VIDEO",
    val editingCourseKey: String? = null,
    val editCourseTitle: String = "",
    val editCourseDescription: String = "",
    val coursePendingDelete: String? = null,
    val editingSeanceCourseKey: String? = null,
    val editingSeanceId: String? = null,
    val editSeanceTitle: String = "",
    val editSeanceDurationMinutes: String = "",
    val editSeanceType: String = "VIDEO",
    val editSeanceStatus: String = "PLANIFIEE",
    val seancePendingDelete: Pair<String, String>? = null,
    val loading: Boolean = false,
    val publishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val showPublishWarningConfirm: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

data class EditableCourseState(
    val id: String?,
    val key: String,
    val title: String,
    val description: String,
    val orderIndex: Int,
    val expanded: Boolean = false,
    val seances: List<EditableSeanceState> = emptyList()
)

data class EditableSeanceState(
    val id: String?,
    val title: String,
    val type: String,
    val durationSeconds: Int?,
    val orderIndex: Int,
    val status: String = "PLANIFIEE"
)
