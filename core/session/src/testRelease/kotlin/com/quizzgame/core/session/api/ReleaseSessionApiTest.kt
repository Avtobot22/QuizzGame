package com.quizzgame.core.session.api

import org.junit.Assert.assertThrows
import org.junit.Test

class ReleaseSessionApiTest {

    @Test
    fun releaseSessionClasspathExcludesSessionAndTransportDiagnostics() {
        listOf(
            "com.quizzgame.core.session.api.RefreshFailure\$Diagnostic",
            "com.quizzgame.core.network.RefreshFailureReason\$Diagnostic",
            "com.quizzgame.core.network.NetworkFailure\$MockConfiguration",
            "com.quizzgame.core.network.NetworkDiagnosticException",
            "com.quizzgame.core.network.MockFailureReason",
        ).forEach { name ->
            assertThrows(ClassNotFoundException::class.java) { Class.forName(name) }
        }
    }
}
