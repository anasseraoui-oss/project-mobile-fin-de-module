package com.elearning.app.domain.repository

import com.elearning.app.domain.model.Course
import com.elearning.app.domain.model.Formation

interface FormationRepository {
    suspend fun getFormationById(id: String): Formation
    suspend fun getCoursesForFormation(formationId: String): List<Course>
    suspend fun enrollInFormation(formationId: String)
}
