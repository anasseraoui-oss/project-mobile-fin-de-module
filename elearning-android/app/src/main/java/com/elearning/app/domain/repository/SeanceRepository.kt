package com.elearning.app.domain.repository

import com.elearning.app.domain.model.Seance
import com.elearning.app.domain.model.PedagogicalResource
import com.elearning.app.domain.model.Result
import okhttp3.MultipartBody

interface SeanceRepository {
    suspend fun getSeanceById(id: String): Seance
    suspend fun getStreamUrl(seanceId: String): String
    suspend fun getResources(seanceId: String): List<PedagogicalResource>
    suspend fun getResourceDownloadUrl(resourceId: String): String
    suspend fun uploadVideo(seanceId: String, video: MultipartBody.Part): Result<Unit>
    suspend fun deleteVideo(seanceId: String): Result<Unit>
    suspend fun updateTextContent(seanceId: String, content: String): Result<Unit>
    suspend fun updateSeance(seanceId: String, title: String, description: String?, type: String, durationSeconds: Int?, orderIndex: Int, status: String?): Result<Seance>
    suspend fun uploadResource(seanceId: String, title: String, file: MultipartBody.Part): Result<PedagogicalResource>
    suspend fun replaceResource(resourceId: String, title: String, file: MultipartBody.Part): Result<PedagogicalResource>
    suspend fun deleteResource(resourceId: String): Result<Unit>
    suspend fun saveProgress(seanceId: String, progressSeconds: Int)
    suspend fun getLocalVideoPath(seanceId: String): String?
}
