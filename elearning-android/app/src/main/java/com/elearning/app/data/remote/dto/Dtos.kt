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
    @SerializedName(value = "progressPercent", alternate = ["progress_percent"]) val progressPercent: Int?
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
    val answers: Map<String, String>
)

data class BackendQuizResultDto(
    val score: Int?,
    val passed: Boolean?,
    @SerializedName(value = "attemptId", alternate = ["attempt_id"]) val attemptId: String?,
    @SerializedName(value = "remainingAttempts", alternate = ["remaining_attempts"]) val remainingAttempts: Int?
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
    @SerializedName("deep_link") val deepLink: String?,
    @SerializedName("is_read")   val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String
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
