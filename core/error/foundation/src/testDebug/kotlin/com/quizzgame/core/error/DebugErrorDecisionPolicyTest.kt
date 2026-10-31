package com.quizzgame.core.error

import org.junit.Assert.assertEquals
import org.junit.Test

class DebugErrorDecisionPolicyTest {

    private val policy: ErrorDecisionPolicy = DefaultErrorDecisionPolicy()

    @Test
    fun debugDiagnosticsUseLocalMockCopyAndRespectRetrySafety() {
        val actions = mapOf(
            RetrySafety.SAFE_TO_REPEAT to listOf(ErrorActionKind.RETRY),
            RetrySafety.REQUIRES_CONFIRMATION to listOf(ErrorActionKind.CONFIRM_RETRY),
            RetrySafety.FORBIDDEN to emptyList(),
        )
        DevelopmentKind.entries.forEach { kind ->
            actions.forEach { (safety, expected) ->
                val context = ErrorOperationContext(OperationKind.INITIAL_LOAD, false, safety)
                assertEquals(
                    ErrorPresentationSpec(ErrorSurface.BLOCKING, ErrorCopyKey.MOCK_CONFIGURATION, actions = expected),
                    policy.decide(DevelopmentFailure(kind), context),
                )
            }
        }
    }

    @Test
    fun debugDiagnosticsPreserveContentAndDoNotInterruptOptionalPrefetch() {
        val failure = DevelopmentFailure(DevelopmentKind.MOCK_NO_MATCH)
        val refresh = ErrorOperationContext(OperationKind.USER_REFRESH, true, RetrySafety.SAFE_TO_REPEAT)
        val prefetch = ErrorOperationContext(OperationKind.PREFETCH, false, RetrySafety.SAFE_TO_REPEAT)
        assertEquals(ErrorSurface.BANNER, policy.decide(failure, refresh).surface)
        val silent = policy.decide(failure, prefetch)
        assertEquals(ErrorSurface.SILENT, silent.surface)
        assertEquals(emptyList<ErrorActionKind>(), silent.actions)
    }
}
