package com.quizzgame.core.error.ui.tv.policy

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.ConnectivityKind
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorSurface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TvErrorDecisionPolicyTest {
    private val policy = TvErrorDecisionPolicy()
    private val failure = AppFailure.Connectivity(ConnectivityKind.TIMEOUT)

    @Test
    fun commandErrorsStayNearTheActionOrAlongsideExistingContent() {
        listOf(false to ErrorSurface.INLINE, true to ErrorSurface.BANNER).forEach { (content, surface) ->
            val result = policy.decide(failure, context(OperationKind.USER_COMMAND, content))
            assertEquals(surface, result.surface)
            assertEquals(listOf(ErrorActionKind.RETRY, ErrorActionKind.DISMISS), result.actions)
        }
    }

    @Test
    fun confirmationAndForbiddenRetryContractsArePreserved() {
        val confirmation = policy.decide(
            failure,
            context(OperationKind.USER_COMMAND, true).copy(retrySafety = RetrySafety.REQUIRES_CONFIRMATION),
        )
        assertEquals(listOf(ErrorActionKind.CONFIRM_RETRY, ErrorActionKind.DISMISS), confirmation.actions)
        val forbidden = policy.decide(
            failure,
            context(OperationKind.USER_COMMAND, true).copy(retrySafety = RetrySafety.FORBIDDEN),
        )
        assertEquals(listOf(ErrorActionKind.DISMISS), forbidden.actions)
    }

    @Test
    fun loadingRefreshAuthenticationAndBackgroundKeepCommonDecisions() {
        val common = DefaultErrorDecisionPolicy()
        OperationKind.entries.filter { it != OperationKind.USER_COMMAND }.forEach { operation ->
            listOf(false, true).forEach { content ->
                val context = context(operation, content)
                assertEquals(common.decide(failure, context), policy.decide(failure, context))
            }
        }
    }

    @Test
    fun tvNeverRoutesAnErrorToATransientPopup() {
        OperationKind.entries.forEach { operation ->
            listOf(false, true).forEach { content ->
                assertNotEquals(ErrorSurface.TRANSIENT, policy.decide(failure, context(operation, content)).surface)
            }
        }
    }

    private fun context(operation: OperationKind, content: Boolean) =
        ErrorOperationContext(operation, content, RetrySafety.SAFE_TO_REPEAT)
}
