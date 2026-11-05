package com.quizzgame.core.error.diagnostic

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.ConnectivityKind
import com.quizzgame.core.error.failure.ServiceKind
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.ErrorOperationKey
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.policy.ErrorDecisionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier
import java.util.UUID

class ErrorDiagnosticsTest {

    @Test
    fun `sink receives only a prepared safe diagnostic`() {
        val events = mutableListOf<ErrorDiagnostic>()
        val sink = ErrorDiagnostics { events += it }
        val diagnostic = ErrorDiagnostic.from(
            AppFailure.Connectivity(ConnectivityKind.TIMEOUT),
            ErrorOperationKey.from(Operations.FEATURE_LOAD),
        )
        sink.record(diagnostic)
        assertEquals(listOf(diagnostic), events)
    }

    @Test
    fun `no op accepts repeated events without retaining them or changing policy output`() {
        val policy: ErrorDecisionPolicy = DefaultErrorDecisionPolicy()
        val failure = AppFailure.Service(ServiceKind.UNAVAILABLE, 503)
        val context = ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT)
        val expected = policy.decide(failure, context)
        val operation = ErrorOperationKey.from(Operations.FEATURE_LOAD)
        val ids = listOf(null, UUID.fromString("9d07927a-a4ea-4ca2-884d-321c1eec2360"))
        ids.forEach { id ->
            val event = ErrorDiagnostic.from(failure, operation, id)
            repeat(10) { assertEquals(Unit, NoOpErrorDiagnostics.record(event)) }
            assertEquals(expected, policy.decide(failure, context))
        }
        assertTrue(NoOpErrorDiagnostics::class.java.declaredFields.all { Modifier.isStatic(it.modifiers) })
    }

    private enum class Operations {
        FEATURE_LOAD,
    }
}
