package com.elearning.app.data.repository

import com.elearning.app.domain.model.*
import com.elearning.app.domain.repository.FormationRepository
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

class MockFormationRepositoryImpl @Inject constructor() : FormationRepository {
    override suspend fun getFormationById(id: String): Formation {
        delay(500)
        return Formation(
            id = UUID.fromString(id),
            title = "Mock Formation",
            description = "Mock description",
            thumbnailUrl = null,
            level = FormationLevel.DEBUTANT,
            language = "Français",
            organisation = "Mock Org",
            durationHours = 10,
            price = 0.0
        )
    }

    override suspend fun getCoursesForFormation(formationId: String): List<Course> {
        delay(500)
        return listOf(
            Course(
                id = UUID.randomUUID(),
                formationId = java.util.UUID.fromString(formationId),
                title = "Mock Course 1",
                description = "Mock Description",
                orderIndex = 1,
                seances = emptyList()
            )
        )
    }

    override suspend fun enrollInFormation(formationId: String) {
        delay(500)
    }
}
