package com.quizzgame.core.session.refresh.api

import org.junit.Assert.assertThrows
import org.junit.Test

class ReleaseSessionApiTest {

    @Test
    fun releaseSessionClasspathExcludesSessionAndTransportDiagnostics() {
        listOf(
            "com.quizzgame.core.session.refresh.api.RefreshFailure\$Diagnostic",
            "com.quizzgame.core.network.auth.RefreshFailureReason\$Diagnostic",
            "com.quizzgame.core.network.result.NetworkFailure\$MockConfiguration",
            "com.quizzgame.core.network.diagnostic.NetworkDiagnosticException",
            "com.quizzgame.core.network.diagnostic.MockFailureReason",
        ).forEach { name ->
            assertThrows(ClassNotFoundException::class.java) { Class.forName(name) }
        }
    }
}
