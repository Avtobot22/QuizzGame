package com.quizzgame.feature.quizentry.tv.domain

import com.quizzgame.feature.quizentry.tv.domain.entity.QuizEntryResult
import javax.inject.Inject

internal class LoadQuizEntryUseCase @Inject constructor(
    private val repository: QuizEntryRepository,
) : suspend (String) -> QuizEntryResult by repository::load
