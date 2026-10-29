package com.quizzgame.core.session

import android.os.Process
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.core.session.storage.SESSION_KEY_ALIAS
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest

private const val FINGERPRINT_BYTES = 32

@RunWith(AndroidJUnit4::class)
class AndroidSessionProcessTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val marker = File(context.cacheDir, "session-process-test")
    private val directory = File(context.noBackupFilesDir, SESSION_DIRECTORY)

    @Test
    fun persistedSessionProcessBoundary() = runBlocking {
        check(context.packageName.endsWith(".test")) { "Storage tests require the isolated library test host" }
        val phase = InstrumentationRegistry.getArguments().getString("sessionPhase", "roundtrip")
        require(phase in setOf("write", "read", "roundtrip"))
        if (phase != "read") writeSession()
        if (phase != "write") {
            try {
                verifySession(requireNewProcess = phase == "read")
            } finally {
                removeStorage()
            }
        }
    }

    private suspend fun writeSession() {
        removeStorage()
        AndroidSession.open(context).use { manager ->
            assertEquals(SessionOperation.Applied, manager.startSession(TokenPair("process-access", "process-refresh")))
            DataOutputStream(marker.outputStream()).use {
                it.write(fingerprint(manager.deviceIdProvider.get()))
                it.writeInt(Process.myPid())
            }
        }
    }

    private suspend fun verifySession(requireNewProcess: Boolean) {
        var id: String? = null
        AndroidSession.open(context).use { manager ->
            id = manager.deviceIdProvider.get()
            DataInputStream(marker.inputStream()).use {
                val expectedId = ByteArray(FINGERPRINT_BYTES).also(it::readFully)
                assertArrayEquals(expectedId, fingerprint(manager.deviceIdProvider.get()))
                val previousPid = it.readInt()
                if (requireNewProcess) assertNotEquals(previousPid, Process.myPid())
            }
            assertEquals("process-access", manager.snapshot().accessToken)
            assertEquals(SessionOperation.Applied, manager.logout())
        }
        AndroidSession.open(context).use {
            assertEquals(SessionState.Guest, it.state.value)
            assertEquals(id, it.deviceIdProvider.get())
        }
    }

    private fun fingerprint(id: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(id.toByteArray())

    private fun removeStorage() {
        directory.deleteRecursively()
        marker.delete()
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(SESSION_KEY_ALIAS)
    }
}
