package com.elearning.app.domain.repository

import com.elearning.app.domain.model.Course
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.FormationDraftRequest
import com.elearning.app.domain.model.InstructorDashboard
import com.elearning.app.domain.model.InstructorFormationSummary
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.model.Seance
import okhttp3.MultipartBody

interface InstructorRepository {
    suspend fun getDashboard(): Result<InstructorDashboard>
    suspend fun getMyFormations(status: String? = null, page: Int = 0, size: Int = 20): Result<List<InstructorFormationSummary>>
    suspend fun createFormationDraft(request: FormationDraftRequest): Result<Formation>
    suspend fun updateFormation(id: String, request: FormationDraftRequest): Result<Unit>
    suspend fun updateFormationCover(id: String, thumbnail: MultipartBody.Part): Result<Formation>
    suspend fun publishFormation(id: String): Result<Unit>
    suspend fun archiveFormation(id: String): Result<Unit>
    suspend fun deleteFormation(id: String): Result<Unit>
    suspend fun createCourse(formationId: String, title: String, description: String?, orderIndex: Int, estimatedDuration: Int? = null): Result<Course>
    suspend fun updateCourse(formationId: String, courseId: String, title: String, description: String?, orderIndex: Int, estimatedDuration: Int? = null, status: String? = null): Result<Course>
    suspend fun deleteCourse(courseId: String): Result<Unit>
    suspend fun createSeance(courseId: String, title: String, type: String, durationSeconds: Int?, orderIndex: Int, description: String? = null): Result<Seance>
    suspend fun updateSeance(seanceId: String, title: String, description: String?, type: String, durationSeconds: Int?, orderIndex: Int, status: String? = null): Result<Seance>
    suspend fun deleteSeance(seanceId: String): Result<Unit>
}
