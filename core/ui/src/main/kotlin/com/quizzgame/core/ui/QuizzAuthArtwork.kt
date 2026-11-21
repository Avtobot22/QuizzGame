package com.quizzgame.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

/** Символ этапа входа в фирменной иллюстрации. Логотип провайдера сохраняет свои цвета. */
@Composable
fun QuizzAuthArtwork(
    @DrawableRes icon: Int,
    primary: Color,
    accent: Color,
    size: Dp,
    iconSize: Dp,
    branded: Boolean = false,
) {
    QuizzSceneIllustration(primary, accent, Modifier.size(size)) {
        Image(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            colorFilter = if (branded) null else ColorFilter.tint(primary),
        )
    }
}
