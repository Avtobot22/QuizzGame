package com.quizzgame.core.session.storage

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.quizzgame.core.session.SessionManager
import com.quizzgame.core.session.SessionOperation
import com.quizzgame.core.session.SessionState
import com.quizzgame.core.session.SessionStorageException
import com.quizzgame.core.session.SessionStorageFailure
import com.quizzgame.core.session.SessionUnavailableException
import com.quizzgame.core.session.TokenPair
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.security.GeneralSecurityException
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SecureDataStoreTest {

    @get:Rule
    val temporary = TemporaryFolder()
    private val aead = testAead()

    @Test
    fun `encrypted DataStore reopens the complete pair and persists clearing`() {
        val file = File(temporary.root, "session.enc")
        var id: String? = null
        DataStoreSessionStore(file, aead).use { store ->
            val record = store.read()
            id = record.deviceId
            assertNull(record.tokens)
            store.write(TokenPair("fixture-sensitive-access", "fixture-sensitive-refresh"))
            val ciphertext = file.readBytes().toString(Charsets.ISO_8859_1)
            assertFalse(ciphertext.contains("fixture-sensitive"))
            assertFalse(ciphertext.contains(record.deviceId))
        }
        DataStoreSessionStore(file, aead).use { reopened ->
            assertEquals(id, reopened.read().deviceId)
            assertEquals("fixture-sensitive-access", reopened.read().tokens!!.access)
            assertEquals("fixture-sensitive-refresh", reopened.read().tokens!!.refresh)
            reopened.write(null)
        }
        DataStoreSessionStore(file, aead).use {
            val guest = it.read()
            assertNull(guest.tokens)
            assertEquals(id, guest.deviceId)
        }
        assertEquals(listOf("session.enc"), temporary.root.list()!!.toList())
    }

    @Test
    fun `tampering and unavailable key retain the file and expose only safe failures`() {
        val file = savedFile()
        val damaged = file.readBytes().apply { this[lastIndex] = (this[lastIndex].toInt() xor 1).toByte() }
        file.writeBytes(damaged)
        DataStoreSessionStore(
            file,
            aead,
        ).use { store -> assertSafeFailure(SessionStorageFailure.CRYPTO) { store.read() } }
        assertArrayEquals(damaged, file.readBytes())
        val noKey = object : Aead by aead {
            override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray =
                throw GeneralSecurityException("fixture-sensitive-key")
        }
        DataStoreSessionStore(file, noKey).use { assertSafeFailure(SessionStorageFailure.CRYPTO) { it.read() } }
        assertArrayEquals(damaged, file.readBytes())
    }

    @Test
    fun `malformed oversized and empty existing records never become a guest by fallback`() {
        val malformed = File(temporary.root, "malformed.enc")
        malformed.writeBytes(
            aead.encrypt("fixture-sensitive".toByteArray(), "session:${malformed.name}:v1".toByteArray()),
        )
        val oversized = File(temporary.root, "oversized.enc").apply {
            writeBytes(ByteArray(MAX_SESSION_RECORD_BYTES + 1))
        }
        val empty = File(temporary.root, "empty.enc").apply { writeBytes(byteArrayOf()) }
        val invalidIdentity = File(temporary.root, "invalid-identity.enc")
        val plain = ByteArrayOutputStream()
        DataOutputStream(plain).use {
            it.writeByte(1)
            it.writeUTF("fixture-sensitive-id")
            it.writeBoolean(false)
        }
        invalidIdentity.writeBytes(
            aead.encrypt(plain.toByteArray(), "session:${invalidIdentity.name}:v1".toByteArray()),
        )
        listOf(malformed, oversized, empty, invalidIdentity).forEach { file ->
            val before = file.readBytes()
            DataStoreSessionStore(file, aead).use { store ->
                assertSafeFailure(SessionStorageFailure.CORRUPT) { store.read() }
                assertSafeFailure(SessionStorageFailure.CORRUPT) { store.write(null) }
            }
            assertArrayEquals(before, file.readBytes())
        }
    }

    @Test
    fun `failed encryption commit leaves both cached and persisted tokens unchanged`() {
        val fail = AtomicBoolean(false)
        val failingAead = object : Aead by aead {
            override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray {
                if (fail.get()) throw GeneralSecurityException("fixture-sensitive-write")
                return aead.encrypt(plaintext, associatedData)
            }
        }
        val file = File(temporary.root, "session.enc")
        DataStoreSessionStore(file, failingAead).use { store ->
            store.write(TokenPair("old-access", "old-refresh"))
            val id = store.read().deviceId
            val before = file.readBytes()
            fail.set(true)
            assertSafeFailure(SessionStorageFailure.CRYPTO) { store.write(TokenPair("new-access", "new-refresh")) }
            assertArrayEquals(before, file.readBytes())
            assertEquals("old-access", store.read().tokens!!.access)
            assertEquals(id, store.read().deviceId)
        }
        DataStoreSessionStore(file, aead).use { assertEquals("old-refresh", it.read().tokens!!.refresh) }
    }

    @Test
    fun `clearing or replacing a record with unreadable identity never resets the installation`() {
        val file = savedFile()
        file.writeBytes(byteArrayOf(0))
        DataStoreSessionStore(file, aead).use { store ->
            assertSafeFailure(SessionStorageFailure.CRYPTO) { store.read() }
            assertArrayEquals(byteArrayOf(0), file.readBytes())
            assertSafeFailure(SessionStorageFailure.CRYPTO) { store.write(null) }
            assertSafeFailure(SessionStorageFailure.CRYPTO) { store.write(TokenPair("new-access", "new-refresh")) }
            assertArrayEquals(byteArrayOf(0), file.readBytes())
        }
    }

    @Test
    fun `concurrent DataStore readers see only full pairs`() {
        val file = File(temporary.root, "concurrent.enc")
        DataStoreSessionStore(file, aead).use { store ->
            store.write(TokenPair("access-0", "refresh-0"))
            val id = store.read().deviceId
            val executor = Executors.newFixedThreadPool(2)
            try {
                val writer = executor.submit { repeat(40) { store.write(TokenPair("access-$it", "refresh-$it")) } }
                val reader = executor.submit {
                    repeat(80) {
                        val record = store.read()
                        assertEquals(id, record.deviceId)
                        val tokens = record.tokens!!
                        assertEquals(tokens.access.removePrefix("access-"), tokens.refresh.removePrefix("refresh-"))
                    }
                }
                writer.get(10, TimeUnit.SECONDS)
                reader.get(10, TimeUnit.SECONDS)
            } finally {
                executor.shutdownNow()
            }
        }
    }

    @Test
    fun `concurrent initialization commits one canonical UUID and independent stores get different identities`() {
        val writes = AtomicInteger()
        val countingAead = object : Aead by aead {
            override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray {
                writes.incrementAndGet()
                return aead.encrypt(plaintext, associatedData)
            }
        }
        val file = File(temporary.root, "session.enc")
        val id = DataStoreSessionStore(file, countingAead).use { store ->
            val executor = Executors.newFixedThreadPool(4)
            try {
                val ids = List(8) { executor.submit<String> { store.read().deviceId } }
                    .map { it.get(5, TimeUnit.SECONDS) }
                assertEquals(1, ids.toSet().size)
                assertEquals(1, writes.get())
                ids.first().also { assertEquals(it, UUID.fromString(it).toString()) }
            } finally {
                executor.shutdownNow()
            }
        }
        DataStoreSessionStore(file, countingAead).use { assertEquals(id, it.read().deviceId) }
        assertEquals(1, writes.get())
        DataStoreSessionStore(File(temporary.root, "another-installation.enc"), aead).use {
            assertNotEquals(id, it.read().deviceId)
        }
    }

    @Test
    fun `manager login refresh logout and cache reads preserve the committed UUID`() = runTest {
        val cryptoCalls = AtomicInteger()
        val countingAead = object : Aead by aead {
            override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray {
                cryptoCalls.incrementAndGet()
                return aead.encrypt(plaintext, associatedData)
            }

            override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray {
                cryptoCalls.incrementAndGet()
                return aead.decrypt(ciphertext, associatedData)
            }
        }
        val file = File(temporary.root, "session.enc")
        val manager = SessionManager.open(DataStoreSessionStore(file, countingAead))
        val id = manager.deviceIdProvider.get()
        manager.use {
            assertEquals(SessionOperation.Applied, manager.startSession(TokenPair("access-1", "refresh-1")))
            assertEquals(id, manager.deviceIdProvider.get())
            assertEquals(
                SessionOperation.Applied,
                manager.applyRefreshedTokensIfCurrentBlocking(manager.snapshot(), TokenPair("access-2", "refresh-2")),
            )
            assertEquals(id, manager.deviceIdProvider.get())
            val calls = cryptoCalls.get()
            repeat(20) {
                assertEquals(id, manager.deviceIdProvider.get())
                assertEquals("access-2", manager.snapshot().accessToken)
            }
            assertEquals(calls, cryptoCalls.get())
            assertEquals(SessionOperation.Applied, manager.logout())
            assertEquals(id, manager.deviceIdProvider.get())
            assertEquals(SessionState.Guest, manager.state.value)
        }
        SessionManager.open(DataStoreSessionStore(file, aead)).use { restored ->
            assertEquals(id, restored.deviceIdProvider.get())
            assertNull(restored.snapshot().accessToken)
        }
    }

    @Test
    fun `failed initial commit exposes neither UUID nor credentials and can be retried`() = runTest {
        val fail = AtomicBoolean(true)
        val failingAead = object : Aead by aead {
            override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray {
                if (fail.get()) throw GeneralSecurityException("fixture-sensitive-key")
                return aead.encrypt(plaintext, associatedData)
            }
        }
        val file = File(temporary.root, "session.enc")
        SessionManager.open(DataStoreSessionStore(file, failingAead)).use { manager ->
            assertEquals(SessionState.Unavailable(SessionStorageFailure.CRYPTO), manager.state.value)
            assertThrows(SessionUnavailableException::class.java) { manager.deviceIdProvider.get() }
            assertThrows(SessionUnavailableException::class.java) { manager.snapshot() }
            assertFalse(file.exists())
            fail.set(false)
            assertEquals(SessionOperation.Applied, manager.restoreStoredSession())
            assertEquals(SessionState.Guest, manager.state.value)
            assertTrue(file.isFile)
            assertEquals(manager.deviceIdProvider.get(), UUID.fromString(manager.deviceIdProvider.get()).toString())
        }
    }

    private fun savedFile(): File = File(temporary.root, "session.enc").also { file ->
        DataStoreSessionStore(file, aead).use { it.write(TokenPair("old-access", "old-refresh")) }
    }

    private fun assertSafeFailure(expected: SessionStorageFailure, action: () -> Unit) {
        val failure = assertThrows(SessionStorageException::class.java, action)
        assertEquals(expected, failure.failure)
        assertNull(failure.cause)
        assertFalse(failure.toString().contains("fixture-sensitive"))
    }
}

internal fun testAead(): Aead {
    AeadConfig.register()
    return KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM)
        .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
}
