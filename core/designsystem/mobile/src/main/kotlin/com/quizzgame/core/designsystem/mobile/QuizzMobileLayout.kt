package com.quizzgame.core.designsystem.mobile

import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.tokens.QuizzCatalogDimensions

/** Геометрия мобильных страниц и карточек; контент сохраняет системный масштаб текста. */
object QuizzMobileLayout {
    val pageGutter = 20.dp
    val contentWidth = 600.dp
    val dialogWidth = 400.dp
    val railCardWidth = 208.dp
    const val COVER_ASPECT_RATIO = QuizzCatalogDimensions.artworkAspectRatio
}
