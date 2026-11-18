package com.quizzgame.feature.feed.mobile.data

import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.classification.classifyApiUnavailable
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.result.NetworkFailure
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.feature.feed.mobile.data.api.MobileFeedApi
import com.quizzgame.shared.feed.data.FeedRemoteDataSource
import com.quizzgame.shared.feed.data.mapper.FeedMapper
import com.quizzgame.shared.feed.domain.entity.Feed
import com.quizzgame.shared.feed.domain.entity.FeedResult
import com.quizzgame.shared.feed.domain.entity.RailItems
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_GONE
import javax.inject.Inject

internal class FeedRemoteDataSourceImpl @Inject constructor(
    private val executor: ApiCallExecutor,
    private val session: SessionStateReader,
    private val dispatchers: DispatcherProvider,
    private val mapper: FeedMapper,
) : FeedRemoteDataSource {
    override val sectionPageSize: Int get() = MobileFeedApi.SECTION_LIMIT
    override val itemPageSize: Int get() = MobileFeedApi.ITEM_LIMIT
    override suspend fun load(cursor: String?, owner: SessionState?): FeedResult<Feed> = execute(owner) {
        val dto = load(POLICY, cursor = cursor)
        withContext(dispatchers.default) { mapper.map(dto) }
    }

    override suspend fun loadItems(sectionId: String, cursor: String, owner: SessionState): FeedResult<RailItems> =
        execute(owner) {
            val dto = items(sectionId, cursor, POLICY)
            withContext(dispatchers.default) { mapper.mapItems(dto, sectionId) }
        }

    private suspend fun <T> execute(owner: SessionState?, request: suspend MobileFeedApi.() -> T): FeedResult<T> {
        val current = session.current()
        if (owner != null && owner != current) return FeedResult.Failed(AppFailure.OperationInterrupted)
        val generation = owner ?: current
        val response = executor.call(MobileFeedApi::class.java, request)
        return if (session.current() != generation) {
            FeedResult.Failed(AppFailure.OperationInterrupted)
        } else {
            when (response) {
                is ApiCallResult.Executed -> when (val result = response.result) {
                    is NetworkResult.Success -> FeedResult.Loaded(result.value)
                    is NetworkResult.Failure -> classify(result.reason)
                }
                is ApiCallResult.Unavailable -> FeedResult.Failed(classifyApiUnavailable(response.reason))
                ApiCallResult.Closed -> FeedResult.Failed(AppFailure.OperationInterrupted)
            }
        }
    }

    private fun classify(reason: NetworkFailure): FeedResult<Nothing> = when {
        reason is NetworkFailure.Http && reason.status == HTTP_GONE -> FeedResult.Expired
        reason is NetworkFailure.Http && reason.status == HTTP_BAD_REQUEST ->
            FeedResult.Failed(AppFailure.InvalidServerData)
        else -> FeedResult.Failed(DefaultErrorClassifier().fromNetwork(reason))
    }
}

private val POLICY = RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE)
