package com.elearning.app.data.remote.api

import com.elearning.app.data.remote.dto.FormationDto
import com.elearning.app.data.remote.dto.PagedResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ResourceApiService {
    @GET("api/v1/formations")
    suspend fun getFormations(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("search") search: String?,
        @Query("level") level: String?
    ): PagedResponse<FormationDto>
}
