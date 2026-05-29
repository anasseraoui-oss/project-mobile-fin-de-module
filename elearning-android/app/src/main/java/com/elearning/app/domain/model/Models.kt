package com.elearning.app.domain.model

import java.util.UUID

// ──────────────────────────────────────────────────
// AUTH
// ──────────────────────────────────────────────────

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 3600L,
    val scope: String? = null
)

data class User(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val avatarUrl: String? = null,
    val isEmailVerified: Boolean = false
) {
    val fullName: String get() = "$firstName $lastName"
    val isAdmin: Boolean get() = role == UserRole.ADMIN
    val isFormateur: Boolean get() = role == UserRole.FORMATEUR
}

enum class UserRole { APPRENANT, FORMATEUR, ADMIN }

// ──────────────────────────────────────────────────
// FORMATION
// ──────────────────────────────────────────────────

data class Formation(
    val id: UUID,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
    val level: FormationLevel,
    val language: String,
    val organisation: String,
    val durationHours: Int,
    val price: Double,
    val currency: String = "MAD",
    val rating: Float = 0f,
    val enrollmentCount: Int = 0,
    val courseCount: Int = 0,
    val tags: List<String> = emptyList(),
    val isEnrolled: Boolean = false,
    val progressPercent: Int = 0
)

enum class FormationLevel { DEBUTANT, INTERMEDIAIRE, AVANCE }

// ──────────────────────────────────────────────────
// COURSE
// ──────────────────────────────────────────────────

data class Course(
    val id: UUID,
    val formationId: UUID,
    val title: String,
    val description: String?,
    val orderIndex: Int,
    val seances: List<Seance> = emptyList()
)

// ──────────────────────────────────────────────────
// SEANCE
// ──────────────────────────────────────────────────

data class Seance(
    val id: UUID,
    val courseId: UUID,
    val title: String,
    val description: String?,
    val type: SeanceType,
    val durationSeconds: Int,
    val orderIndex: Int,
    val status: SeanceStatus,
    val videoKey: String?,
    val pdfKey: String? = null,
    val thumbnailUrl: String? = null,
    val meetingLink: String? = null,
    val scheduledAt: String? = null,
    val isCompleted: Boolean = false,
    val progressSeconds: Int = 0
)

data class PedagogicalResource(
    val id: UUID,
    val seanceId: UUID?,
    val title: String,
    val fileName: String,
    val objectKey: String?,
    val fileUrl: String?,
    val mimeType: String,
    val sizeBytes: Long,
    val isDownloadable: Boolean
)

enum class SeanceType { VIDEO, LIVE, DOCUMENT, QUIZ }
enum class SeanceStatus { PLANIFIEE, EN_COURS, TERMINEE, ANNULEE }

// ──────────────────────────────────────────────────
// QUIZ
// ──────────────────────────────────────────────────

data class Quiz(
    val id: UUID,
    val seanceId: UUID,
    val title: String,
    val description: String?,
    val durationMinutes: Int,
    val maxAttempts: Int,
    val passingScore: Int,
    val questions: List<Question> = emptyList()
)

data class Question(
    val id: UUID,
    val quizId: UUID,
    val text: String,
    val type: QuestionType,
    val points: Int,
    val options: List<QuestionOption> = emptyList()
)

data class QuestionOption(
    val id: UUID,
    val questionId: UUID,
    val text: String,
    val isCorrect: Boolean = false // only populated after submission
)

enum class QuestionType { QCM, VRAI_FAUX, OUVERTE }

data class QuizResult(
    val attemptId: UUID,
    val quizId: UUID,
    val score: Int,
    val maxScore: Int,
    val isPassed: Boolean,
    val durationSeconds: Int,
    val answers: List<AnswerResult>,
    val certificateUrl: String? = null
)

data class AnswerResult(
    val questionId: UUID,
    val selectedOptionIds: List<UUID>,
    val openAnswer: String?,
    val isCorrect: Boolean,
    val pointsEarned: Int
)

// ──────────────────────────────────────────────────
// NOTIFICATIONS
// ──────────────────────────────────────────────────

data class AppNotification(
    val id: UUID,
    val title: String,
    val body: String,
    val type: NotificationType,
    val deepLink: String?,
    val isRead: Boolean = false,
    val createdAt: String
)

enum class NotificationType { COURS, QUIZ, CERTIFICAT, SYSTEM }

// ──────────────────────────────────────────────────
// COMMON WRAPPERS
// ──────────────────────────────────────────────────

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
}
