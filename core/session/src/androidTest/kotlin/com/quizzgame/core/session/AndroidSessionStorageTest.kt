package com.quizzgame.core.session

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.core.session.storage.SESSION_KEY_ALIAS
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.KeyStore

@RunWith(AndroidJUnit4::class)
class AndroidSessionStorageTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val directory = File(context.noBackupFilesDir, SESSION_DIRECTORY)
    private val sessionFile = File(directory, SESSION_FILE)
    private val managers = mutableListOf<SessionManager>()

    @Before
    fun prepareStorage() {
        check(context.packageName.endsWith(".test")) { "Storage tests require the isolated library test host" }
        removeStorage()
    }

    @After
    fun removeStorage() {
        managers.asReversed().forEach(SessionManager::close)
        managers.clear()
        directory.deleteRecursively()
        keyStore().deleteEntry(SESSION_KEY_ALIAS)
    }

    @Test
    fun keystorePairRestoresFromDiskAndLogoutKeepsInstallationIdentity() = runBlocking {
        val manager = openSession()
        val id = manager.deviceIdProvider.get()
        assertEquals(SessionState.Guest, manager.state.value)
        assertTrue(sessionFile.isFile)
        assertEquals(SessionOperation.Applied, manager.startSession(TokenPair("fixture-access", "fixture-refresh")))
        val encrypted = sessionFile.readBytes().toString(Charsets.ISO_8859_1)
        assertFalse(encrypted.contains("fixture-"))
        assertFalse(encrypted.contains(id))
        assertNull(keyStore().getKey(SESSION_KEY_ALIAS, null).encoded)
        manager.close()
        val reopened = openSession()
        assertEquals("fixture-access", reopened.snapshot().accessToken)
        assertEquals(id, reopened.deviceIdProvider.get())
        assertEquals(
            SessionOperation.Applied,
            reopened.applyRefreshedTokensIfCurrentBlocking(reopened.snapshot(), TokenPair("new-access", "new-refresh")),
        )
        assertEquals(id, reopened.deviceIdProvider.get())
        assertEquals(SessionOperation.Applied, reopened.logout())
        reopened.close()
        val guest = openSession()
        assertEquals(SessionState.Guest, guest.state.value)
        assertNull(guest.snapshot().accessToken)
        assertEquals(id, guest.deviceIdProvider.get())
        assertEquals(listOf(SESSION_FILE), directory.list()!!.toList())
    }

    @Test
    fun damagedCiphertextDoesNotDeleteCredentialsOrPretendGuest() = runBlocking {
        val manager = openSession()
        manager.startSession(TokenPair("fixture-access", "fixture-refresh"))
        manager.close()
        val damaged = sessionFile.readBytes().apply { this[lastIndex] = (this[lastIndex].toInt() xor 1).toByte() }
        sessionFile.writeBytes(damaged)
        val unavailable = openSession()
        assertEquals(SessionState.Unavailable(SessionStorageFailure.CRYPTO), unavailable.state.value)
        assertThrows(SessionUnavailableException::class.java) { unavailable.snapshot() }
        assertThrows(SessionUnavailableException::class.java) { unavailable.deviceIdProvider.get() }
        assertArrayEquals(damaged, sessionFile.readBytes())
        assertEquals(SessionOperation.Failed(SessionStorageFailure.CRYPTO), unavailable.logout())
        assertArrayEquals(damaged, sessionFile.readBytes())
        unavailable.close()
        assertEquals(SessionState.Unavailable(SessionStorageFailure.CRYPTO), openSession().state.value)
    }

    @Test
    fun missingKeyDoesNotRegenerateItWhileReadingExistingCredentials() = runBlocking {
        val manager = openSession()
        manager.startSession(TokenPair("fixture-access", "fixture-refresh"))
        manager.close()
        val record = sessionFile.readBytes()
        keyStore().deleteEntry(SESSION_KEY_ALIAS)
        val unavailable = openSession()
        assertEquals(SessionState.Unavailable(SessionStorageFailure.CRYPTO), unavailable.state.value)
        assertThrows(SessionUnavailableException::class.java) { unavailable.deviceIdProvider.get() }
        assertThrows(SessionUnavailableException::class.java) { unavailable.snapshot() }
        assertEquals(SessionOperation.Failed(SessionStorageFailure.CRYPTO), unavailable.logout())
        assertFalse(keyStore().containsAlias(SESSION_KEY_ALIAS))
        assertArrayEquals(record, sessionFile.readBytes())
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private suspend fun openSession(): SessionManager = AndroidSession.open(context).also(managers::add)

    @Test
    fun guestInstallationIdentityIsEncryptedAndRestoresWithoutAnAccount() = runBlocking {
        val manager = openSession()
        val id = manager.deviceIdProvider.get()
        assertEquals(SessionState.Guest, manager.state.value)
        assertTrue(sessionFile.isFile)
        assertFalse(sessionFile.readBytes().toString(Charsets.ISO_8859_1).contains(id))
        manager.close()
        val reopened = openSession()
        assertEquals(id, reopened.deviceIdProvider.get())
        assertEquals(SessionState.Guest, reopened.state.value)
        assertEquals(listOf(SESSION_FILE), directory.list()!!.toList())
    }
}
