package com.quizzgame.core.error.classification

import org.junit.Assert.assertThrows
import org.junit.Test

class ReleaseDiagnosticClassifierTest {

    @Test
    fun releaseClasspathExcludesAllDevelopmentFailureTypes() {
        listOf(
            "com.quizzgame.core.error.failure.DevelopmentFailure",
            "com.quizzgame.core.error.failure.DevelopmentKind",
            "com.quizzgame.core.error.failure.AppFailure\$Development",
            "com.quizzgame.core.network.result.NetworkFailure\$MockConfiguration",
            "com.quizzgame.core.network.diagnostic.MockFailureReason",
            "com.quizzgame.core.network.diagnostic.NetworkDiagnosticException",
            "com.quizzgame.core.network.auth.RefreshFailureReason\$Diagnostic",
            "com.quizzgame.core.session.refresh.api.RefreshFailure\$Diagnostic",
        ).forEach { name ->
            assertThrows(ClassNotFoundException::class.java) { Class.forName(name) }
        }
    }
}
