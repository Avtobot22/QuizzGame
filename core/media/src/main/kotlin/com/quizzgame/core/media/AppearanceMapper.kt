package com.quizzgame.core.media

import com.quizzgame.core.media.data.dto.ColorDto
import com.quizzgame.core.media.data.dto.MediaDto
import com.quizzgame.core.media.data.dto.presentation.MediaScaleDto
import com.quizzgame.core.media.data.dto.presentation.MediaTransitionDto
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** Проверяет значения типизированных подсказок оформления перед отображением. */
object AppearanceMapper {
    fun color(dto: ColorDto?): ColorSpec? = when (dto) {
        is ColorDto.Token -> ColorSpec.Token(dto.name)
        is ColorDto.Hex -> hex(dto)
        null -> null
    }

    fun media(dto: MediaDto?, iconKeys: Set<String>, assetKeys: Set<String>): MediaSpec = when (dto) {
        is MediaDto.Remote -> dto.url.takeIf { it.toHttpUrlOrNull()?.isHttps == true }
            ?.let { MediaSpec.Remote(it, presentation(dto)) }
        is MediaDto.Icon -> dto.name.takeIf { it in iconKeys }?.let { MediaSpec.Icon(it, presentation(dto)) }
        is MediaDto.Asset -> dto.name.takeIf { it in assetKeys }?.let { MediaSpec.Asset(it, presentation(dto)) }
        is MediaDto.Embedded -> embedded(dto)
        null -> null
    } ?: MediaSpec.Placeholder

    private fun presentation(dto: MediaDto): MediaPresentation {
        val hint = dto.presentation
        val ratio = hint?.aspectRatio
        val blur = hint?.blurRadius
        val (transition, duration) = when (val value = hint?.transition) {
            MediaTransitionDto.None -> TRANSITION_NONE to 0
            is MediaTransitionDto.Fade -> TRANSITION_FADE to value.durationMs
            is MediaTransitionDto.FlipHorizontal -> TRANSITION_FLIP_HORIZONTAL to value.durationMs
            is MediaTransitionDto.FlipVertical -> TRANSITION_FLIP_VERTICAL to value.durationMs
            null -> TRANSITION_FADE to DEFAULT_DURATION
        }
        return MediaPresentation(
            contain = hint?.scale == MediaScaleDto.CONTAIN,
            aspectRatio = ratio?.takeIf { it.isFinite() && it in MIN_RATIO..MAX_RATIO } ?: DEFAULT_RATIO,
            durationMs = duration.coerceIn(0, MAX_DURATION),
            transition = transition,
            blurRadius = blur?.takeIf { it.isFinite() }?.coerceIn(0f, MAX_BLUR) ?: 0f,
            tint = color(dto.tint ?: hint?.tint),
        )
    }

    private fun embedded(dto: MediaDto.Embedded): MediaSpec? {
        if (dto.mimeType !in MIME_TYPES || dto.base64.length > MAX_BASE64_LENGTH) return null
        return MediaSpec.Embedded(dto.mimeType, dto.base64, presentation(dto))
    }

    private fun hex(dto: ColorDto.Hex): ColorSpec? = if (dto.value != null) singleHex(dto) else themeHex(dto)

    private fun singleHex(dto: ColorDto.Hex): ColorSpec? {
        if (dto.light != null || dto.dark != null) return null
        val value = dto.value?.let(::rgba) ?: return null
        return ColorSpec.Hex(value, value)
    }

    private fun themeHex(dto: ColorDto.Hex): ColorSpec? {
        val light = dto.light?.let(::rgba) ?: return null
        val dark = dto.dark?.let(::rgba) ?: return null
        return ColorSpec.Hex(light, dark)
    }

    private fun rgba(value: String): Long? {
        if (!HEX.matches(value)) return null
        val bytes = value.drop(1).toLong(HEX_RADIX)
        return if (value.length == RGB_LENGTH) {
            bytes or OPAQUE_ALPHA
        } else {
            ((bytes and ALPHA_MASK) shl ALPHA_SHIFT) or (bytes ushr BYTE_BITS)
        }
    }

    private const val MIN_RATIO = 0.25f
    private const val MAX_RATIO = 4f
    private const val DEFAULT_RATIO = 16f / 9f
    private const val DEFAULT_DURATION = 220
    private const val MAX_DURATION = 600
    private const val MAX_BLUR = 24f
    private const val TRANSITION_NONE = "none"
    private const val TRANSITION_FADE = "fade"
    private const val TRANSITION_FLIP_HORIZONTAL = "flipHorizontal"
    private const val TRANSITION_FLIP_VERTICAL = "flipVertical"
    private val HEX = Regex("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")
    private val MIME_TYPES = setOf("image/png", "image/jpeg", "image/webp")
    private const val MAX_BASE64_LENGTH = 174764
    private const val HEX_RADIX = 16
    private const val RGB_LENGTH = 7
    private const val OPAQUE_ALPHA = 0xFF000000L
    private const val ALPHA_MASK = 0xFFL
    private const val ALPHA_SHIFT = 24
    private const val BYTE_BITS = 8
}
