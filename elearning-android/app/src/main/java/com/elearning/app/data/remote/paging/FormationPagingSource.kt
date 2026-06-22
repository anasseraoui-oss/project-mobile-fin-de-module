package com.elearning.app.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.elearning.app.data.remote.api.ResourceApiService
import com.elearning.app.data.remote.dto.FormationDto
import com.elearning.app.data.remote.mapper.toDomain
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.FormationLevel
import retrofit2.HttpException
import java.io.IOException

class FormationPagingSource(
    private val api: ResourceApiService,
    private val query: String,
    private val level: FormationLevel?,
    private val categoryId: String?
) : PagingSource<Int, Formation>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Formation> {
        val page = params.key ?: 0
        return try {
            val response = api.getFormations(
                page = page,
                size = params.loadSize,
                search = query.takeIf { it.isNotBlank() },
                level = level?.name,
                categoryId = categoryId
            )
            
            val formations = response.content.orEmpty().map { it.toDomainWithCoverUrl() }
            
            LoadResult.Page(
                data = formations,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (response.last) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Formation>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    private suspend fun FormationDto.toDomainWithCoverUrl(): Formation {
        val formation = toDomain()
        if (!formation.thumbnailUrl.isNullOrBlank()) return formation

        val coverUrl = id?.let { formationId ->
            runCatching { api.getFormationCoverUrl(formationId).url }.getOrNull()
        }
        return formation.copy(thumbnailUrl = coverUrl)
    }
}
