package com.quizzgame.core.flags

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.result.NetworkResult
import io.github.avtobot22.featureflags.FeatureFlagsSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class ExecutorFeatureFlagsSource<S : Any>(
    private val executor: ApiCallExecutor,
    private val service: Class<S>,
    private val request: suspend S.() -> ResponseBody,
) : FeatureFlagsSource {
    override suspend fun loadJson(): String = withContext(Dispatchers.IO) {
        when (val call = executor.call(service) { request().use { it.string() } }) {
            is ApiCallResult.Executed -> when (val result = call.result) {
                is NetworkResult.Success -> result.value
                is NetworkResult.Failure -> fail(FeatureFlagsLoadFailure.Network(result.reason))
            }
            is ApiCallResult.Unavailable -> fail(FeatureFlagsLoadFailure.Runtime(call.reason))
            ApiCallResult.Closed -> fail(FeatureFlagsLoadFailure.Closed)
        }
    }

    private fun fail(failure: FeatureFlagsLoadFailure): Nothing = throw FeatureFlagsSourceException(failure)
}
