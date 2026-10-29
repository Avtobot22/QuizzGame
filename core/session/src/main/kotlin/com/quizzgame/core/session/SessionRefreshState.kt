package com.quizzgame.core.session

import com.quizzgame.core.network.CredentialsSnapshot

internal class SessionRefreshState(
    val credentials: CredentialsSnapshot,
    val deviceId: String,
    val tokens: TokenPair,
) {
    override fun toString(): String = "SessionRefreshState(<redacted>)"
}
