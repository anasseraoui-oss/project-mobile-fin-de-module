package com.elearning.app.data.repository

import com.elearning.app.data.remote.api.ResourceApiService
import com.elearning.app.data.remote.dto.CourseRequestDto
import com.elearning.app.data.remote.dto.FormationRequestDto
import com.elearning.app.data.remote.dto.SeanceRequestDto
import com.elearning.app.data.remote.mapper.toDomain
import com.elearning.app.domain.model.Course
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.FormationDraftRequest
import com.elearning.app.domain.model.InstructorDashboard
import com.elearning.app.domain.model.InstructorFormationSummary
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.model.Seance
import com.elearning.app.domain.repository.InstructorRepository
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject

class InstructorRepositoryImpl @Inject constructor(
    private val api: ResourceApiService
) : InstructorRepository {

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun getDashboard(): Result<InstructorDashboard> = runCatchingResult {
        api.getInstructorDashboard().toDomain()
    }

    override suspend fun getMyFormations(status: String?, page: Int, size: Int): Result<List<InstructorFormationSummary>> =
        runCatchingResult {
            api.getInstructorFormations(status, page, size).content.orEmpty().map { it.toDomain() }
        }

    override suspend fun createFormationDraft(request: FormationDraftRequest): Result<Formation> = runCatchingResult {
        api.createFormation(gson.toJson(request.toDto()).toRequestBody(jsonMediaType)).toDomain()
    }

    override suspend fun updateFormation(id: String, request: FormationDraftRequest): Result<Unit> = runCatchingResult {
        val response = api.updateFormation(id, request.toDto())
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun updateFormationCover(id: String, thumbnail: MultipartBody.Part): Result<Formation> = runCatchingResult {
        api.updateFormationCover(id, thumbnail).toDomain()
    }

    override suspend fun publishFormation(id: String): Result<Unit> = runCatchingResult {
        val response = api.publishFormation(id)
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun archiveFormation(id: String): Result<Unit> = runCatchingResult {
        val response = api.archiveFormation(id)
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun deleteFormation(id: String): Result<Unit> = runCatchingResult {
        val response = api.deleteFormation(id)
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun createCourse(
        formationId: String,
        title: String,
        description: String?,
        orderIndex: Int,
        estimatedDuration: Int?
    ): Result<Course> = runCatchingResult {
        api.createCourse(
            CourseRequestDto(
                title = title,
                description = description,
                orderIndex = orderIndex,
                estimatedDuration = estimatedDuration,
                formationId = formationId
            )
        ).toDomain()
    }

    override suspend fun updateCourse(
        formationId: String,
        courseId: String,
        title: String,
        description: String?,
        orderIndex: Int,
        estimatedDuration: Int?,
        status: String?
    ): Result<Course> = runCatchingResult {
        api.updateCourse(
            courseId,
            CourseRequestDto(
                title = title,
                description = description,
                orderIndex = orderIndex,
                estimatedDuration = estimatedDuration,
                formationId = formationId,
                status = status
            )
        ).toDomain()
    }

    override suspend fun deleteCourse(courseId: String): Result<Unit> = runCatchingResult {
        val response = api.deleteCourse(courseId)
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun createSeance(
        courseId: String,
        title: String,
        type: String,
        durationSeconds: Int?,
        orderIndex: Int,
        description: String?
    ): Result<Seance> = runCatchingResult {
        val dto = SeanceRequestDto(
            title = title,
            description = description,
            type = type,
            duration = durationSeconds,
            orderIndex = orderIndex
        )
        api.createSeance(
            courseId = courseId,
            data = gson.toJson(dto).toRequestBody(jsonMediaType)
        ).toDomain()
    }

    override suspend fun updateSeance(
        seanceId: String,
        title: String,
        description: String?,
        type: String,
        durationSeconds: Int?,
        orderIndex: Int,
        status: String?
    ): Result<Seance> = runCatchingResult {
        api.updateSeance(
            seanceId,
            SeanceRequestDto(
                title = title,
                description = description,
                type = type,
                duration = durationSeconds,
                orderIndex = orderIndex,
                status = status
            )
        ).toDomain()
    }

    override suspend fun deleteSeance(seanceId: String): Result<Unit> = runCatchingResult {
        val response = api.deleteSeance(seanceId)
        if (!response.isSuccessful) throw HttpException(response)
    }

    private fun FormationDraftRequest.toDto() = FormationRequestDto(
        title = title,
        description = description,
        level = level.name,
        language = language,
        price = price,
        currency = currency,
        categoryId = categoryId,
        prerequisites = prerequisites,
        certified = certified
    )

    private suspend inline fun <T> runCatchingResult(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.Success(block())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage)
        }
    }
}
