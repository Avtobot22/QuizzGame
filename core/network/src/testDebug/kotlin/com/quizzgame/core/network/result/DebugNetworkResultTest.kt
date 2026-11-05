package com.quizzgame.core.network.result

import com.quizzgame.core.network.auth.RefreshFailedException
import com.quizzgame.core.network.auth.RefreshFailureReason
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.network.diagnostic.MockFailureReason
import com.quizzgame.core.network.diagnostic.NetworkDiagnosticException
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
