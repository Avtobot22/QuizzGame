package com.quizzgame.core.network

import org.junit.Assert.assertThrows
import org.junit.Test

class ReleaseNetworkApiTest {

    @Test
    fun releaseNetworkClasspathExcludesDebugTransportDiagnostics() {
        listOf(
            "NetworkFailure\$MockConfiguration",
            "MockFailureReason",
            "NetworkDiagnosticException",
            "RefreshFailureReason\$Diagnostic",
        ).forEach { name ->
            assertThrows(ClassNotFoundException::class.java) {
                Class.forName("com.quizzgame.core.network.$name")
            }
        }
    }
}
