package com.elearning.app.data.remote.mapper

import com.elearning.app.data.remote.dto.*
import com.elearning.app.domain.model.*
import kotlin.math.ceil
import java.util.UUID

// ─── Auth Tokens ─────────────────────────────────────────────────────────────

fun TokenResponseDto.toDomain() = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken ?: "",
    tokenType = tokenType ?: "Bearer",
    expiresIn = expiresIn ?: 3600L,
    scope = scope
)

// ─── User ─────────────────────────────────────────────────────────────────────

fun UserDto.toDomain() = User(
    id = UUID.fromString(id),
    email = email,
    firstName = firstName,
    lastName = lastName,
    role = UserRole.valueOf(role),
    avatarUrl = avatarUrl,
    isEmailVerified = isEmailVerified
)

fun UserProfileDto.toDomain() = UserProfile(
    id = UUID.fromString(id),
    email = email,
    firstName = firstName,
    lastName = lastName,
    fullName = fullName ?: "$firstName $lastName",
    role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.APPRENANT),
    avatarUrl = avatarUrl,
    organisationName = organisationName,
    hoursSpent = hoursSpent ?: 0,
    completedCourses = completedCourses ?: 0,
    completedFormations = completedFormations ?: 0,
    enrolledFormations = enrolledFormations ?: 0,
    certificatesCount = certificatesCount ?: 0
)

// ─── Formation ────────────────────────────────────────────────────────────────

fun FormationDto.toDomain() = Formation(
    id = UUID.fromString(requireNotNull(id) { "Formation id is missing from backend response" }),
    title = title ?: "Sans titre",
    description = description ?: "",
    thumbnailUrl = coverImageUrl ?: thumbnailUrl,
    level = runCatching { FormationLevel.valueOf(level ?: "DEBUTANT") }.getOrDefault(FormationLevel.DEBUTANT),
    language = language ?: "Français",
    organisation = organisation ?: "Organisation inconnue",
    durationHours = durationHours ?: totalDuration.toHours(),
    price = price ?: 0.0,
    currency = currency ?: "MAD",
    rating = rating ?: 0f,
    enrollmentCount = enrollmentCount ?: 0,
    courseCount = courseCount ?: 0,
    tags = tags ?: emptyList(),
    isEnrolled = isEnrolled ?: false,
    progressPercent = progressPercent ?: 0,
    categoryId = categoryId,
    prerequisites = prerequisites.orEmpty(),
    certified = certified ?: false
)

fun FormationCategoryDto.toDomain() = FormationCategory(
    id = id,
    title = title,
    icon = icon,
    formationsCount = formationsCount ?: 0
)

fun InstructorDashboardDto.toDomain() = InstructorDashboard(
    instructor = instructor.toDomain(),
    stats = stats.toDomain()
)

fun InstructorProfileSummaryDto.toDomain() = InstructorProfileSummary(
    id = UUID.fromString(id),
    fullName = fullName ?: email.orEmpty(),
    email = email.orEmpty(),
    avatarUrl = avatarUrl,
    certificationStatus = certificationStatus ?: "CERTIFIED",
    levelLabel = levelLabel ?: "Formateur",
    organisationName = organisationName
)

fun InstructorStatsDto.toDomain() = InstructorStats(
    activeFormations = activeFormations ?: 0,
    totalLearners = totalLearners ?: 0,
    averageCompletionPercent = averageCompletionPercent ?: 0,
    monthlyRevenue = monthlyRevenue ?: 0.0,
    monthlyRevenueCurrency = monthlyRevenueCurrency ?: "MAD",
    pendingActions = pendingActions ?: 0
)

fun InstructorFormationSummaryDto.toDomain() = InstructorFormationSummary(
    id = UUID.fromString(id),
    title = title ?: "Sans titre",
    description = description.orEmpty(),
    status = status ?: "BROUILLON",
    coverImageUrl = coverImageUrl,
    coursesCount = coursesCount ?: 0,
    seancesCount = seancesCount ?: 0,
    enrolledCount = enrolledCount ?: 0,
    totalDuration = totalDuration ?: 0,
    updatedAt = updatedAt
)

// ─── Course ───────────────────────────────────────────────────────────────────

fun CourseDto.toDomain() = Course(
    id = UUID.fromString(id),
    formationId = UUID.fromString(formationId),
    title = title,
    description = description,
    orderIndex = orderIndex,
    seances = seances.orEmpty().map { it.toDomain() }
)

// ─── Seance ───────────────────────────────────────────────────────────────────

fun SeanceDto.toDomain() = Seance(
    id = UUID.fromString(id),
    courseId = UUID.fromString(courseId),
    title = title,
    description = description,
    type = when (type) {
        "ENREGISTREE" -> SeanceType.VIDEO
        else -> runCatching { SeanceType.valueOf(type) }.getOrDefault(SeanceType.VIDEO)
    },
    durationSeconds = durationSeconds ?: 0,
    orderIndex = orderIndex ?: 0,
    status = when (status) {
        "CONTENU_DISPONIBLE" -> SeanceStatus.TERMINEE
        else -> runCatching { SeanceStatus.valueOf(status ?: "PLANIFIEE") }.getOrDefault(SeanceStatus.PLANIFIEE)
    },
    videoKey = videoKey,
    pdfKey = pdfKey,
    thumbnailUrl = thumbnailUrl,
    meetingLink = meetingLink,
    scheduledAt = scheduledAt,
    isCompleted = isCompleted ?: false,
    progressSeconds = progressSeconds ?: 0
)

fun PedagogicalResourceDto.toDomain() = PedagogicalResource(
    id = UUID.fromString(id),
    seanceId = seanceId?.let { runCatching { UUID.fromString(it) }.getOrNull() },
    title = title ?: fileName ?: "Fichier",
    fileName = fileName ?: title ?: objectKey?.substringAfterLast('/') ?: "fichier",
    objectKey = objectKey,
    fileUrl = fileUrl,
    mimeType = mimeType ?: "application/octet-stream",
    sizeBytes = sizeBytes ?: 0L,
    isDownloadable = isDownloadable ?: true
)

private fun Int?.toHours(): Int {
    if (this == null || this <= 0) return 0
    return maxOf(1, ceil(this / 60.0).toInt())
}

// ─── Quiz ─────────────────────────────────────────────────────────────────────

fun QuizDto.toDomain() = Quiz(
    id = UUID.fromString(id),
    seanceId = UUID.fromString(seanceId),
    title = title,
    description = description,
    durationMinutes = durationMinutes,
    maxAttempts = maxAttempts,
    passingScore = passingScore,
    questions = questions.orEmpty().map { it.toDomain() }
)

fun QuestionDto.toDomain() = Question(
    id = UUID.fromString(id),
    quizId = UUID.fromString(quizId),
    text = text,
    type = QuestionType.valueOf(type),
    points = points,
    options = options.orEmpty().map { it.toDomain() }
)

fun QuestionOptionDto.toDomain() = QuestionOption(
    id = UUID.fromString(id),
    questionId = UUID.fromString(questionId),
    text = text,
    isCorrect = isCorrect
)

fun QuizResultDto.toDomain() = QuizResult(
    attemptId = UUID.fromString(attemptId),
    quizId = UUID.fromString(quizId),
    score = score,
    maxScore = maxScore,
    isPassed = isPassed,
    durationSeconds = durationSeconds,
    answers = answers.orEmpty().map { it.toDomain() },
    certificateUrl = certificateUrl
)

fun AnswerResultDto.toDomain() = AnswerResult(
    questionId = UUID.fromString(questionId),
    selectedOptionIds = selectedOptionIds.orEmpty().map { UUID.fromString(it) },
    openAnswer = openAnswer,
    isCorrect = isCorrect,
    pointsEarned = pointsEarned
)

// ─── Notification ─────────────────────────────────────────────────────────────

fun NotificationDto.toDomain() = AppNotification(
    id = runCatching { UUID.fromString(id) }.getOrElse { UUID.randomUUID() },
    title = title,
    body = body,
    type = runCatching { NotificationType.valueOf(type.uppercase()) }.getOrDefault(NotificationType.SYSTEM),
    deepLink = deepLink ?: data.extractNotificationDeepLink(),
    isRead = isRead,
    createdAt = createdAt
)

private fun String?.extractNotificationDeepLink(): String? {
    if (this.isNullOrBlank()) return null
    val patterns = listOf(
        """"deepLink"\s*:\s*"([^"]+)"""".toRegex(),
        """"deep_link"\s*:\s*"([^"]+)"""".toRegex()
    )
    return patterns.firstNotNullOfOrNull { pattern ->
        pattern.find(this)?.groupValues?.getOrNull(1)
    }
}
