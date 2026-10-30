package com.quizzgame.core.network

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DebugNetworkResultTest {

    private val results = NetworkResultAdapter(createNetworkJson())

    @Test
    fun debugTransportDiagnosticsPreserveEveryMockReason() = runTest {
        MockFailureReason.entries.forEach { reason ->
            val diagnostic = NetworkFailure.MockConfiguration(reason)
            assertEquals(
                NetworkResult.Failure(diagnostic),
                results.call { throw NetworkDiagnosticException(diagnostic) },
            )
        }
    }

    @Test
    fun debugRefreshDiagnosticsPreserveEveryMockReason() = runTest {
        MockFailureReason.entries.forEach { reason ->
            val diagnostic = RefreshFailureReason.Diagnostic(NetworkFailure.MockConfiguration(reason))
            assertEquals(
                NetworkResult.Failure(NetworkFailure.RefreshFailed(diagnostic)),
                results.call { throw RefreshFailedException(diagnostic) },
            )
        }
    }
}
