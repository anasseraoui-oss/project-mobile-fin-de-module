package com.elearning.app.domain.repository

import com.elearning.app.domain.model.Seance
import com.elearning.app.domain.model.PedagogicalResource

interface SeanceRepository {
    suspend fun getSeanceById(id: String): Seance
    suspend fun getStreamUrl(seanceId: String): String
    suspend fun getResources(seanceId: String): List<PedagogicalResource>
    suspend fun getResourceDownloadUrl(resourceId: String): String
    suspend fun saveProgress(seanceId: String, progressSeconds: Int)
    suspend fun getLocalVideoPath(seanceId: String): String?
}
