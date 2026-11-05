package com.quizzgame.app.network

import com.quizzgame.app.graph.AppGraph
import com.quizzgame.app.startup.AppStartup
import com.quizzgame.app.startup.AppStartupFailure
import com.quizzgame.app.startup.GraphAccessResult
import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.api.ApiRuntime
import com.quizzgame.core.network.api.ApiUnavailableReason
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import retrofit2.Response
import java.util.concurrent.RejectedExecutionException

internal class StartupApiCallExecutor(private val startup: AppStartup) : ApiCallExecutor {
    override suspend fun <S : Any, T> call(service: Class<S>, request: suspend S.() -> T): ApiCallResult<T> =
        withRuntime { runtime -> runtime.results.call { request(runtime.retrofit.create(service)) } }

    override suspend fun <S : Any, T> response(
        service: Class<S>,
        request: suspend S.() -> Response<T>,
    ): ApiCallResult<T> =
        withRuntime { runtime -> runtime.results.response { request(runtime.retrofit.create(service)) } }

    private suspend fun <T> withRuntime(request: suspend (ApiRuntime) -> NetworkResult<T>): ApiCallResult<T> {
        currentCoroutineContext().ensureActive()
        return when (val access = startup.accessGraph()) {
            GraphAccessResult.Closed -> ApiCallResult.Closed
            is GraphAccessResult.Unavailable -> ApiCallResult.Unavailable(access.failure.reason.toApiReason())
            is GraphAccessResult.Ready -> execute(access.graph, request)
        }
    }

    private suspend fun <T> execute(
        graph: AppGraph,
        request: suspend (ApiRuntime) -> NetworkResult<T>,
    ): ApiCallResult<T> {
        val initial = unavailable(graph)
        return if (initial != null) {
            initial
        } else {
            val result = try {
                ApiCallResult.Executed(request(graph.network.api))
            } catch (rejected: RejectedExecutionException) {
                currentCoroutineContext().ensureActive()
                if (!graph.network.isClosed) throw rejected
                ApiCallResult.Closed
            }
            currentCoroutineContext().ensureActive()
            unavailable(graph) ?: result
        }
    }

    private fun unavailable(graph: AppGraph): ApiCallResult<Nothing>? = when {
        graph.network.isClosed || graph.session.state.value == SessionState.Closed -> ApiCallResult.Closed
        else -> null
    }
}

private fun AppStartupFailure.toApiReason(): ApiUnavailableReason = when (this) {
    is AppStartupFailure.Storage -> ApiUnavailableReason.STORAGE
    AppStartupFailure.NetworkConfiguration -> ApiUnavailableReason.CONFIGURATION
    AppStartupFailure.Transport -> ApiUnavailableReason.TRANSPORT
}
