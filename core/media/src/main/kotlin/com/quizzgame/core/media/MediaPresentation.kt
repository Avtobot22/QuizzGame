package com.quizzgame.core.media

data class MediaPresentation(
    val contain: Boolean = false,
    val aspectRatio: Float = DEFAULT_ASPECT_RATIO,
    val durationMs: Int = 220,
    val transition: String = "fade",
    val blurRadius: Float = 0f,
    val tint: ColorSpec? = null,
)

private const val DEFAULT_ASPECT_RATIO = 16f / 9f
