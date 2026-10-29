package com.quizzgame.core.session.storage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.FileStorage
import androidx.datastore.core.Serializer
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import com.quizzgame.core.session.SessionRecord
import com.quizzgame.core.session.SessionStorageException
import com.quizzgame.core.session.SessionStorageFailure
import com.quizzgame.core.session.SessionStore
import com.quizzgame.core.session.TokenPair
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.ProviderException
import java.util.UUID
import java.util.concurrent.Executors

internal class DataStoreSessionStore(file: File, aead: Aead) : SessionStore {

    private val dispatcher = Executors.newSingleThreadExecutor { work ->
        Thread(work, "session-datastore").apply { isDaemon = true }
    }.asCoroutineDispatcher()
    private val job = SupervisorJob()
    private val encrypted = AeadSerializer(aead, RecordSerializer, "session:${file.name}:v1".toByteArray())
    private val serializer = object : Serializer<SessionRecord?> {
        override val defaultValue = encrypted.defaultValue
        override suspend fun readFrom(input: InputStream): SessionRecord? {
            val bytes = input.readRecordBytes()
            return encrypted.readFrom(ByteArrayInputStream(bytes))
        }

        override suspend fun writeTo(t: SessionRecord?, output: OutputStream) = encrypted.writeTo(t, output)
    }
    private val dataStore = DataStore.Builder(
        FileStorage(serializer = serializer, produceFile = { file }),
        job + dispatcher,
    ).build()

    override fun read(): SessionRecord = sanitized(SessionStorageFailure.READ) {
        runBlocking {
            checkNotNull(dataStore.updateData { record -> record ?: SessionRecord(UUID.randomUUID().toString(), null) })
        }
    }

    override fun write(tokens: TokenPair?): SessionRecord = sanitized(SessionStorageFailure.WRITE) {
        runBlocking {
            checkNotNull(
                dataStore.updateData { record ->
                    SessionRecord(record?.deviceId ?: UUID.randomUUID().toString(), tokens)
                },
            )
        }
    }

    override fun close() {
        runBlocking { job.cancelAndJoin() }
        dispatcher.close()
    }

    private object RecordSerializer : Serializer<SessionRecord?> {

        override val defaultValue: SessionRecord? = null

        override suspend fun readFrom(input: InputStream): SessionRecord = try {
            SessionRecordCodec.decode(input.readRecordBytes())
        } catch (_: IOException) {
            throw CorruptionException("Invalid session record")
        } catch (_: IllegalArgumentException) {
            throw CorruptionException("Invalid session record")
        } catch (_: IllegalStateException) {
            throw CorruptionException("Invalid session record")
        }

        override suspend fun writeTo(t: SessionRecord?, output: OutputStream) {
            val bytes = SessionRecordCodec.encode(checkNotNull(t))
            try {
                output.write(bytes)
            } finally {
                bytes.fill(0)
            }
        }
    }
}

internal const val MAX_SESSION_RECORD_BYTES = 256 * 1024
private const val MAX_CAUSE_DEPTH = 8

private fun InputStream.readRecordBytes(): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val count = read(buffer)
        if (count < 0) return output.toByteArray()
        if (output.size() + count > MAX_SESSION_RECORD_BYTES) throw CorruptionException("Invalid session record size")
        output.write(buffer, 0, count)
    }
}

private inline fun <T> sanitized(fallback: SessionStorageFailure, action: () -> T): T = try {
    action()
} catch (failure: CorruptionException) {
    val cryptoFailure = generateSequence<Throwable>(failure) { it.cause }.take(MAX_CAUSE_DEPTH)
        .any { it is GeneralSecurityException }
    val reason = if (cryptoFailure) SessionStorageFailure.CRYPTO else SessionStorageFailure.CORRUPT
    throw SessionStorageException(reason)
} catch (_: GeneralSecurityException) {
    throw SessionStorageException(SessionStorageFailure.CRYPTO)
} catch (_: ProviderException) {
    throw SessionStorageException(SessionStorageFailure.CRYPTO)
} catch (_: IOException) {
    throw SessionStorageException(fallback)
}
