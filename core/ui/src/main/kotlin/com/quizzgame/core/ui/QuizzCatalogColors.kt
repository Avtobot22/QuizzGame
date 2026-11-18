package com.quizzgame.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.quizzgame.core.ui.tokens.QuizzPalette

/** Семантические роли каталога Mobile/TV. Бейджи сохраняют фирменный цвет в обеих темах. */
data class QuizzCatalogColors(
    val badge: Color,
    val onBadge: Color,
    val liveBadge: Color,
    val onLiveBadge: Color,
    val artworkStart: Color,
    val artworkEnd: Color,
)

object QuizzCatalogPalettes {
    val Dark = QuizzCatalogColors(
        badge = QuizzPalette.secondary30,
        onBadge = QuizzPalette.secondary90,
        liveBadge = QuizzPalette.tertiary80,
        onLiveBadge = QuizzPalette.tertiary20,
        artworkStart = QuizzPalette.primary30,
        artworkEnd = QuizzPalette.neutral6,
    )
    val Light = Dark.copy(artworkStart = QuizzPalette.primary90, artworkEnd = QuizzPalette.neutral98)
}

val LocalQuizzCatalogColors = staticCompositionLocalOf<QuizzCatalogColors> {
    error("Wrap content in QuizzMobileTheme or QuizzTvTheme")
}
