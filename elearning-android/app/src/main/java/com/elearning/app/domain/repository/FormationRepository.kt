package com.elearning.app.domain.repository

import com.elearning.app.domain.model.Course
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.Result

interface FormationRepository {
    suspend fun getFormationById(id: String): Formation
    suspend fun getCoursesForFormation(formationId: String): List<Course>
    suspend fun enrollInFormation(formationId: String)
    suspend fun getFavoriteFormations(): Result<List<Formation>>
    suspend fun getRecommendedFormations(): Result<List<Formation>>
    suspend fun getEnrolledFormations(): Result<List<Formation>>
}
