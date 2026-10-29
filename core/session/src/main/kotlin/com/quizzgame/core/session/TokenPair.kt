package com.quizzgame.core.session

import com.quizzgame.core.network.CredentialsSnapshot

class TokenPair(val access: String, val refresh: String) {
    init {
        CredentialsSnapshot(access)
        require(refresh.isNotBlank()) { "Refresh token must not be blank" }
    }

    override fun toString(): String = "TokenPair(access=<redacted>, refresh=<redacted>)"
}
