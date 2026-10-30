package com.quizzgame.core.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ErrorDecisionPolicyTest {

    private val policy: ErrorDecisionPolicy = DefaultErrorDecisionPolicy()
    private val unavailable = AppFailure.Service(ServiceKind.UNAVAILABLE, 503)

    @Test
    fun `same 503 blocks empty load preserves refresh content and suppresses prefetch`() {
        val load = policy.decide(unavailable, context(OperationKind.INITIAL_LOAD, false))
        val refresh = policy.decide(unavailable, context(OperationKind.USER_REFRESH, true))
        val prefetch = policy.decide(unavailable, context(OperationKind.PREFETCH, false))

        assertEquals(
            ErrorPresentationSpec(
                ErrorSurface.BLOCKING,
                ErrorCopyKey.SERVER_UNAVAILABLE,
                actions = listOf(ErrorActionKind.RETRY),
            ),
            load,
        )
        assertEquals(
            ErrorPresentationSpec(
                ErrorSurface.BANNER,
                ErrorCopyKey.SERVER_UNAVAILABLE,
                actions = listOf(ErrorActionKind.RETRY, ErrorActionKind.DISMISS),
            ),
            refresh,
        )
        assertEquals(ErrorPresentationSpec(ErrorSurface.SILENT, ErrorCopyKey.SERVER_UNAVAILABLE), prefetch)
    }

    @Test
    fun `operation matrix covers all surfaces with and without usable content`() {
        val expected = mapOf(
            OperationKind.INITIAL_LOAD to (ErrorSurface.BLOCKING to ErrorSurface.BANNER),
            OperationKind.USER_REFRESH to (ErrorSurface.BLOCKING to ErrorSurface.BANNER),
            OperationKind.USER_COMMAND to (ErrorSurface.TRANSIENT to ErrorSurface.TRANSIENT),
            OperationKind.AUTH_FLOW to (ErrorSurface.INLINE to ErrorSurface.INLINE),
            OperationKind.BACKGROUND to (ErrorSurface.SILENT to ErrorSurface.SILENT),
            OperationKind.PREFETCH to (ErrorSurface.SILENT to ErrorSurface.SILENT),
        )
        assertEquals(OperationKind.entries.toSet(), expected.keys)
        expected.forEach { (operation, surfaces) ->
            assertEquals(operation.name, surfaces.first, policy.decide(unavailable, context(operation, false)).surface)
            assertEquals(operation.name, surfaces.second, policy.decide(unavailable, context(operation, true)).surface)
        }
    }

    @Test
    fun `only dismissible surfaces offer dismiss and no navigation is invented`() {
        OperationKind.entries.forEach { operation ->
            listOf(false, true).forEach { content ->
                val spec = policy.decide(unavailable, context(operation, content))
                assertEquals(
                    spec.surface in setOf(ErrorSurface.BANNER, ErrorSurface.TRANSIENT),
                    ErrorActionKind.DISMISS in spec.actions,
                )
                assertFalse(
                    spec.actions.any {
                        it in setOf(ErrorActionKind.GO_BACK, ErrorActionKind.EXIT, ErrorActionKind.OPEN_SETTINGS)
                    },
                )
            }
        }
    }

    @Test
    fun `optional background errors never offer authentication or recovery actions`() {
        val failures = listOf(
            unavailable,
            AppFailure.Connectivity(ConnectivityKind.TIMEOUT),
            AppFailure.Authentication(AuthenticationKind.REVOKED),
            AppFailure.Storage(StorageKind.CRYPTO),
        )
        listOf(OperationKind.BACKGROUND, OperationKind.PREFETCH).forEach { operation ->
            failures.forEach { failure ->
                RetrySafety.entries.forEach { safety ->
                    val spec = policy.decide(failure, ErrorOperationContext(operation, false, safety))
                    assertEquals(ErrorSurface.SILENT, spec.surface)
                    assertEquals(emptyList<ErrorActionKind>(), spec.actions)
                }
            }
        }
    }

    @Test
    fun `same inputs always produce same decision`() {
        val context = context(OperationKind.USER_REFRESH, true)
        val first = policy.decide(unavailable, context)
        repeat(10) { assertEquals(first, DefaultErrorDecisionPolicy().decide(unavailable, context)) }
    }

    private fun context(operation: OperationKind, content: Boolean): ErrorOperationContext =
        ErrorOperationContext(operation, content, RetrySafety.SAFE_TO_REPEAT)
}
