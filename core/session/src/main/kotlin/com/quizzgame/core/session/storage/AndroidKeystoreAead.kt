package com.quizzgame.core.session.storage

import com.google.crypto.tink.Aead
import com.google.crypto.tink.integration.android.AndroidKeystore
import java.security.GeneralSecurityException

internal const val SESSION_KEY_ALIAS = "quizzgame.session.v1"

internal class AndroidKeystoreAead : Aead {

    override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray = synchronized(keyLock) {
        if (!AndroidKeystore.hasKey(SESSION_KEY_ALIAS)) AndroidKeystore.generateNewAes256GcmKey(SESSION_KEY_ALIAS)
        AndroidKeystore.getAead(SESSION_KEY_ALIAS).encrypt(plaintext, associatedData)
    }

    override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray = synchronized(keyLock) {
        if (!AndroidKeystore.hasKey(SESSION_KEY_ALIAS)) {
            throw GeneralSecurityException("Session encryption key is unavailable")
        }
        AndroidKeystore.getAead(SESSION_KEY_ALIAS).decrypt(ciphertext, associatedData)
    }

    companion object {

        private val keyLock = Any()
    }
}
