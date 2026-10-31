package com.quizzgame.core.error

import org.junit.Assert.assertFalse
import org.junit.Test

class ReleaseErrorCopyKeyTest {

    @Test
    fun releaseCopyKeysExcludeMockDiagnostics() {
        assertFalse(ErrorCopyKey.entries.any { it.name.contains("MOCK") })
    }
}
