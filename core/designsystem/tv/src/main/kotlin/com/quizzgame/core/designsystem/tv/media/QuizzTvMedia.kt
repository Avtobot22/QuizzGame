package com.quizzgame.core.designsystem.tv.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.core.media.android.CatalogMediaLoader
import com.quizzgame.core.ui.media.QuizzCatalogArtwork

val LocalTvMediaLoader = staticCompositionLocalOf<CatalogMediaLoader> {
    error("Image loader must be provided by the application")
}

/** Изображение декоративное, его доступное имя предоставляет карточка или вкладка. */
@Composable
fun QuizzTvMedia(
    media: MediaSpec,
    modifier: Modifier = Modifier,
    localResource: Int? = null,
    placeholderBrush: Brush? = null,
) {
    if (media is MediaSpec.Icon && localResource != null) {
        Icon(
            painterResource(localResource),
            contentDescription = null,
            modifier = modifier,
            tint = media.presentation.tint.themeColor(LocalContentColor.current),
        )
        return
    }
    val colors = MaterialTheme.colorScheme
    val loader = if (media is MediaSpec.Remote || media is MediaSpec.Embedded) LocalTvMediaLoader.current else null
    QuizzCatalogArtwork(
        media,
        loader,
        colors.primary,
        colors.tertiary,
        modifier,
        placeholderBrush,
    )
}
