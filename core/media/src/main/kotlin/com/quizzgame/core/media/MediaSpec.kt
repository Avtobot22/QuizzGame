package com.quizzgame.core.media

sealed interface MediaSpec {
    val presentation: MediaPresentation get() = MediaPresentation()
    data class Remote(val url: String, override val presentation: MediaPresentation = MediaPresentation()) : MediaSpec {
        override fun toString(): String = "Remote(<redacted>)"
    }
    data class Icon(val name: String, override val presentation: MediaPresentation = MediaPresentation()) : MediaSpec
    data class Asset(val name: String, override val presentation: MediaPresentation = MediaPresentation()) : MediaSpec
    class Embedded(
        val mimeType: String,
        val base64: String,
        override val presentation: MediaPresentation = MediaPresentation(),
    ) : MediaSpec {
        override fun toString(): String = "Embedded(<redacted>)"
    }
    data object Placeholder : MediaSpec
}
