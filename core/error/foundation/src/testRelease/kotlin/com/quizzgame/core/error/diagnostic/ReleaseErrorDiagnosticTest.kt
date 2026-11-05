package com.quizzgame.core.error.diagnostic

import org.junit.Assert.assertFalse
import org.junit.Test

class ReleaseErrorDiagnosticTest {

    @Test
    fun releaseDiagnosticCategoriesExcludeMockCodes() {
        assertFalse(ErrorDiagnosticCategory.entries.any { it.name.contains("MOCK") })
    }
}
