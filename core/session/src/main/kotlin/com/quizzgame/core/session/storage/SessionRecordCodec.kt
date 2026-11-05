package com.quizzgame.core.session.storage

import com.quizzgame.core.session.model.TokenPair
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

internal object SessionRecordCodec {
    fun encode(record: SessionRecord): ByteArray = ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeUTF(record.deviceId)
            output.writeLong(record.epoch)
            output.writeLong(record.revision)
            output.writeByte(record.intent.ordinal)
            output.writeBoolean(record.tokens != null)
            record.tokens?.let { tokens ->
                output.writeUTF(tokens.access)
                output.writeUTF(tokens.refresh)
            }
        }
        bytes.toByteArray()
    }

    fun decode(bytes: ByteArray): SessionRecord = DataInputStream(ByteArrayInputStream(bytes)).use { input ->
        val id = input.readUTF()
        val epoch = input.readLong()
        val revision = input.readLong()
        val intent = SessionIntent.entries.getOrNull(input.readUnsignedByte()) ?: error("Invalid session intent")
        val tokens = when (input.readUnsignedByte()) {
            0 -> null
            1 -> TokenPair(input.readUTF(), input.readUTF())
            else -> error("Invalid session record")
        }
        check(input.read() == -1) { "Invalid session record" }
        SessionRecord(id, tokens, epoch, revision, intent)
    }
}
