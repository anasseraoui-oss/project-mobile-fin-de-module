package com.elearning.app.data.repository

import android.util.Log
import com.elearning.app.data.local.db.FormationDao
import com.elearning.app.data.local.db.SeanceDao
import com.elearning.app.data.local.entity.FormationEntity
import com.elearning.app.data.local.entity.SeanceEntity
import com.elearning.app.data.remote.api.ResourceApiService
import com.elearning.app.data.remote.dto.FormationDto
import com.elearning.app.data.remote.mapper.toDomain
import com.elearning.app.domain.model.Course
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.FormationCategory
import com.elearning.app.domain.model.FormationLevel
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.repository.FormationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.util.UUID
import javax.inject.Inject

class FormationRepositoryImpl @Inject constructor(
    private val api: ResourceApiService,
    private val formationDao: FormationDao,
    private val seanceDao: SeanceDao
) : FormationRepository {

    override suspend fun getFormationById(id: String): Formation {
        val remote = api.getFormation(id)
        val formation = remote.toDomainWithCoverUrl()
        formationDao.upsert(formation.toEntity())
        return formation
    }

    override suspend fun getCoursesForFormation(formationId: String): List<Course> {
        val courses = api.getCourses(formationId)
        return courses.map { courseDto ->
            val seances = api.getSeances(courseDto.id)
            seanceDao.upsertAll(seances.map { it.toDomain().toEntity(formationId) })
            courseDto.toDomain().copy(seances = seances.map { it.toDomain() })
        }
    }

    override suspend fun enrollInFormation(formationId: String) {
        val response = api.enrollInFormation(formationId)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        val formation = api.getFormation(formationId).toDomainWithCoverUrl().copy(isEnrolled = true)
        formationDao.upsert(formation.toEntity())
    }

    override suspend fun getFavoriteFormations(): Result<List<Formation>> {
        Log.i(TAG, "Favorites endpoint is not available yet; returning an empty state.")
        return Result.Success(emptyList())
    }

    override suspend fun getRecommendedFormations(): Result<List<Formation>> {
        return try {
            val response = api.getFormations(0, 10, null, null)
            Result.Success(response.content.orEmpty().map { it.toDomainWithCoverUrl() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getEnrolledFormations(): Result<List<Formation>> {
        return refreshEnrolledFormations()
    }

    override fun observeEnrolledFormations(): Flow<List<Formation>> {
        return formationDao.observeEnrolled().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshEnrolledFormations(): Result<List<Formation>> {
        return try {
            val remote = api.getEnrolledFormations()
                .map { it.toDomainWithCoverUrl().copy(isEnrolled = true) }
            formationDao.upsertAll(remote.map { it.toEntity() })
            Result.Success(remote)
        } catch (e: Exception) {
            val local = formationDao.findEnrolled().map { it.toDomain() }
            if (local.isNotEmpty()) Result.Success(local) else Result.Error(e)
        }
    }

    override suspend fun getCategories(): Result<List<FormationCategory>> {
        return try {
            Result.Success(api.getCategories().map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun Formation.toEntity() = FormationEntity(
        id = id.toString(),
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        level = level.name,
        language = language,
        organisation = organisation,
        durationHours = durationHours,
        price = price,
        currency = currency,
        rating = rating,
        enrollmentCount = enrollmentCount,
        courseCount = courseCount,
        tags = tags.joinToString(prefix = "[", postfix = "]") { "\"$it\"" },
        isEnrolled = isEnrolled,
        progressPercent = progressPercent,
        categoryId = categoryId,
        prerequisites = prerequisites.joinToString(separator = "\n"),
        certified = certified
    )

    private suspend fun FormationDto.toDomainWithCoverUrl(): Formation {
        val formation = toDomain()
        if (!formation.thumbnailUrl.isNullOrBlank()) return formation

        val coverUrl = id?.let { formationId ->
            runCatching { api.getFormationCoverUrl(formationId).url }.getOrNull()
        }
        return formation.copy(thumbnailUrl = coverUrl)
    }

    private fun FormationEntity.toDomain() = Formation(
        id = UUID.fromString(id),
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        level = runCatching { FormationLevel.valueOf(level) }.getOrDefault(FormationLevel.DEBUTANT),
        language = language,
        organisation = organisation,
        durationHours = durationHours,
        price = price,
        currency = currency,
        rating = rating,
        enrollmentCount = enrollmentCount,
        courseCount = courseCount,
        tags = emptyList(),
        isEnrolled = isEnrolled,
        progressPercent = progressPercent,
        categoryId = categoryId,
        prerequisites = prerequisites.lines().filter { it.isNotBlank() },
        certified = certified
    )

    private fun com.elearning.app.domain.model.Seance.toEntity(formationId: String) = SeanceEntity(
        id = id.toString(),
        courseId = courseId.toString(),
        formationId = formationId,
        title = title,
        description = description,
        type = type.name,
        durationSeconds = durationSeconds,
        orderIndex = orderIndex,
        status = status.name,
        videoKey = videoKey,
        localVideoPath = null,
        meetingLink = meetingLink,
        scheduledAt = scheduledAt,
        isCompleted = isCompleted,
        progressSeconds = progressSeconds
    )

    private companion object {
        private const val TAG = "FormationRepositoryImpl"
    }
}
