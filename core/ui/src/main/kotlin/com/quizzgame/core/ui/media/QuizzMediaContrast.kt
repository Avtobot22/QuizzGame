package com.quizzgame.core.ui.media

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

/** Сохраняет цвет текста, если его контраст с фоном составляет хотя бы 4.5:1. */
fun Color.readableOn(background: Color, fallback: Color): Color {
    val candidate = compositeOver(background)
    val light = maxOf(candidate.luminance(), background.luminance())
    val dark = minOf(candidate.luminance(), background.luminance())
    return if ((light + CONTRAST_OFFSET) / (dark + CONTRAST_OFFSET) >= TEXT_CONTRAST) candidate else fallback
}

private const val CONTRAST_OFFSET = 0.05f
private const val TEXT_CONTRAST = 4.5f
