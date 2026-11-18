package com.quizzgame.feature.quizentry.mobile.data

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.feature.quizentry.mobile.data.api.MobileQuizEntryApi
import com.quizzgame.shared.quizentry.data.QuizEntryRemoteDataSource
import javax.inject.Inject

internal class MobileQuizEntryRemoteDataSource @Inject constructor(private val executor: ApiCallExecutor) :
    QuizEntryRemoteDataSource {
    override suspend fun load(entryId: String) = executor.call(MobileQuizEntryApi::class.java) {
        load(entryId, RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE))
    }
}
