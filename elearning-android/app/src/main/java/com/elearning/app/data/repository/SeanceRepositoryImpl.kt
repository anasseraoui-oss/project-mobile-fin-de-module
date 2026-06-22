package com.elearning.app.data.repository

import com.elearning.app.data.local.db.SeanceDao
import com.elearning.app.data.local.entity.SeanceEntity
import com.elearning.app.data.remote.api.ResourceApiService
import com.elearning.app.data.remote.dto.PedagogicalResourceRequestDto
import com.elearning.app.data.remote.dto.ProgressUpdateDto
import com.elearning.app.data.remote.dto.SeanceTextContentRequestDto
import com.elearning.app.data.remote.dto.SeanceRequestDto
import com.elearning.app.data.remote.mapper.toDomain
import com.elearning.app.domain.model.PedagogicalResource
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.model.Seance
import com.elearning.app.domain.model.SeanceStatus
import com.elearning.app.domain.model.SeanceType
import com.elearning.app.domain.repository.SeanceRepository
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.util.UUID
import javax.inject.Inject

class SeanceRepositoryImpl @Inject constructor(
    private val api: ResourceApiService,
    private val seanceDao: SeanceDao
) : SeanceRepository {

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun getSeanceById(id: String): Seance {
        return try {
            val remote = api.getSeance(id).toDomain()
            val cachedFormationId = seanceDao.findById(id)?.formationId.orEmpty()
            seanceDao.upsert(remote.toEntity(cachedFormationId))
            remote
        } catch (e: Exception) {
            seanceDao.findById(id)?.toDomain() ?: throw e
        }
    }

    override suspend fun getStreamUrl(seanceId: String): String {
        return api.getStreamUrl(seanceId).streamUrl
    }

    override suspend fun getResources(seanceId: String): List<PedagogicalResource> {
        return api.getSeanceResources(seanceId).map { it.toDomain() }
    }

    override suspend fun getResourceDownloadUrl(resourceId: String): String {
        return api.getResourceDownloadUrl(resourceId).url
    }

    override suspend fun uploadVideo(seanceId: String, video: MultipartBody.Part): Result<Unit> = runCatchingResult {
        val response = api.uploadSeanceVideo(seanceId, video)
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun deleteVideo(seanceId: String): Result<Unit> = runCatchingResult {
        val response = api.deleteSeanceVideo(seanceId)
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun updateTextContent(seanceId: String, content: String): Result<Unit> = runCatchingResult {
        val response = api.updateSeanceTextContent(seanceId, SeanceTextContentRequestDto(content))
        if (!response.isSuccessful) throw HttpException(response)
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

    override suspend fun uploadResource(seanceId: String, title: String, file: MultipartBody.Part): Result<PedagogicalResource> =
        runCatchingResult {
            val data = gson.toJson(PedagogicalResourceRequestDto(title = title))
                .toRequestBody(jsonMediaType)
            api.uploadSeanceResource(seanceId, data, file).toDomain()
        }

    override suspend fun replaceResource(resourceId: String, title: String, file: MultipartBody.Part): Result<PedagogicalResource> =
        runCatchingResult {
            val data = gson.toJson(PedagogicalResourceRequestDto(title = title))
                .toRequestBody(jsonMediaType)
            api.replaceResource(resourceId, data, file).toDomain()
        }

    override suspend fun deleteResource(resourceId: String): Result<Unit> = runCatchingResult {
        val response = api.deleteResource(resourceId)
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun saveProgress(seanceId: String, progressSeconds: Int) {
        val cached = seanceDao.findById(seanceId)
        val duration = cached?.durationSeconds ?: 0
        val completed = duration > 0 && progressSeconds >= (duration * 0.9f).toInt()
        seanceDao.updateProgress(seanceId, progressSeconds, completed)
        api.updateProgress(
            seanceId,
            ProgressUpdateDto(
                watchedSeconds = progressSeconds,
                completed = completed
            )
        )
    }

    override suspend fun getLocalVideoPath(seanceId: String): String? {
        return seanceDao.findById(seanceId)?.localVideoPath
    }

    private fun Seance.toEntity(formationIdValue: String) = SeanceEntity(
        id = id.toString(),
        courseId = courseId.toString(),
        formationId = formationIdValue,
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

    private fun SeanceEntity.toDomain() = Seance(
        id = UUID.fromString(id),
        courseId = UUID.fromString(courseId),
        title = title,
        description = description,
        type = runCatching { SeanceType.valueOf(type) }.getOrDefault(SeanceType.VIDEO),
        durationSeconds = durationSeconds,
        orderIndex = orderIndex,
        status = runCatching { SeanceStatus.valueOf(status) }.getOrDefault(SeanceStatus.PLANIFIEE),
        videoKey = videoKey,
        pdfKey = null,
        thumbnailUrl = null,
        meetingLink = meetingLink,
        scheduledAt = scheduledAt,
        isCompleted = isCompleted,
        progressSeconds = progressSeconds
    )

    private suspend inline fun <T> runCatchingResult(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.Success(block())
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage)
        }
    }
}
