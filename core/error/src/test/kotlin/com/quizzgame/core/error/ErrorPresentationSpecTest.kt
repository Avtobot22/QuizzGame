package com.quizzgame.core.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ErrorPresentationSpecTest {

    @Test
    fun `silent presentation cannot carry interactive actions`() {
        ErrorActionKind.entries.forEach { action ->
            assertThrows(IllegalArgumentException::class.java) {
                ErrorPresentationSpec(ErrorSurface.SILENT, ErrorCopyKey.GENERIC_FAILURE, actions = listOf(action))
            }
        }
    }

    @Test
    fun `retry contracts cannot conflict or compete with sign in`() {
        val invalid = listOf(
            listOf(ErrorActionKind.RETRY, ErrorActionKind.CONFIRM_RETRY),
            listOf(ErrorActionKind.RETRY, ErrorActionKind.SIGN_IN),
            listOf(ErrorActionKind.CONFIRM_RETRY, ErrorActionKind.SIGN_IN),
            listOf(ErrorActionKind.DISMISS, ErrorActionKind.DISMISS),
        )
        invalid.forEach { actions ->
            assertThrows(IllegalArgumentException::class.java) {
                ErrorPresentationSpec(ErrorSurface.BANNER, ErrorCopyKey.GENERIC_FAILURE, actions = actions)
            }
        }
    }

    @Test
    fun `custom policy can supply distinct local title and message keys`() {
        val policy = ErrorDecisionPolicy { _, _ ->
            ErrorPresentationSpec(
                surface = ErrorSurface.INLINE,
                title = ErrorCopyKey.GENERIC_FAILURE,
                message = ErrorCopyKey.CONNECTION_PROBLEM,
                actions = listOf(ErrorActionKind.GO_BACK),
            )
        }
        val spec = policy.decide(
            AppFailure.Connectivity(ConnectivityKind.OFFLINE_OR_UNREACHABLE),
            ErrorOperationContext(OperationKind.USER_COMMAND, true, RetrySafety.FORBIDDEN),
        )
        assertEquals(ErrorCopyKey.CONNECTION_PROBLEM, spec.message)
        assertEquals(listOf(ErrorActionKind.GO_BACK), spec.actions)
    }
}
