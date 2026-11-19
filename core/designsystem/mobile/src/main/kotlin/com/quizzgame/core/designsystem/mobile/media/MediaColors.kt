package com.quizzgame.core.designsystem.mobile.media

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.quizzgame.core.media.ColorSpec
import com.quizzgame.core.ui.media.readableOn

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
fun ColorSpec?.readableColor(fallback: Color, background: Color): Color =
    themeColor(fallback).readableOn(background, fallback)
