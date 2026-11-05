package com.quizzgame.core.network.diagnostic

import org.junit.Assert.assertThrows
import org.junit.Test

class ReleaseNetworkApiTest {

    @Test
    fun releaseNetworkClasspathExcludesDebugTransportDiagnostics() {
        listOf(
            "com.quizzgame.core.network.result.NetworkFailure\$MockConfiguration",
            "com.quizzgame.core.network.diagnostic.MockFailureReason",
            "com.quizzgame.core.network.diagnostic.NetworkDiagnosticException",
            "com.quizzgame.core.network.auth.RefreshFailureReason\$Diagnostic",
        ).forEach { name ->
            assertThrows(ClassNotFoundException::class.java) {
                Class.forName(name)
            }
        }
    }
}
