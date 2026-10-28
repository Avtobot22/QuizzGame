package com.quizzgame.core.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class CredentialsSnapshotTest {
    @Test
    fun `snapshot and request auth context diagnostics redact access credentials`() {
        val secret = "fixture-sensitive-access"
        val snapshot = CredentialsSnapshot(secret, 5)
        val context = RequestAuthContext(snapshot)
        assertFalse(snapshot.toString().contains(secret))
        assertFalse(context.toString().contains(secret))
    }

    @Test
    fun `invalid bearer values fail with neutral diagnostics before header construction`() {
        listOf(
            "",
            " ",
            "fixture-sensitive\r\nheader",
            "fixture-sensitive\tvalue",
            "fixture-sensitive value",
            "fixture-sensitive\u0100",
        ).forEach { invalid ->
            val failure = assertThrows(IllegalArgumentException::class.java) { CredentialsSnapshot(invalid) }
            assertFalse(failure.toString().contains("fixture-sensitive"))
        }
    }
}
