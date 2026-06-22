package com.elearning.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ─── Token Response ───────────────────────────────────────────────────────────

data class TokenResponseDto(
    @SerializedName("access_token")  val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("token_type")    val tokenType: String?,
    @SerializedName("expires_in")    val expiresIn: Long?,
    @SerializedName("scope")         val scope: String?,
    @SerializedName("id_token")      val idToken: String?
)

// ─── Login / Register Request ─────────────────────────────────────────────────

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class GoogleLoginRequestDto(
    val idToken: String
)

data class RegisterRequestDto(
    val email: String,
    val password: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name")  val lastName: String
)

// ─── User ─────────────────────────────────────────────────────────────────────

data class UserDto(
    val id: String,
    val email: String,
    @SerializedName(value = "firstName", alternate = ["first_name"]) val firstName: String,
    @SerializedName(value = "lastName", alternate = ["last_name"]) val lastName: String,
    val role: String,
    @SerializedName(value = "avatarKey", alternate = ["avatar_url", "avatar_key"]) val avatarUrl: String?,
    @SerializedName(value = "isEmailVerified", alternate = ["email_verified", "emailVerified"]) val isEmailVerified: Boolean
)

data class UserProfileDto(
    val id: String,
    val email: String,
    @SerializedName(value = "firstName", alternate = ["first_name"]) val firstName: String,
    @SerializedName(value = "lastName", alternate = ["last_name"]) val lastName: String,
    @SerializedName(value = "fullName", alternate = ["full_name"]) val fullName: String?,
    val role: String,
    @SerializedName(value = "avatarUrl", alternate = ["avatar_url"]) val avatarUrl: String?,
    @SerializedName(value = "avatarKey", alternate = ["avatar_key"]) val avatarKey: String?,
    @SerializedName(value = "organisationId", alternate = ["organisation_id"]) val organisationId: String?,
    @SerializedName(value = "organisationName", alternate = ["organisation_name"]) val organisationName: String?,
    @SerializedName(value = "enrolledFormations", alternate = ["enrolled_formations"]) val enrolledFormations: Int?,
    @SerializedName(value = "completedFormations", alternate = ["completed_formations"]) val completedFormations: Int?,
    @SerializedName(value = "completedCourses", alternate = ["completed_courses"]) val completedCourses: Int?,
    @SerializedName(value = "certificatesCount", alternate = ["certificates_count"]) val certificatesCount: Int?,
    @SerializedName(value = "hoursSpent", alternate = ["hours_spent"]) val hoursSpent: Int?
)

// ─── Formation ────────────────────────────────────────────────────────────────

data class FormationDto(
    val id: String?,
    val title: String?,
    val description: String?,
    @SerializedName(value = "thumbnailUrl", alternate = ["thumbnail_url"]) val thumbnailUrl: String? = null,
    @SerializedName(value = "coverImageUrl", alternate = ["cover_image_url"]) val coverImageUrl: String? = null,
    @SerializedName(value = "thumbnailKey", alternate = ["thumbnail_key"]) val thumbnailKey: String? = null,
    @SerializedName(value = "coverImageKey", alternate = ["cover_image_key"]) val coverImageKey: String? = null,
    val level: String?,
    val language: String?,
    @SerializedName("organisationName") val organisation: String?,
    @SerializedName(value = "durationHours", alternate = ["duration_hours"]) val durationHours: Int?,
    @SerializedName(value = "totalDuration", alternate = ["total_duration"]) val totalDuration: Int? = null,
    @SerializedName(value = "previewVideoUrl", alternate = ["preview_video_url"]) val previewVideoUrl: String? = null,
    val price: Double?,
    val currency: String?,
    val rating: Float?,
    @SerializedName("enrolledCount")    val enrollmentCount: Int?,
    @SerializedName("coursesCount")     val courseCount: Int?,
    val tags: List<String>? = null,
    @SerializedName(value = "isEnrolled", alternate = ["is_enrolled"]) val isEnrolled: Boolean?,
    @SerializedName(value = "progressPercent", alternate = ["progress_percent"]) val progressPercent: Int?,
    val categoryId: String? = null,
    val prerequisites: List<String>? = null,
    val certified: Boolean? = null
)

data class FormationCategoryDto(
    val id: String,
    val title: String,
    val icon: String?,
    @SerializedName(value = "formationsCount", alternate = ["formations_count"]) val formationsCount: Int?
)

data class InstructorDashboardDto(
    val instructor: InstructorProfileSummaryDto,
    val stats: InstructorStatsDto
)

data class InstructorProfileSummaryDto(
    val id: String,
    @SerializedName(value = "fullName", alternate = ["full_name"]) val fullName: String?,
    val email: String?,
    @SerializedName(value = "avatarUrl", alternate = ["avatar_url"]) val avatarUrl: String?,
    val certificationStatus: String?,
    val levelLabel: String?,
    val organisationName: String?
)

data class InstructorStatsDto(
    val activeFormations: Int?,
    val totalLearners: Int?,
    val averageCompletionPercent: Int?,
    val monthlyRevenue: Double?,
    val monthlyRevenueCurrency: String?,
    val pendingActions: Int?
)

data class InstructorFormationSummaryDto(
    val id: String,
    val title: String?,
    val description: String?,
    val status: String?,
    val coverImageUrl: String?,
    val coursesCount: Int?,
    val seancesCount: Int?,
    val enrolledCount: Int?,
    val totalDuration: Int?,
    val updatedAt: String?
)

data class FormationRequestDto(
    val title: String,
    val description: String,
    val level: String,
    val language: String,
    val price: Double,
    val currency: String = "MAD",
    val categoryId: String? = null,
    val prerequisites: List<String> = emptyList(),
    val certified: Boolean = false
)

data class EnrollmentResponseDto(
    @SerializedName(value = "inscriptionId", alternate = ["inscription_id"]) val inscriptionId: String?,
    @SerializedName(value = "formationId", alternate = ["formation_id"]) val formationId: String?,
    val status: String?,
    @SerializedName(value = "enrolledAt", alternate = ["enrolled_at"]) val enrolledAt: String?,
    @SerializedName(value = "paymentRequired", alternate = ["payment_required"]) val paymentRequired: Boolean?
)

// ─── Course ───────────────────────────────────────────────────────────────────

data class CourseDto(
    val id: String,
    @SerializedName(value = "formationId", alternate = ["formation_id"]) val formationId: String,
    val title: String,
    val description: String?,
    @SerializedName(value = "orderIndex", alternate = ["order_index"]) val orderIndex: Int,
    val seances: List<SeanceDto>? = null
)

data class CourseRequestDto(
    val title: String,
    val description: String? = null,
    val orderIndex: Int,
    val estimatedDuration: Int? = null,
    val formationId: String,
    val status: String? = null
)

// ─── Seance ───────────────────────────────────────────────────────────────────

data class SeanceDto(
    val id: String,
    @SerializedName(value = "courseId", alternate = ["course_id"]) val courseId: String,
    val title: String,
    val description: String?,
    val type: String,
    @SerializedName(value = "durationSeconds", alternate = ["duration_seconds"]) val durationSeconds: Int?,
    @SerializedName(value = "orderIndex", alternate = ["order_index"]) val orderIndex: Int?,
    val status: String?,
    @SerializedName(value = "videoKey", alternate = ["video_key"]) val videoKey: String?,
    @SerializedName(value = "pdfKey", alternate = ["pdf_key"]) val pdfKey: String? = null,
    @SerializedName(value = "thumbnailUrl", alternate = ["thumbnail_url"]) val thumbnailUrl: String? = null,
    @SerializedName(value = "meetingLink", alternate = ["meeting_link"]) val meetingLink: String?,
    @SerializedName(value = "scheduledAt", alternate = ["scheduled_at"]) val scheduledAt: String?,
    @SerializedName(value = "isCompleted", alternate = ["is_completed"]) val isCompleted: Boolean?,
    @SerializedName(value = "progressSeconds", alternate = ["progress_seconds"]) val progressSeconds: Int?
)

data class SeanceRequestDto(
    val title: String,
    val description: String? = null,
    val type: String,
    val duration: Int? = null,
    val scheduledAt: String? = null,
    val meetingLink: String? = null,
    val orderIndex: Int,
    val status: String? = null
)

data class SeanceTextContentRequestDto(
    val content: String
)

// ─── Stream URL ───────────────────────────────────────────────────────────────

data class StreamUrlDto(
    @SerializedName(value = "stream_url", alternate = ["url"]) val streamUrl: String,
    @SerializedName(value = "expires_at", alternate = ["expiresAt"]) val expiresAt: String?
)

data class CoverUrlDto(
    @SerializedName(value = "url", alternate = ["cover_url", "coverUrl"]) val url: String
)

data class PedagogicalResourceDto(
    val id: String,
    @SerializedName(value = "formationId", alternate = ["formation_id"]) val formationId: String?,
    @SerializedName(value = "courseId", alternate = ["course_id"]) val courseId: String?,
    @SerializedName(value = "seanceId", alternate = ["seance_id"]) val seanceId: String?,
    val type: String?,
    val title: String?,
    @SerializedName(value = "fileName", alternate = ["file_name"]) val fileName: String?,
    @SerializedName(value = "objectKey", alternate = ["object_key", "fileKey", "file_key"]) val objectKey: String?,
    @SerializedName(value = "fileUrl", alternate = ["file_url", "download_url", "url"]) val fileUrl: String?,
    @SerializedName(value = "mimeType", alternate = ["mime_type"]) val mimeType: String?,
    @SerializedName(value = "sizeBytes", alternate = ["size_bytes", "fileSize", "file_size"]) val sizeBytes: Long?,
    @SerializedName(value = "isDownloadable", alternate = ["is_downloadable"]) val isDownloadable: Boolean?
)

data class PedagogicalResourceRequestDto(
    val title: String,
    val type: String = "FILE",
    val isDownloadable: Boolean = true
)

data class DownloadUrlDto(
    @SerializedName(value = "download_url", alternate = ["url", "downloadUrl"]) val url: String,
    @SerializedName(value = "expires_at", alternate = ["expiresAt"]) val expiresAt: String?
)

// ─── Progress ─────────────────────────────────────────────────────────────────

data class ProgressUpdateDto(
    val watchedSeconds: Int,
    val completed: Boolean
)

data class CourseQuizDto(
    val id: String,
    @SerializedName(value = "courseId", alternate = ["course_id"]) val courseId: String,
    val title: String,
    @SerializedName(value = "timeLimit", alternate = ["time_limit"]) val timeLimitSeconds: Int?,
    val questions: List<CourseQuizQuestionDto>? = null
)

data class CourseQuizQuestionDto(
    val id: String,
    @SerializedName(value = "question", alternate = ["text"]) val text: String,
    val reponses: List<CourseQuizOptionDto>? = null,
    val points: Int?
)

data class CourseQuizOptionDto(
    val id: String,
    val text: String
)

data class QuizSubmitRequestDto(
    val attemptId: String,
    val answers: Map<String, String>
)

data class BackendQuizResultDto(
    val score: Int?,
    val passed: Boolean?,
    @SerializedName(value = "attemptId", alternate = ["attempt_id"]) val attemptId: String?,
    @SerializedName(value = "remainingAttempts", alternate = ["remaining_attempts"]) val remainingAttempts: Int?
)

data class QuizHistoryItemDto(
    @SerializedName(value = "attemptId", alternate = ["attempt_id"]) val attemptId: String,
    @SerializedName(value = "quizId", alternate = ["quiz_id"]) val quizId: String,
    @SerializedName(value = "quizTitle", alternate = ["quiz_title"]) val quizTitle: String,
    @SerializedName(value = "courseId", alternate = ["course_id"]) val courseId: String?,
    @SerializedName(value = "courseTitle", alternate = ["course_title"]) val courseTitle: String?,
    @SerializedName(value = "formationId", alternate = ["formation_id"]) val formationId: String?,
    @SerializedName(value = "formationTitle", alternate = ["formation_title"]) val formationTitle: String?,
    @SerializedName(value = "submittedAt", alternate = ["submitted_at"]) val submittedAt: String?,
    val score: Double?,
    val status: String?,
    @SerializedName(value = "attemptNumber", alternate = ["attempt_number"]) val attemptNumber: Int?,
    val passed: Boolean?,
    @SerializedName(value = "certificateAvailable", alternate = ["certificate_available"]) val certificateAvailable: Boolean?
)

data class CertificateDto(
    val id: String,
    @SerializedName(value = "formationId", alternate = ["formation_id"]) val formationId: String?,
    @SerializedName(value = "formationTitle", alternate = ["formation_title"]) val formationTitle: String?,
    @SerializedName(value = "learnerName", alternate = ["learner_name"]) val learnerName: String?,
    @SerializedName(value = "issuedAt", alternate = ["issued_at"]) val issuedAt: String?,
    @SerializedName(value = "averageScore", alternate = ["average_score"]) val averageScore: Double?,
    val score: Int?,
    @SerializedName(value = "maxScore", alternate = ["max_score"]) val maxScore: Int?,
    @SerializedName(value = "pdfKey", alternate = ["pdf_key"]) val pdfKey: String?,
    @SerializedName(value = "downloadUrl", alternate = ["download_url", "certificateUrl", "certificate_url"]) val downloadUrl: String?,
    @SerializedName(value = "verificationCode", alternate = ["verification_code"]) val verificationCode: String?
)

// ─── Quiz ─────────────────────────────────────────────────────────────────────

data class QuizDto(
    val id: String,
    @SerializedName("seance_id")          val seanceId: String,
    val title: String,
    val description: String?,
    @SerializedName("duration_minutes")   val durationMinutes: Int,
    @SerializedName("max_attempts")       val maxAttempts: Int,
    @SerializedName("passing_score")      val passingScore: Int,
    val questions: List<QuestionDto>? = null
)

data class QuestionDto(
    val id: String,
    @SerializedName("quiz_id") val quizId: String,
    val text: String,
    val type: String,
    val points: Int,
    val options: List<QuestionOptionDto>? = null
)

data class QuestionOptionDto(
    val id: String,
    @SerializedName("question_id") val questionId: String,
    val text: String,
    @SerializedName("is_correct") val isCorrect: Boolean
)

data class QuizSubmissionDto(
    @SerializedName("quiz_id") val quizId: String,
    val answers: List<AnswerSubmissionDto>? = null
)

data class AnswerSubmissionDto(
    @SerializedName("question_id")       val questionId: String,
    @SerializedName("selected_option_ids") val selectedOptionIds: List<String>? = null,
    @SerializedName("open_answer")       val openAnswer: String?
)

data class QuizResultDto(
    @SerializedName("attempt_id")      val attemptId: String,
    @SerializedName("quiz_id")         val quizId: String,
    val score: Int,
    @SerializedName("max_score")       val maxScore: Int,
    @SerializedName("is_passed")       val isPassed: Boolean,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    val answers: List<AnswerResultDto>? = null,
    @SerializedName("certificate_url") val certificateUrl: String?
)

data class AnswerResultDto(
    @SerializedName("question_id")         val questionId: String,
    @SerializedName("selected_option_ids") val selectedOptionIds: List<String>? = null,
    @SerializedName("open_answer")         val openAnswer: String?,
    @SerializedName("is_correct")          val isCorrect: Boolean,
    @SerializedName("points_earned")       val pointsEarned: Int
)

// ─── Notification ─────────────────────────────────────────────────────────────

data class NotificationDto(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    @SerializedName(value = "deepLink", alternate = ["deep_link"]) val deepLink: String?,
    val data: String? = null,
    @SerializedName(value = "isRead", alternate = ["is_read"]) val isRead: Boolean,
    @SerializedName(value = "createdAt", alternate = ["created_at"]) val createdAt: String
)

// ─── Paged Response ───────────────────────────────────────────────────────────

data class PagedResponse<T>(
    val content: List<T>? = null,
    @SerializedName(value = "totalElements", alternate = ["total_elements"]) val totalElements: Long,
    @SerializedName(value = "totalPages", alternate = ["total_pages"]) val totalPages: Int,
    val number: Int,
    val size: Int,
    val last: Boolean
)

// ─── QR Code ──────────────────────────────────────────────────────────────────

data class QrTokenDto(
    @SerializedName("qr_token")  val qrToken: String,
    @SerializedName("expires_at") val expiresAt: String
)

data class AttendanceScanDto(
    @SerializedName("qr_token")  val qrToken: String,
    @SerializedName("seance_id") val seanceId: String
)
