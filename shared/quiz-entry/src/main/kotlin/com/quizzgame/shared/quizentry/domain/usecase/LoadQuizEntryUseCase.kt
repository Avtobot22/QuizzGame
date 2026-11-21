package com.quizzgame.shared.quizentry.domain.usecase

import com.quizzgame.shared.quizentry.domain.repository.QuizEntryRepository
import javax.inject.Inject

class LoadQuizEntryUseCase @Inject constructor(private val repository: QuizEntryRepository) {

    suspend operator fun invoke(entryId: String) = repository.load(entryId)
}
