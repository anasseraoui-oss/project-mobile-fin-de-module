package com.elearning.app.domain.repository

import kotlinx.coroutines.flow.Flow

data class Quiz(
    val id: String,
    val attemptId: String,
    val title: String,
    val durationMinutes: Int,
    val questions: List<Question>
)

data class Question(
    val id: String,
    val text: String,
    val options: List<Option>
)

data class Option(
    val id: String,
    val text: String
)

data class QuizResult(
    val score: Int,
    val maxScore: Int,
    val passed: Boolean,
    val certificateUrl: String?
)

data class QuizHistoryItem(
    val attemptId: String,
    val quizId: String,
    val quizTitle: String,
    val courseTitle: String?,
    val formationTitle: String?,
    val submittedAt: String?,
    val score: Int,
    val passed: Boolean,
    val attemptNumber: Int,
    val certificateAvailable: Boolean
)

interface QuizRepository {
    suspend fun getQuiz(quizId: String): Quiz
    suspend fun submitAnswers(quizId: String, attemptId: String, answers: Map<String, String>): QuizResult
    suspend fun getQuizHistory(): List<QuizHistoryItem>
}
