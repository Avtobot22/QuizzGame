package com.quizzgame.core.designsystem.mobile.media

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.core.media.android.CatalogMediaLoader
import com.quizzgame.core.ui.media.QuizzCatalogArtwork

val LocalMobileMediaLoader = staticCompositionLocalOf<CatalogMediaLoader> {
    error("Image loader must be provided by the application")
}

/** Изображение декоративное, его доступное имя предоставляет карточка или вкладка. */
@Composable
fun QuizzMobileMedia(
    media: MediaSpec,
    modifier: Modifier = Modifier,
    localResource: Int? = null,
    placeholderBrush: Brush? = null,
    fadeTo: Color? = null,
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
    val loader = if (media is MediaSpec.Remote || media is MediaSpec.Embedded) LocalMobileMediaLoader.current else null
    QuizzCatalogArtwork(
        media,
        loader,
        colors.primary,
        colors.tertiary,
        modifier,
        placeholderBrush,
        fadeTo,
    )
}
