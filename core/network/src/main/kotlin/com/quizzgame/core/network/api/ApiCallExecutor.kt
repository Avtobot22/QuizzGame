package com.quizzgame.core.network.api

import com.quizzgame.core.network.result.NetworkResult
import retrofit2.Response

interface ApiCallExecutor {
    suspend fun <S : Any, T> call(service: Class<S>, request: suspend S.() -> T): ApiCallResult<T>

    suspend fun <S : Any, T> response(service: Class<S>, request: suspend S.() -> Response<T>): ApiCallResult<T>
}

sealed interface ApiCallResult<out T> {
    data class Executed<T>(val result: NetworkResult<T>) : ApiCallResult<T>
    data class Unavailable(val reason: ApiUnavailableReason) : ApiCallResult<Nothing>
    data object Closed : ApiCallResult<Nothing>
}

enum class ApiUnavailableReason {
    STORAGE,
    CONFIGURATION,
    TRANSPORT,
}
