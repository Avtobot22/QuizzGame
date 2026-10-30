package com.quizzgame.core.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ErrorRecoveryPolicyTest {

    private val policy: ErrorDecisionPolicy = DefaultErrorDecisionPolicy()

    @Test
    fun `recoverable failure requires safe repeat or explicit confirmation`() {
        val failures = listOf(
            AppFailure.Connectivity(ConnectivityKind.OFFLINE_OR_UNREACHABLE),
            AppFailure.Connectivity(ConnectivityKind.TIMEOUT),
            AppFailure.Service(ServiceKind.UNAVAILABLE, 503),
            AppFailure.Service(ServiceKind.RATE_LIMITED, 429),
        )
        val expected = mapOf(
            RetrySafety.SAFE_TO_REPEAT to listOf(ErrorActionKind.RETRY),
            RetrySafety.REQUIRES_CONFIRMATION to listOf(ErrorActionKind.CONFIRM_RETRY),
            RetrySafety.FORBIDDEN to emptyList(),
        )
        failures.forEach { failure ->
            expected.forEach { (safety, actions) ->
                val context = ErrorOperationContext(OperationKind.INITIAL_LOAD, false, safety)
                assertEquals("$failure, $safety", actions, policy.decide(failure, context).actions)
            }
        }
    }

    @Test
    fun `potentially completed command offers confirmation rather than immediate repeat`() {
        val spec = policy.decide(
            AppFailure.Connectivity(ConnectivityKind.TIMEOUT),
            ErrorOperationContext(OperationKind.USER_COMMAND, true, RetrySafety.REQUIRES_CONFIRMATION),
        )
        assertEquals(ErrorSurface.TRANSIENT, spec.surface)
        assertEquals(listOf(ErrorActionKind.CONFIRM_RETRY, ErrorActionKind.DISMISS), spec.actions)
    }

    @Test
    fun `authentication offers sign in as a capability independently of repeat safety`() {
        AuthenticationKind.entries.forEach { kind ->
            RetrySafety.entries.forEach { safety ->
                val context = ErrorOperationContext(OperationKind.AUTH_FLOW, true, safety)
                val spec = policy.decide(AppFailure.Authentication(kind), context)
                assertEquals(ErrorSurface.INLINE, spec.surface)
                assertEquals(listOf(ErrorActionKind.SIGN_IN), spec.actions)
            }
        }
    }

    @Test
    fun `permanent failures do not offer retry or sign in even when repeat is safe`() {
        val failures = listOf(
            AppFailure.Service(ServiceKind.ACCESS_DENIED, 403),
            AppFailure.Service(ServiceKind.CLIENT_REJECTED, 401),
            AppFailure.Service(ServiceKind.CLIENT_REJECTED, 422),
            AppFailure.Service(ServiceKind.OTHER_HTTP, 399),
            AppFailure.InvalidServerData,
        ) + StorageKind.entries.map(AppFailure::Storage)
        failures.forEach { failure ->
            RetrySafety.entries.forEach { safety ->
                val context = ErrorOperationContext(OperationKind.USER_COMMAND, true, safety)
                assertEquals(listOf(ErrorActionKind.DISMISS), policy.decide(failure, context).actions)
            }
        }
    }

    @Test
    fun `forbidden safety never offers either retry action on any surface`() {
        val failures = listOf(
            AppFailure.Connectivity(ConnectivityKind.TIMEOUT),
            AppFailure.Service(ServiceKind.UNAVAILABLE, 503),
            AppFailure.Authentication(AuthenticationKind.REQUIRED),
        )
        OperationKind.entries.forEach { operation ->
            listOf(false, true).forEach { content ->
                failures.forEach { failure ->
                    val context = ErrorOperationContext(operation, content, RetrySafety.FORBIDDEN)
                    val actions = policy.decide(failure, context).actions
                    assertFalse(ErrorActionKind.RETRY in actions)
                    assertFalse(ErrorActionKind.CONFIRM_RETRY in actions)
                }
            }
        }
    }
}
