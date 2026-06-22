package com.elearning.app.data.repository

import android.util.Log
import com.elearning.app.data.remote.api.ResourceApiService
import com.elearning.app.data.remote.dto.CourseQuizDto
import com.elearning.app.data.remote.dto.QuizSubmitRequestDto
import com.elearning.app.domain.repository.Option
import com.elearning.app.domain.repository.Question
import com.elearning.app.domain.repository.Quiz
import com.elearning.app.domain.repository.QuizHistoryItem
import com.elearning.app.domain.repository.QuizRepository
import com.elearning.app.domain.repository.QuizResult
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val api: ResourceApiService
) : QuizRepository {

    override suspend fun getQuiz(quizId: String): Quiz {
        Log.d(TAG, "Fetching quiz info for quizId=$quizId")
        val quiz = api.getCourseQuiz(quizId)
        Log.d(TAG, "Starting quiz session for quizId=${quiz.id}")
        val startResponse = api.startQuiz(quiz.id)
        val attemptId = if (startResponse.isSuccessful) {
            val id = startResponse.body()?.get("attemptId") ?: startResponse.body()?.get("tentativeId") ?: ""
            Log.d(TAG, "Quiz session started successfully. attemptId=$id")
            id
        } else {
            Log.w(TAG, "Quiz session start failed for quiz=${quiz.id}: HTTP ${startResponse.code()}")
            ""
        }
        return quiz.toDomain(attemptId)
    }

    override suspend fun submitAnswers(quizId: String, attemptId: String, answers: Map<String, String>): QuizResult {
        Log.d(TAG, "Submitting quiz answers. quizId=$quizId, attemptId=$attemptId, answersCount=${answers.size}")
        val result = api.submitQuiz(quizId, QuizSubmitRequestDto(attemptId = attemptId, answers = answers))
        Log.d(TAG, "Quiz submission result: score=${result.score}, passed=${result.passed}")
        return QuizResult(
            score = result.score ?: 0,
            maxScore = MAX_SCORE_PERCENT,
            passed = result.passed ?: false,
            certificateUrl = null
        )
    }

    override suspend fun getQuizHistory(): List<QuizHistoryItem> {
        Log.d(TAG, "Fetching quiz history")
        return api.getQuizHistory().map { item ->
            QuizHistoryItem(
                attemptId = item.attemptId,
                quizId = item.quizId,
                quizTitle = item.quizTitle,
                courseTitle = item.courseTitle,
                formationTitle = item.formationTitle,
                submittedAt = item.submittedAt,
                score = item.score?.toInt() ?: 0,
                passed = item.passed ?: false,
                attemptNumber = item.attemptNumber ?: 1,
                certificateAvailable = item.certificateAvailable ?: false
            )
        }
    }

    private fun CourseQuizDto.toDomain(attemptId: String) = Quiz(
        id = id,
        attemptId = attemptId,
        title = title,
        durationMinutes = ((timeLimitSeconds ?: DEFAULT_TIME_LIMIT_SECONDS) / 60).coerceAtLeast(1),
        questions = questions.orEmpty()
            .map { question ->
                Question(
                    id = question.id,
                    text = question.text,
                    options = question.reponses.orEmpty().map { option ->
                        Option(id = option.id, text = option.text)
                    }
                )
            }
    )

    private companion object {
        private const val TAG = "QuizRepositoryImpl"
        private const val DEFAULT_TIME_LIMIT_SECONDS = 1800
        private const val MAX_SCORE_PERCENT = 100
    }
}
