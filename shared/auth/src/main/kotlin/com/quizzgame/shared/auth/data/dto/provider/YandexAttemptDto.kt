package com.quizzgame.shared.auth.data.dto.provider

import kotlinx.serialization.Serializable

@Serializable
internal class YandexAttemptDto(
    val attemptId: String,
    val authorizationUrl: String,
    val attemptSecret: String,
    val expiresAt: String,
)
