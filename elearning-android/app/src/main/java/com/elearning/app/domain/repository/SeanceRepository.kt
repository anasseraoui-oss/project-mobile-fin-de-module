package com.elearning.app.domain.repository

import com.elearning.app.domain.model.Seance

interface SeanceRepository {
    suspend fun getSeanceById(id: String): Seance
    suspend fun getStreamUrl(seanceId: String): String
    suspend fun saveProgress(seanceId: String, progressSeconds: Int)
    suspend fun getLocalVideoPath(seanceId: String): String?
}
