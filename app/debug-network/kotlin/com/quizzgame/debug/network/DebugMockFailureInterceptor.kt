package com.quizzgame.debug.network

import com.quizzgame.core.network.MockFailureReason
import com.quizzgame.core.network.NetworkDiagnosticException
import com.quizzgame.core.network.NetworkFailure
import dev.androidmock.okhttp.server.MockServerFailureProtocol
import okhttp3.Interceptor
import okhttp3.Response

enum class DebugMockFailure {
    NO_MATCHING_RULE,
    INVALID_REQUEST,
    CALLBACK_FAILURE,
    INVALID_RESPONSE,
}

class DebugMockTransportException(val failure: DebugMockFailure) : NetworkDiagnosticException(
    NetworkFailure.MockConfiguration(MockFailureReason.valueOf(failure.name)),
)

internal class DebugMockFailureInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val failure = when (MockServerFailureProtocol.failureName(response)) {
            "NoMatchingRule" -> DebugMockFailure.NO_MATCHING_RULE
            "InvalidRequest" -> DebugMockFailure.INVALID_REQUEST
            "CallbackFailure" -> DebugMockFailure.CALLBACK_FAILURE
            "InvalidResponse" -> DebugMockFailure.INVALID_RESPONSE
            else -> return response
        }
        response.close()
        throw DebugMockTransportException(failure)
    }
}
