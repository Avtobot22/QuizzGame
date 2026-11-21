package com.quizzgame.shared.room.data

import com.quizzgame.shared.room.data.storage.recoverRoomCredential
import com.quizzgame.shared.room.domain.entity.session.RoomRole
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.ProviderException
import javax.crypto.AEADBadTagException

class RoomCredentialRecoveryTest {

    @Test
    fun validRecordIsRestored() {
        val record = recoverRoomCredential(
            Json,
            decrypt = { HOST_RECORD.encodeToByteArray() },
            resetKey = { error("Valid key must be kept") },
            discard = { error("Valid record must be kept") },
        )

        assertEquals(RoomRole.HOST, record?.credential?.role)
        assertEquals(1L, record?.accountGeneration)
    }

    @Test
    fun missingKeyAndDamagedRecordsAreDiscarded() {
        val unreadableRecords = listOf<() -> ByteArray?>(
            { null },
            { throw AEADBadTagException() },
            { "{".encodeToByteArray() },
            { HOST_RECORD.replace(ROOM_ID, "invalid-id").encodeToByteArray() },
        )

        unreadableRecords.forEach { decrypt ->
            var discardCount = 0
            val record = recoverRoomCredential(
                Json,
                decrypt,
                resetKey = { error("The key has not been invalidated") },
                discard = { discardCount++ },
            )
            assertNull(record)
            assertEquals(1, discardCount)
        }
    }

    @Test
    fun temporaryKeystoreFailureKeepsTheRecord() {
        val failures = listOf(GeneralSecurityException(), ProviderException())
        failures.forEach { failure ->
            assertThrows(failure.javaClass) {
                recoverRoomCredential(
                    Json,
                    decrypt = { throw failure },
                    resetKey = { error("Unavailable key must be kept") },
                    discard = { error("Record must be available for retry") },
                )
            }
        }
    }

    @Test
    fun failedDiscardDoesNotReportAnEmptyStore() {
        assertThrows(IOException::class.java) {
            recoverRoomCredential(
                Json,
                decrypt = { null },
                resetKey = { error("Missing key cannot be reset") },
                discard = { throw IOException() },
            )
        }
    }
}

private const val ROOM_ID = "11111111-1111-1111-1111-111111111111"
private const val HOST_RECORD = """
    {
        "roomId": "$ROOM_ID",
        "credential": {
            "role": "host",
            "roomToken": "fake-room-credential",
            "expiresAt": "2026-11-22T00:00:00Z"
        },
        "self": null,
        "accountGeneration": 1
    }
"""
