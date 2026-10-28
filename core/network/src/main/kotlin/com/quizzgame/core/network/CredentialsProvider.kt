package com.quizzgame.core.network

fun interface CredentialsProvider {

    fun snapshot(): CredentialsSnapshot

    companion object {

        fun guest(): CredentialsProvider = CredentialsProvider { CredentialsSnapshot() }
    }
}

class CredentialsSnapshot(
    val accessToken: String? = null,
    val sessionGeneration: Long = 0,
) {

    init {
        require(sessionGeneration >= 0) { "Session generation must not be negative" }
        require(accessToken == null || (accessToken.isNotEmpty() && accessToken.all { it in '!'..'~' })) {
            "Access token must contain only visible ASCII characters without whitespace"
        }
    }

    override fun toString(): String =
        "CredentialsSnapshot(sessionGeneration=$sessionGeneration, accessToken=<redacted>)"
}

class RequestAuthContext internal constructor(val observedCredentials: CredentialsSnapshot) {

    override fun toString(): String = "RequestAuthContext(observedCredentials=<redacted>)"
}
