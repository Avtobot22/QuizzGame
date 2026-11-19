package com.quizzgame.shared.auth.data

import com.quizzgame.core.error.classification.classifyApiUnavailable
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.shared.auth.data.mapper.toAuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthResult

internal val authPolicy = RequestPolicy(AuthPolicy.NONE, ReplayPolicy.NEVER)

internal suspend fun <S : Any, D, T> ApiCallExecutor.executeAuthRequest(
    service: Class<S>,
    request: suspend S.() -> D,
    map: (D) -> T,
): AuthResult<T> =
    when (val call = call(service, request)) {
        is ApiCallResult.Executed -> when (val result = call.result) {
            is NetworkResult.Failure -> AuthResult.Failed(result.reason.toAuthFailure())
            is NetworkResult.Success -> try {
                AuthResult.Success(map(result.value))
            } catch (_: IllegalArgumentException) {
                AuthResult.Failed(AuthFailure.General(AppFailure.InvalidServerData))
            }
        }
        is ApiCallResult.Unavailable -> AuthResult.Failed(AuthFailure.General(classifyApiUnavailable(call.reason)))
        ApiCallResult.Closed -> AuthResult.Failed(AuthFailure.General(AppFailure.OperationInterrupted))
    }
