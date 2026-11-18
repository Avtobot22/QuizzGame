package com.quizzgame.feature.quizentry.tv.data

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.feature.quizentry.tv.data.api.TvQuizEntryApi
import com.quizzgame.shared.quizentry.data.QuizEntryRemoteDataSource
import javax.inject.Inject

internal class TvQuizEntryRemoteDataSource @Inject constructor(private val executor: ApiCallExecutor) :
    QuizEntryRemoteDataSource {
    override suspend fun load(entryId: String) = executor.call(TvQuizEntryApi::class.java) {
        load(entryId, RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE))
    }
}
