package com.quizzgame.core.error

import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorCopyPolicyTest {

    private val policy: ErrorDecisionPolicy = DefaultErrorDecisionPolicy()
    private val context = ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT)

    @Test
    fun `failure category selects local copy without guessing from HTTP or reason code`() {
        val cases = mapOf(
            AppFailure.Connectivity(ConnectivityKind.OFFLINE_OR_UNREACHABLE) to ErrorCopyKey.CONNECTION_PROBLEM,
            AppFailure.Connectivity(ConnectivityKind.TIMEOUT) to ErrorCopyKey.REQUEST_TIMED_OUT,
            AppFailure.Service(ServiceKind.UNAVAILABLE, 503) to ErrorCopyKey.SERVER_UNAVAILABLE,
            AppFailure.Service(ServiceKind.RATE_LIMITED, 429) to ErrorCopyKey.TOO_MANY_REQUESTS,
            AppFailure.Service(ServiceKind.ACCESS_DENIED, 403) to ErrorCopyKey.ACCESS_DENIED,
            AppFailure.Service(ServiceKind.CLIENT_REJECTED, 401) to ErrorCopyKey.GENERIC_FAILURE,
            AppFailure.Service(ServiceKind.OTHER_HTTP, 399) to ErrorCopyKey.GENERIC_FAILURE,
            AppFailure.Authentication(AuthenticationKind.REQUIRED) to ErrorCopyKey.SIGN_IN_REQUIRED,
            AppFailure.Authentication(AuthenticationKind.REVOKED) to ErrorCopyKey.SESSION_EXPIRED,
            AppFailure.InvalidServerData to ErrorCopyKey.UNEXPECTED_RESPONSE,
        )
        cases.forEach { (failure, copyKey) ->
            val spec = policy.decide(failure, context)
            assertEquals(failure.toString(), copyKey, spec.title)
            assertEquals(null, spec.message)
        }
        StorageKind.entries.forEach { kind ->
            assertEquals(ErrorCopyKey.STORAGE_PROBLEM, policy.decide(AppFailure.Storage(kind), context).title)
        }
    }

    @Test
    fun `status and unknown backend codes do not override semantic decision`() {
        val expected = policy.decide(AppFailure.Service(ServiceKind.UNAVAILABLE, 503), context)
        listOf(500, 503, 599).forEach { status ->
            listOf(null, 401, 403, 9001).forEach { code ->
                assertEquals(
                    expected,
                    policy.decide(AppFailure.Service(ServiceKind.UNAVAILABLE, status, code), context),
                )
            }
        }
    }
}
