package com.quizzgame.core.media.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MediaLoadingDto(val maxAttempts: Int = 1, val retryDelayMs: Int = DEFAULT_RETRY_DELAY_MS)

private const val DEFAULT_RETRY_DELAY_MS = 600
