package com.elearning.app.data.remote.mapper

import com.elearning.app.data.remote.dto.*
import com.elearning.app.domain.model.*
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

// ─── Formation ────────────────────────────────────────────────────────────────

fun FormationDto.toDomain() = Formation(
    id = id?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
    title = title ?: "Sans titre",
    description = description ?: "",
    thumbnailUrl = thumbnailUrl,
    level = runCatching { FormationLevel.valueOf(level ?: "DEBUTANT") }.getOrDefault(FormationLevel.DEBUTANT),
    language = language ?: "Français",
    organisation = organisation ?: "Organisation inconnue",
    durationHours = durationHours ?: 0,
    price = price ?: 0.0,
    currency = currency ?: "MAD",
    rating = rating ?: 0f,
    enrollmentCount = enrollmentCount ?: 0,
    courseCount = courseCount ?: 0,
    tags = tags ?: emptyList(),
    isEnrolled = isEnrolled ?: false,
    progressPercent = progressPercent ?: 0
)

// ─── Course ───────────────────────────────────────────────────────────────────

fun CourseDto.toDomain() = Course(
    id = UUID.fromString(id),
    formationId = UUID.fromString(formationId),
    title = title,
    description = description,
    orderIndex = orderIndex,
    seances = seances.map { it.toDomain() }
)

// ─── Seance ───────────────────────────────────────────────────────────────────

fun SeanceDto.toDomain() = Seance(
    id = UUID.fromString(id),
    courseId = UUID.fromString(courseId),
    title = title,
    description = description,
    type = SeanceType.valueOf(type),
    durationSeconds = durationSeconds,
    orderIndex = orderIndex,
    status = SeanceStatus.valueOf(status),
    videoKey = videoKey,
    meetingLink = meetingLink,
    scheduledAt = scheduledAt,
    isCompleted = isCompleted,
    progressSeconds = progressSeconds
)

// ─── Quiz ─────────────────────────────────────────────────────────────────────

fun QuizDto.toDomain() = Quiz(
    id = UUID.fromString(id),
    seanceId = UUID.fromString(seanceId),
    title = title,
    description = description,
    durationMinutes = durationMinutes,
    maxAttempts = maxAttempts,
    passingScore = passingScore,
    questions = questions.map { it.toDomain() }
)

fun QuestionDto.toDomain() = Question(
    id = UUID.fromString(id),
    quizId = UUID.fromString(quizId),
    text = text,
    type = QuestionType.valueOf(type),
    points = points,
    options = options.map { it.toDomain() }
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
    answers = answers.map { it.toDomain() },
    certificateUrl = certificateUrl
)

fun AnswerResultDto.toDomain() = AnswerResult(
    questionId = UUID.fromString(questionId),
    selectedOptionIds = selectedOptionIds.map { UUID.fromString(it) },
    openAnswer = openAnswer,
    isCorrect = isCorrect,
    pointsEarned = pointsEarned
)

// ─── Notification ─────────────────────────────────────────────────────────────

fun NotificationDto.toDomain() = AppNotification(
    id = UUID.fromString(id),
    title = title,
    body = body,
    type = NotificationType.valueOf(type),
    deepLink = deepLink,
    isRead = isRead,
    createdAt = createdAt
)
