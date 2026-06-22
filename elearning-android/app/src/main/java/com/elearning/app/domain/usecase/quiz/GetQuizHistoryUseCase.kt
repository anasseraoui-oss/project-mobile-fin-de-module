package com.elearning.app.domain.usecase.quiz

import com.elearning.app.domain.repository.QuizHistoryItem
import com.elearning.app.domain.repository.QuizRepository
import javax.inject.Inject

class GetQuizHistoryUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(): List<QuizHistoryItem> = repository.getQuizHistory()
}
