package com.elearning.app.data.repository

import com.elearning.app.domain.repository.*
import kotlinx.coroutines.delay
import javax.inject.Inject

class MockQuizRepositoryImpl @Inject constructor() : QuizRepository {
    override suspend fun getQuiz(quizId: String): Quiz {
        delay(500)
        return Quiz(
            id = quizId,
            title = "Mock Quiz",
            durationMinutes = 10,
            questions = listOf(
                Question("1", "Question 1", listOf(Option("1", "A"), Option("2", "B"))),
                Question("2", "Question 2", listOf(Option("3", "C"), Option("4", "D")))
            )
        )
    }

    override suspend fun submitAnswers(quizId: String, answers: Map<String, String>): QuizResult {
        delay(500)
        return QuizResult(
            score = 100,
            maxScore = 100,
            passed = true,
            certificateUrl = null
        )
    }
}
