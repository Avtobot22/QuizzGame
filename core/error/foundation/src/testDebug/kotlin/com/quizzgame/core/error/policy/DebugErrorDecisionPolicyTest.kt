package com.quizzgame.core.error.policy

import com.quizzgame.core.error.failure.DevelopmentFailure
import com.quizzgame.core.error.failure.DevelopmentKind
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
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
