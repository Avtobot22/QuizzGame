package com.quizzgame.feature.dashboard.mobile.data

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
import com.quizzgame.feature.dashboard.mobile.data.api.MobileDashboardApi
import com.quizzgame.feature.dashboard.mobile.data.mapper.DashboardMapper
import com.quizzgame.feature.dashboard.mobile.domain.DashboardRepository
import com.quizzgame.feature.dashboard.mobile.domain.entity.DashboardResult
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DashboardRepositoryImpl @Inject constructor(
    private val executor: ApiCallExecutor,
    private val session: SessionStateReader,
    private val dispatchers: DispatcherProvider,
    private val mapper: DashboardMapper,
) : DashboardRepository {
    override suspend fun load(): DashboardResult {
        val generation = session.current()
        val response = executor.call(MobileDashboardApi::class.java) {
            val dto = load(RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE))
            withContext(dispatchers.default) { mapper.map(dto) }
        }
        if (session.current() != generation) return DashboardResult.Failed(AppFailure.OperationInterrupted)
        return when (response) {
            is ApiCallResult.Executed -> when (val result = response.result) {
                is NetworkResult.Success -> DashboardResult.Loaded(result.value)
                is NetworkResult.Failure -> DashboardResult.Failed(DefaultErrorClassifier().fromNetwork(result.reason))
            }
            is ApiCallResult.Unavailable -> DashboardResult.Failed(classifyApiUnavailable(response.reason))
            ApiCallResult.Closed -> DashboardResult.Failed(AppFailure.OperationInterrupted)
        }
    }
}
