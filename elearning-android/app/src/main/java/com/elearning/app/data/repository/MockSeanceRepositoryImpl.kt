package com.elearning.app.data.repository

import com.elearning.app.domain.model.Seance
import com.elearning.app.domain.model.SeanceStatus
import com.elearning.app.domain.model.SeanceType
import com.elearning.app.domain.repository.SeanceRepository
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

class MockSeanceRepositoryImpl @Inject constructor() : SeanceRepository {
    override suspend fun getSeanceById(id: String): Seance {
        delay(500)
        return Seance(
            id = UUID.fromString(id),
            courseId = UUID.randomUUID(),
            title = "Mock Seance",
            description = "Mock description",
            type = SeanceType.VIDEO,
            durationSeconds = 600,
            orderIndex = 1,
            status = SeanceStatus.EN_COURS,
            videoKey = null,
            meetingLink = null,
            scheduledAt = null
        )
    }

    override suspend fun getStreamUrl(seanceId: String): String {
        delay(500)
        return "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    }

    override suspend fun saveProgress(seanceId: String, progressSeconds: Int) {
        delay(100)
    }

    override suspend fun getLocalVideoPath(seanceId: String): String? {
        return null
    }
}
