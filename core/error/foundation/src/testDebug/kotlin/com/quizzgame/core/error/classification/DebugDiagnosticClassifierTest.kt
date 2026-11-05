package com.quizzgame.core.error.classification

import com.quizzgame.core.error.failure.DevelopmentFailure
import com.quizzgame.core.error.failure.DevelopmentKind
import com.quizzgame.core.network.auth.RefreshFailureReason
import com.quizzgame.core.network.diagnostic.MockFailureReason
import com.quizzgame.core.network.result.NetworkFailure
import org.junit.Assert.assertEquals
import org.junit.Test

class DebugDiagnosticClassifierTest {

    private val classifier: ErrorClassifier = DefaultErrorClassifier()
    private val cases = mapOf(
        MockFailureReason.NO_MATCHING_RULE to DevelopmentKind.MOCK_NO_MATCH,
        MockFailureReason.INVALID_REQUEST to DevelopmentKind.MOCK_INVALID_REQUEST,
        MockFailureReason.CALLBACK_FAILURE to DevelopmentKind.MOCK_CALLBACK,
        MockFailureReason.INVALID_RESPONSE to DevelopmentKind.MOCK_INVALID_RESPONSE,
    )

    @Test
    fun debugMockFailuresPreserveEveryDevelopmentCause() {
        assertEquals(MockFailureReason.entries.toSet(), cases.keys)
        cases.forEach { (reason, kind) ->
            assertEquals(
                DevelopmentFailure(kind),
                classifier.fromNetwork(NetworkFailure.MockConfiguration(reason)),
            )
        }
    }

    @Test
    fun debugRefreshDiagnosticsPreserveEveryDevelopmentCause() {
        cases.forEach { (reason, kind) ->
            assertEquals(
                DevelopmentFailure(kind),
                classifier.fromNetwork(
                    NetworkFailure.RefreshFailed(
                        RefreshFailureReason.Diagnostic(NetworkFailure.MockConfiguration(reason)),
                    ),
                ),
            )
        }
    }
}
