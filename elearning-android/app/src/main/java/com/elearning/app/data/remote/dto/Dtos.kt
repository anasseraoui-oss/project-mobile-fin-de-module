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
    @SerializedName("first_name")       val firstName: String,
    @SerializedName("last_name")        val lastName: String,
    val role: String,
    @SerializedName("avatar_url")       val avatarUrl: String?,
    @SerializedName("email_verified")   val isEmailVerified: Boolean
)

// ─── Formation ────────────────────────────────────────────────────────────────

data class FormationDto(
    val id: String,
    val title: String,
    val description: String,
    @SerializedName("thumbnail_url")    val thumbnailUrl: String?,
    val level: String,
    val language: String,
    val organisation: String,
    @SerializedName("duration_hours")   val durationHours: Int,
    val price: Double,
    val currency: String,
    val rating: Float,
    @SerializedName("enrollment_count") val enrollmentCount: Int,
    @SerializedName("course_count")     val courseCount: Int,
    val tags: List<String>,
    @SerializedName("is_enrolled")      val isEnrolled: Boolean,
    @SerializedName("progress_percent") val progressPercent: Int
)

// ─── Course ───────────────────────────────────────────────────────────────────

data class CourseDto(
    val id: String,
    @SerializedName("formation_id") val formationId: String,
    val title: String,
    val description: String?,
    @SerializedName("order_index")  val orderIndex: Int,
    val seances: List<SeanceDto>
)

// ─── Seance ───────────────────────────────────────────────────────────────────

data class SeanceDto(
    val id: String,
    @SerializedName("course_id")        val courseId: String,
    val title: String,
    val description: String?,
    val type: String,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    @SerializedName("order_index")      val orderIndex: Int,
    val status: String,
    @SerializedName("video_key")        val videoKey: String?,
    @SerializedName("meeting_link")     val meetingLink: String?,
    @SerializedName("scheduled_at")     val scheduledAt: String?,
    @SerializedName("is_completed")     val isCompleted: Boolean,
    @SerializedName("progress_seconds") val progressSeconds: Int
)

// ─── Stream URL ───────────────────────────────────────────────────────────────

data class StreamUrlDto(
    @SerializedName("stream_url") val streamUrl: String,
    @SerializedName("expires_at") val expiresAt: String
)

// ─── Progress ─────────────────────────────────────────────────────────────────

data class ProgressUpdateDto(
    @SerializedName("seance_id")        val seanceId: String,
    @SerializedName("progress_seconds") val progressSeconds: Int,
    val completed: Boolean
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
    val questions: List<QuestionDto>
)

data class QuestionDto(
    val id: String,
    @SerializedName("quiz_id") val quizId: String,
    val text: String,
    val type: String,
    val points: Int,
    val options: List<QuestionOptionDto>
)

data class QuestionOptionDto(
    val id: String,
    @SerializedName("question_id") val questionId: String,
    val text: String,
    @SerializedName("is_correct") val isCorrect: Boolean
)

data class QuizSubmissionDto(
    @SerializedName("quiz_id") val quizId: String,
    val answers: List<AnswerSubmissionDto>
)

data class AnswerSubmissionDto(
    @SerializedName("question_id")       val questionId: String,
    @SerializedName("selected_option_ids") val selectedOptionIds: List<String>,
    @SerializedName("open_answer")       val openAnswer: String?
)

data class QuizResultDto(
    @SerializedName("attempt_id")      val attemptId: String,
    @SerializedName("quiz_id")         val quizId: String,
    val score: Int,
    @SerializedName("max_score")       val maxScore: Int,
    @SerializedName("is_passed")       val isPassed: Boolean,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    val answers: List<AnswerResultDto>,
    @SerializedName("certificate_url") val certificateUrl: String?
)

data class AnswerResultDto(
    @SerializedName("question_id")         val questionId: String,
    @SerializedName("selected_option_ids") val selectedOptionIds: List<String>,
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
    val content: List<T>,
    @SerializedName("total_elements") val totalElements: Long,
    @SerializedName("total_pages")    val totalPages: Int,
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
