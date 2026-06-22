package com.elearning.app.data.repository

import android.util.Log
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.repository.FavoritesRepository
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor() : FavoritesRepository {

    override suspend fun getFavorites(): Result<List<Formation>> {
        Log.i(TAG, "Favorites endpoint is not available yet; returning an empty state.")
        return Result.Success(emptyList())
    }

    override suspend fun toggleFavorite(formationId: String): Result<Unit> {
        Log.i(TAG, "Favorite toggle endpoint is not available yet for formation=$formationId.")
        return Result.Success(Unit)
    }

    private companion object {
        private const val TAG = "FavoritesRepositoryImpl"
    }
}
