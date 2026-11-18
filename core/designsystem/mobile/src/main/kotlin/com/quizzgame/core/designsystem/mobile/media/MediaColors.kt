package com.quizzgame.core.designsystem.mobile.media

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.quizzgame.core.media.ColorSpec

/** Семантическая роль и тема вычисляются при каждой смене темы. */
@Composable
fun ColorSpec?.themeColor(fallback: Color): Color {
    return when (this) {
        is ColorSpec.Hex -> Color(if (isSystemInDarkTheme()) dark else light)
        is ColorSpec.Token -> semanticColor(name, fallback)
        null -> fallback
    }
}

@Composable
private fun semanticColor(name: String, fallback: Color): Color {
    val colors = MaterialTheme.colorScheme
    return when (name) {
        "text.primary" -> colors.onSurface
        "text.secondary" -> colors.onSurfaceVariant
        "surface.primary" -> colors.surface
        "surface.secondary" -> colors.surfaceContainerHigh
        "accent.primary" -> colors.primary
        "status.success" -> colors.tertiary
        "status.warning" -> colors.secondary
        "status.error" -> colors.error
        "onAccent" -> colors.onPrimary
        else -> fallback
    }
}

@Composable
fun ColorSpec?.readableColor(fallback: Color, background: Color): Color {
    val candidate = themeColor(fallback).compositeOver(background)
    val light = maxOf(candidate.luminance(), background.luminance())
    val dark = minOf(candidate.luminance(), background.luminance())
    return if ((light + CONTRAST_OFFSET) / (dark + CONTRAST_OFFSET) >= TEXT_CONTRAST) candidate else fallback
}

private const val CONTRAST_OFFSET = 0.05f
private const val TEXT_CONTRAST = 4.5f
