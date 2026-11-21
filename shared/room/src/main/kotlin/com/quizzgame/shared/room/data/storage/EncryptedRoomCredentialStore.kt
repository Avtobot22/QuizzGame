package com.quizzgame.shared.room.data.storage

import android.content.Context
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.util.AtomicFile
import com.google.crypto.tink.integration.android.AndroidKeystore
import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.shared.room.data.dto.admission.CredentialDto
import com.quizzgame.shared.room.data.dto.storage.StoredRoomDto
import com.quizzgame.shared.room.data.mapper.toDomain
import com.quizzgame.shared.room.domain.entity.session.ParticipantId
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomRole
import com.quizzgame.shared.room.domain.entity.session.roomUuid
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.ProviderException
import java.util.Locale
import java.util.UUID
import javax.crypto.BadPaddingException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class EncryptedRoomCredentialStore @Inject constructor(
    @ApplicationContext context: Context,
    private val dispatchers: DispatcherProvider,
    private val json: Json,
) : RoomCredentialStore {
    private val record = AtomicFile(File(context.noBackupFilesDir, "active-room"))
    private val installation = AtomicFile(File(context.noBackupFilesDir, "room-installation"))

    override suspend fun load(): StoredRoom? = withContext(dispatchers.io) {
        val encrypted = record.readIfPresent() ?: return@withContext null
        keystoreOperation("Room credentials cannot be decrypted") {
            recoverRoomCredential(
                json,
                decrypt = { decryptRoomCredential(encrypted) },
                resetKey = { AndroidKeystore.deleteKey(KEY_ALIAS) },
                discard = ::clearRecord,
            )
        }
    }

    override suspend fun save(room: StoredRoom) = withContext(dispatchers.io) {
        val dto = StoredRoomDto(
            room.id.toString(),
            CredentialDto(
                room.credential.role.name.lowercase(Locale.ROOT),
                room.credential.token,
                room.credential.expiresAt.toString(),
            ),
            room.self?.toString(),
            room.accountGeneration,
        )
        val bytes = keystoreOperation("Room credentials cannot be encrypted") {
            if (!AndroidKeystore.hasKey(KEY_ALIAS)) AndroidKeystore.generateNewAes256GcmKey(KEY_ALIAS)
            AndroidKeystore.getAead(KEY_ALIAS).encrypt(json.encodeToString(dto).encodeToByteArray(), associatedData)
        }
        record.replace(bytes)
    }

    override suspend fun clear() = withContext(dispatchers.io) { clearRecord() }

    private fun clearRecord() {
        record.delete()
        if (record.baseFile.exists()) throw IOException("Room credentials could not be cleared")
    }

    override suspend fun installationId(): String = withContext(dispatchers.io) {
        installation.readIfPresent()?.decodeToString()?.roomUuid()?.toString() ?: UUID.randomUUID().toString().also {
            installation.replace(it.encodeToByteArray())
        }
    }

    private fun decryptRoomCredential(encrypted: ByteArray): ByteArray? {
        val aead = try {
            AndroidKeystore.getAead(KEY_ALIAS)
        } catch (failure: InvalidKeyException) {
            // Tink throws InvalidKeyException here when KeyStore.getKey returns null.
            if (failure.javaClass != InvalidKeyException::class.java) throw failure
            return null
        }
        return aead.decrypt(encrypted, associatedData)
    }

    private companion object {
        const val KEY_ALIAS = "quizzgame.room"
        val associatedData = "quizzgame.room.credential.v1".encodeToByteArray()
    }
}

internal fun recoverRoomCredential(
    json: Json,
    decrypt: () -> ByteArray?,
    resetKey: () -> Unit,
    discard: () -> Unit,
): StoredRoom? {
    val data = try {
        decrypt()
    } catch (_: BadPaddingException) {
        null
    } catch (_: KeyPermanentlyInvalidatedException) {
        resetKey()
        null
    }
    val room = try {
        data?.let { json.decodeFromString<StoredRoomDto>(it.decodeToString()).toStoredRoom() }
    } catch (_: IllegalArgumentException) {
        null
    }
    if (room == null) discard()
    return room
}

private fun StoredRoomDto.toStoredRoom(): StoredRoom {
    val decoded = credential.toDomain()
    val participant = self?.let { ParticipantId(requireNotNull(it.roomUuid())) }
    require((decoded.role == RoomRole.PARTICIPANT) == (participant != null))
    return StoredRoom(requireNotNull(RoomId.parse(roomId)), decoded, participant, accountGeneration)
}

private inline fun <T> keystoreOperation(message: String, operation: () -> T): T = try {
    operation()
} catch (_: GeneralSecurityException) {
    throw IOException(message)
} catch (_: ProviderException) {
    throw IOException(message)
}

private fun AtomicFile.readIfPresent(): ByteArray? = try {
    readFully()
} catch (missing: FileNotFoundException) {
    if (baseFile.exists()) throw missing
    null
}

private fun AtomicFile.replace(data: ByteArray) {
    val stream = startWrite()
    try {
        stream.write(data)
        finishWrite(stream)
    } catch (failure: IOException) {
        failWrite(stream)
        throw failure
    }
}
