package com.quizzgame.core.error

import org.junit.Assert.assertThrows
import org.junit.Test

class ReleaseDiagnosticClassifierTest {

    @Test
    fun releaseClasspathExcludesAllDevelopmentFailureTypes() {
        listOf(
            "com.quizzgame.core.error.DevelopmentFailure",
            "com.quizzgame.core.error.DevelopmentKind",
            "com.quizzgame.core.error.AppFailure\$Development",
            "com.quizzgame.core.network.NetworkFailure\$MockConfiguration",
            "com.quizzgame.core.network.MockFailureReason",
            "com.quizzgame.core.network.NetworkDiagnosticException",
            "com.quizzgame.core.network.RefreshFailureReason\$Diagnostic",
            "com.quizzgame.core.session.api.RefreshFailure\$Diagnostic",
        ).forEach { name ->
            assertThrows(ClassNotFoundException::class.java) { Class.forName(name) }
        }
    }
}
