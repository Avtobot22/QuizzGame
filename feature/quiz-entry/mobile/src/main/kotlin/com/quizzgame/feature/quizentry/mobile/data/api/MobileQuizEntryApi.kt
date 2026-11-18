package com.quizzgame.feature.quizentry.mobile.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.shared.catalog.data.dto.entry.QuizEntryDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Tag

internal interface MobileQuizEntryApi {
    @GET("api/mobile/quiz-entries/{entryId}")
    suspend fun load(@Path("entryId") entryId: String, @Tag policy: RequestPolicy): QuizEntryDto
}
