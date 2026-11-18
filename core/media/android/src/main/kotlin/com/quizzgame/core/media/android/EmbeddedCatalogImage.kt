package com.quizzgame.core.media.android

import android.graphics.BitmapFactory
import android.util.Base64
import com.quizzgame.core.media.MediaSpec

internal fun MediaSpec.Embedded.validatedImageBytes(): ByteArray? {
    if (base64.length > MAX_BASE64_LENGTH || base64.any { it !in BASE64_CHARS }) return null
    val bytes = decodeBase64(base64) ?: return null
    if (bytes.size > MAX_EMBEDDED_BYTES) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    return bytes.takeIf {
        bounds.outWidth in 1..MAX_EMBEDDED_DIMENSION && bounds.outHeight in 1..MAX_EMBEDDED_DIMENSION &&
            bounds.outMimeType == mimeType
    }
}

private fun decodeBase64(value: String): ByteArray? = try {
    Base64.decode(value, Base64.NO_WRAP)
} catch (_: IllegalArgumentException) {
    null
}

private const val BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
private const val MAX_BASE64_LENGTH = 174764
private const val MAX_EMBEDDED_BYTES = 131072
private const val MAX_EMBEDDED_DIMENSION = 2048
