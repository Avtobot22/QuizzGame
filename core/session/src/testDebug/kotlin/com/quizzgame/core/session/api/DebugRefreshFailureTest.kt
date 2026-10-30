package com.quizzgame.core.session.api

import com.quizzgame.core.network.MockFailureReason
import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.network.RefreshFailureReason
import org.junit.Assert.assertEquals
import org.junit.Test

class DebugRefreshFailureTest {

    @Test
    fun debugRefreshPreservesEveryTransportDiagnosticThroughBothMappings() {
        MockFailureReason.entries.forEach { reason ->
            val network = NetworkFailure.MockConfiguration(reason)
            val refresh = network.toRefreshFailure(cancelled = false)
            assertEquals(RefreshFailure.Diagnostic(network), refresh)
            assertEquals(RefreshFailureReason.Diagnostic(network), refresh.toRefreshFailureReason())
        }
    }
}
