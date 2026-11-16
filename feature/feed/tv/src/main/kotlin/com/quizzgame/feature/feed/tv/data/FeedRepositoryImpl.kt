package com.quizzgame.feature.feed.tv.data

import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.classification.classifyApiUnavailable
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.feature.feed.tv.data.api.TvFeedApi
import com.quizzgame.feature.feed.tv.data.mapper.FeedMapper
import com.quizzgame.feature.feed.tv.domain.FeedRepository
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class FeedRepositoryImpl @Inject constructor(
    private val executor: ApiCallExecutor,
    private val session: SessionStateReader,
    private val dispatchers: DispatcherProvider,
    private val mapper: FeedMapper,
) : FeedRepository {
    override suspend fun load(): FeedResult {
        val generation = session.current()
        val response = executor.call(TvFeedApi::class.java) {
            val dto = load(RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE))
            withContext(dispatchers.default) { mapper.map(dto) }
        }
        if (session.current() != generation) return FeedResult.Failed(AppFailure.OperationInterrupted)
        return when (response) {
            is ApiCallResult.Executed -> when (val result = response.result) {
                is NetworkResult.Success -> FeedResult.Loaded(result.value)
                is NetworkResult.Failure -> FeedResult.Failed(DefaultErrorClassifier().fromNetwork(result.reason))
            }
            is ApiCallResult.Unavailable -> FeedResult.Failed(classifyApiUnavailable(response.reason))
            ApiCallResult.Closed -> FeedResult.Failed(AppFailure.OperationInterrupted)
        }
    }
}
