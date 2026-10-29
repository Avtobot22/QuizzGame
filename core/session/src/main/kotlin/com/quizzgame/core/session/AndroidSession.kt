package com.quizzgame.core.session

import android.content.Context
import com.quizzgame.core.session.storage.AndroidKeystoreAead
import com.quizzgame.core.session.storage.DataStoreSessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal const val SESSION_DIRECTORY = "session"
internal const val SESSION_FILE = "session.enc"

object AndroidSession {

    suspend fun open(
        context: Context,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): SessionManager {
        var acquired: SessionManager? = null
        try {
            return withContext(ioDispatcher) {
                val directory = File(context.applicationContext.noBackupFilesDir, SESSION_DIRECTORY)
                val store = DataStoreSessionStore(File(directory, SESSION_FILE), AndroidKeystoreAead())
                SessionManager.open(store, ioDispatcher).also { acquired = it }
            }
        } catch (cancelled: CancellationException) {
            acquired?.close()
            throw cancelled
        }
    }
}
