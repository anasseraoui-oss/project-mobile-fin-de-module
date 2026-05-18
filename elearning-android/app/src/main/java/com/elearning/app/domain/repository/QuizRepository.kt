package com.elearning.app.domain.repository

import kotlinx.coroutines.flow.Flow

data class Quiz(
    val id: String,
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

interface QuizRepository {
    suspend fun getQuiz(quizId: String): Quiz
    suspend fun submitAnswers(quizId: String, answers: Map<String, String>): QuizResult
}
