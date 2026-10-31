package com.quizzgame.core.error

import com.quizzgame.core.network.MockFailureReason
import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.network.RefreshFailureReason
import org.junit.Assert.assertEquals
import org.junit.Test

class DebugErrorDiagnosticTest {

    @Test
    fun debugDirectAndRefreshDiagnosticsRetainOnlySafeMockCategory() {
        val classifier: ErrorClassifier = DefaultErrorClassifier()
        val operation = ErrorOperationKey.from(Operations.FEATURE_LOAD)
        val categories = mapOf(
            MockFailureReason.NO_MATCHING_RULE to ErrorDiagnosticCategory.MOCK_NO_MATCH,
            MockFailureReason.INVALID_REQUEST to ErrorDiagnosticCategory.MOCK_INVALID_REQUEST,
            MockFailureReason.CALLBACK_FAILURE to ErrorDiagnosticCategory.MOCK_CALLBACK,
            MockFailureReason.INVALID_RESPONSE to ErrorDiagnosticCategory.MOCK_INVALID_RESPONSE,
        )
        assertEquals(MockFailureReason.entries.toSet(), categories.keys)
        categories.forEach { (reason, category) ->
            val direct = NetworkFailure.MockConfiguration(reason)
            val refresh = NetworkFailure.RefreshFailed(RefreshFailureReason.Diagnostic(direct))
            listOf(direct, refresh).forEach { source ->
                assertEquals(
                    ErrorDiagnostic(operation, category),
                    ErrorDiagnostic.from(classifier.fromNetwork(source), operation),
                )
            }
        }
    }

    private enum class Operations {
        FEATURE_LOAD,
    }
}
