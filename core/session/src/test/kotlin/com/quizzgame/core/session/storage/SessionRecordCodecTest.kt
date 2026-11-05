package com.quizzgame.core.session.storage

import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionUnavailableException
import com.quizzgame.core.session.lifecycle.TEST_DEVICE_ID
import com.quizzgame.core.session.lifecycle.fixturePair
import com.quizzgame.core.session.lifecycle.sameContents
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.ByteBuffer

class SessionRecordCodecTest {
    @get:Rule
    val temporary = TemporaryFolder()

    @Test
    fun roundTripRetainsTokensCountersAndIntent() {
        val record = SessionRecord(
            TEST_DEVICE_ID,
            fixturePair("A"),
            epoch = 3,
            revision = 7,
            intent = SessionIntent.REFRESH,
        )
        assertTrue(record.sameContents(SessionRecordCodec.decode(SessionRecordCodec.encode(record))))
        assertFalse(record.toString().contains("fixture-A"))
    }

    @Test
    fun invalidCountersAndTrailingBytesAreRejected() {
        val encoded = SessionRecordCodec.encode(SessionRecord(TEST_DEVICE_ID, fixturePair("A")))
        assertThrows(IllegalStateException::class.java) { SessionRecordCodec.decode(encoded + byteArrayOf(0)) }
        val invalidEpoch = encoded.clone()
        ByteBuffer.wrap(invalidEpoch).putLong(2 + TEST_DEVICE_ID.length, -1L)
        assertThrows(IllegalArgumentException::class.java) { SessionRecordCodec.decode(invalidEpoch) }
    }

    @Test
    fun encryptedColdStartResolvesDurableIntentsBeforeAnyOldCredentialsArePublished() = runTest {
        val aead = testAead()
        listOf(SessionIntent.LOGOUT, SessionIntent.REVOKED, SessionIntent.REFRESH).forEach { intent ->
            val file = File(temporary.root, "intent-${intent.name}.enc")
            val marker = SessionRecord(
                TEST_DEVICE_ID,
                fixturePair("A"),
                epoch = 2,
                intent = intent,
            )
            DataStoreSessionStore(file, aead).use { it.writeRecord(marker) }
            SessionManager.open(DataStoreSessionStore(file, aead)).use { restarted ->
                when (intent) {
                    SessionIntent.LOGOUT -> assertEquals(SessionState.Guest, restarted.state.value)
                    SessionIntent.REVOKED -> assertEquals(SessionState.Expired, restarted.state.value)
                    SessionIntent.REFRESH -> {
                        assertTrue(restarted.state.value is SessionState.Blocked)
                        assertThrows(SessionUnavailableException::class.java) { restarted.snapshot() }
                    }
                    SessionIntent.NONE -> error("Fixture must contain a pending intent")
                }
            }
        }
    }
}
