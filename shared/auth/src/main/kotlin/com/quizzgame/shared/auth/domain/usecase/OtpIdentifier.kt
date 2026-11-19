package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.shared.auth.domain.entity.AuthMethod

internal fun normalizeIdentifier(method: AuthMethod, input: String): String? {
    val trimmed = input.trim()
    return when (method) {
        AuthMethod.EMAIL -> trimmed.takeIf { it.length <= MAX_EMAIL_LENGTH && EMAIL.matches(it) }
        AuthMethod.SMS -> trimmed.filterNot { it in " -()" }.takeIf(PHONE::matches)
        else -> null
    }
}

private val EMAIL = Regex("[^@\\s]+@[^@\\s.]+(?:[.][^@\\s.]+)+")
private val PHONE = Regex("[+][1-9][0-9]{6,14}")

private const val MAX_EMAIL_LENGTH = 254
