package com.quizzgame.feature.quizentry.tv.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.feature.quizentry.tv.data.dto.QuizEntryDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Tag

internal interface TvQuizEntryApi {
    @GET("api/tv/quiz-entries/{entryId}")
    suspend fun load(@Path("entryId") entryId: String, @Tag policy: RequestPolicy): QuizEntryDto
}
