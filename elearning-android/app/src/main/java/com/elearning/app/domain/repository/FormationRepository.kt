package com.elearning.app.domain.repository

import com.elearning.app.domain.model.Course
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.FormationCategory
import com.elearning.app.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface FormationRepository {
    suspend fun getFormationById(id: String): Formation
    suspend fun getCoursesForFormation(formationId: String): List<Course>
    suspend fun enrollInFormation(formationId: String)
    suspend fun getFavoriteFormations(): Result<List<Formation>>
    suspend fun getRecommendedFormations(): Result<List<Formation>>
    suspend fun getEnrolledFormations(): Result<List<Formation>>
    fun observeEnrolledFormations(): Flow<List<Formation>>
    suspend fun refreshEnrolledFormations(): Result<List<Formation>>
    suspend fun getCategories(): Result<List<FormationCategory>>
}
