package com.quizzgame.shared.quizentry.domain

import javax.inject.Inject

/** Загружает подробности без запуска игры или смены викторины на другом устройстве. */
class LoadQuizEntryUseCase @Inject constructor(private val repository: QuizEntryRepository) {
    suspend operator fun invoke(entryId: String) = repository.load(entryId)
}
