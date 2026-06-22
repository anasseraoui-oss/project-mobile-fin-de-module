package com.elearning.app.domain.repository

import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.Result

interface FavoritesRepository {
    suspend fun getFavorites(): Result<List<Formation>>
    suspend fun toggleFavorite(formationId: String): Result<Unit>
}
