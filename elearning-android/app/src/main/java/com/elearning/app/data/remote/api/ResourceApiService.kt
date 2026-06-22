package com.elearning.app.data.remote.api

import com.elearning.app.data.remote.dto.FormationDto
import com.elearning.app.data.remote.dto.FormationCategoryDto
import com.elearning.app.data.remote.dto.FormationRequestDto
import com.elearning.app.data.remote.dto.InstructorDashboardDto
import com.elearning.app.data.remote.dto.InstructorFormationSummaryDto
import com.elearning.app.data.remote.dto.PagedResponse
import com.elearning.app.data.remote.dto.BackendQuizResultDto
import com.elearning.app.data.remote.dto.CertificateDto
import com.elearning.app.data.remote.dto.CourseDto
import com.elearning.app.data.remote.dto.CourseRequestDto
import com.elearning.app.data.remote.dto.CourseQuizDto
import com.elearning.app.data.remote.dto.DownloadUrlDto
import com.elearning.app.data.remote.dto.EnrollmentResponseDto
import com.elearning.app.data.remote.dto.PedagogicalResourceDto
import com.elearning.app.data.remote.dto.SeanceTextContentRequestDto
import com.elearning.app.data.remote.dto.CoverUrlDto
import com.elearning.app.data.remote.dto.ProgressUpdateDto
import com.elearning.app.data.remote.dto.QuizSubmitRequestDto
import com.elearning.app.data.remote.dto.QuizHistoryItemDto
import com.elearning.app.data.remote.dto.NotificationDto
import com.elearning.app.data.remote.dto.SeanceDto
import com.elearning.app.data.remote.dto.SeanceRequestDto
import com.elearning.app.data.remote.dto.StreamUrlDto
import com.elearning.app.data.remote.dto.UserDto
import com.elearning.app.data.remote.dto.UserProfileDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE

interface ResourceApiService {
    @GET("api/v1/formations")
    suspend fun getFormations(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("search") search: String?,
        @Query("level") level: String?,
        @Query("categoryId") categoryId: String? = null
    ): PagedResponse<FormationDto>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @GET("api/v1/users/me/profile")
    suspend fun getCurrentUserProfile(): UserProfileDto

    @GET("api/v1/categories")
    suspend fun getCategories(): List<FormationCategoryDto>

    @GET("api/v1/formateurs/me/dashboard")
    suspend fun getInstructorDashboard(): InstructorDashboardDto

    @GET("api/v1/formateurs/me/formations")
    suspend fun getInstructorFormations(
        @Query("status") status: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PagedResponse<InstructorFormationSummaryDto>

    @GET("api/v1/formations/{id}")
    suspend fun getFormation(@Path("id") id: String): FormationDto

    @Multipart
    @POST("api/v1/formations")
    suspend fun createFormation(
        @Part("data") data: RequestBody,
        @Part thumbnail: MultipartBody.Part? = null
    ): FormationDto

    @PUT("api/v1/formations/{id}")
    suspend fun updateFormation(
        @Path("id") id: String,
        @Body body: FormationRequestDto
    ): Response<Unit>

    @Multipart
    @PUT("api/v1/formations/{id}/cover")
    suspend fun updateFormationCover(
        @Path("id") id: String,
        @Part thumbnail: MultipartBody.Part
    ): FormationDto

    @POST("api/v1/formations/{id}/publish")
    suspend fun publishFormation(@Path("id") id: String): Response<Unit>

    @POST("api/v1/formations/{id}/archive")
    suspend fun archiveFormation(@Path("id") id: String): Response<Unit>

    @DELETE("api/v1/formations/{id}")
    suspend fun deleteFormation(@Path("id") id: String): Response<Unit>

    @GET("api/v1/formations/enrolled")
    suspend fun getEnrolledFormations(): List<FormationDto>

    @GET("api/v1/formations/{id}/cover-url")
    suspend fun getFormationCoverUrl(@Path("id") id: String): CoverUrlDto

    @GET("api/v1/formations/{formationId}/courses")
    suspend fun getCourses(@Path("formationId") formationId: String): List<CourseDto>

    @POST("api/v1/courses")
    suspend fun createCourse(@Body body: CourseRequestDto): CourseDto

    @PUT("api/v1/courses/{id}")
    suspend fun updateCourse(
        @Path("id") id: String,
        @Body body: CourseRequestDto
    ): CourseDto

    @DELETE("api/v1/courses/{id}")
    suspend fun deleteCourse(@Path("id") id: String): Response<Unit>

    @GET("api/v1/courses/{courseId}/seances")
    suspend fun getSeances(@Path("courseId") courseId: String): List<SeanceDto>

    @Multipart
    @POST("api/v1/courses/{courseId}/seances")
    suspend fun createSeance(
        @Path("courseId") courseId: String,
        @Part("data") data: RequestBody,
        @Part video: MultipartBody.Part? = null
    ): SeanceDto

    @PUT("api/v1/seances/{id}")
    suspend fun updateSeance(
        @Path("id") id: String,
        @Body body: SeanceRequestDto
    ): SeanceDto

    @DELETE("api/v1/seances/{id}")
    suspend fun deleteSeance(@Path("id") id: String): Response<Unit>

    @GET("api/v1/seances/{id}")
    suspend fun getSeance(@Path("id") id: String): SeanceDto

    @GET("api/v1/seances/{id}/stream-url")
    suspend fun getStreamUrl(@Path("id") id: String): StreamUrlDto

    @Multipart
    @POST("api/v1/seances/{id}/video")
    suspend fun uploadSeanceVideo(
        @Path("id") id: String,
        @Part video: MultipartBody.Part
    ): Response<Unit>

    @DELETE("api/v1/seances/{id}/video")
    suspend fun deleteSeanceVideo(@Path("id") id: String): Response<Unit>

    @PUT("api/v1/seances/{id}/text-content")
    suspend fun updateSeanceTextContent(
        @Path("id") id: String,
        @Body body: SeanceTextContentRequestDto
    ): Response<Unit>

    @GET("api/v1/seances/{id}/pdf-url")
    suspend fun getPdfUrl(@Path("id") id: String): DownloadUrlDto

    @GET("api/v1/seances/{seanceId}/resources")
    suspend fun getSeanceResources(@Path("seanceId") seanceId: String): List<PedagogicalResourceDto>

    @Multipart
    @POST("api/v1/seances/{seanceId}/resources")
    suspend fun uploadSeanceResource(
        @Path("seanceId") seanceId: String,
        @Part("data") data: RequestBody,
        @Part file: MultipartBody.Part
    ): PedagogicalResourceDto

    @DELETE("api/v1/resources/{id}")
    suspend fun deleteResource(@Path("id") id: String): Response<Unit>

    @Multipart
    @PUT("api/v1/resources/{id}")
    suspend fun replaceResource(
        @Path("id") id: String,
        @Part("data") data: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): PedagogicalResourceDto

    @GET("api/v1/resources/{id}/download-url")
    suspend fun getResourceDownloadUrl(@Path("id") id: String): DownloadUrlDto

    @PATCH("api/v1/progress/{seanceId}")
    suspend fun updateProgress(
        @Path("seanceId") seanceId: String,
        @Body body: ProgressUpdateDto
    )

    @GET("api/v1/courses/{courseId}/quiz")
    suspend fun getCourseQuiz(@Path("courseId") courseId: String): CourseQuizDto

    @POST("api/v1/quizzes/{quizId}/start")
    suspend fun startQuiz(@Path("quizId") quizId: String): Response<Map<String, String>>

    @POST("api/v1/quizzes/{quizId}/submit")
    suspend fun submitQuiz(
        @Path("quizId") quizId: String,
        @Body body: QuizSubmitRequestDto
    ): BackendQuizResultDto

    @GET("api/v1/quizzes/history")
    suspend fun getQuizHistory(): List<QuizHistoryItemDto>

    @POST("api/v1/formations/{id}/enroll")
    suspend fun enrollInFormation(@Path("id") id: String): Response<EnrollmentResponseDto>

    @GET("api/v1/me/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): PagedResponse<NotificationDto>

    @PUT("api/v1/notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: String): Response<Unit>

    @POST("api/v1/devices/token")
    suspend fun updateFcmToken(@Body body: Map<String, String>): Response<Unit>

    @GET("api/v1/certificats/me")
    suspend fun getCertificates(): List<CertificateDto>

    @GET("api/v1/certificats/{id}/download")
    suspend fun getCertificateDownloadUrl(@Path("id") id: String): DownloadUrlDto
}
