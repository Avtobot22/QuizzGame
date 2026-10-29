package com.quizzgame.core.session.storage

import com.quizzgame.core.session.SessionRecord
import com.quizzgame.core.session.TokenPair
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

internal object SessionRecordCodec {
    fun encode(record: SessionRecord): ByteArray = ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeByte(1)
            output.writeUTF(record.deviceId)
            output.writeBoolean(record.tokens != null)
            record.tokens?.let { tokens ->
                output.writeUTF(tokens.access)
                output.writeUTF(tokens.refresh)
            }
        }
        bytes.toByteArray()
    }

    fun decode(bytes: ByteArray): SessionRecord = DataInputStream(ByteArrayInputStream(bytes)).use { input ->
        check(input.readUnsignedByte() == 1) { "Unsupported session record version" }
        val id = input.readUTF()
        val tokens = when (input.readUnsignedByte()) {
            0 -> null
            1 -> TokenPair(input.readUTF(), input.readUTF())
            else -> error("Invalid session record")
        }
        check(input.read() == -1) { "Invalid session record" }
        SessionRecord(id, tokens)
    }
}
