package com.quizzgame.shared.quizentry.data

import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.shared.catalog.data.dto.entry.QuizEntryDto

fun interface QuizEntryRemoteDataSource {

    suspend fun load(entryId: String): ApiCallResult<QuizEntryDto>
}
