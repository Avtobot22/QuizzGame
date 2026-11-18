package com.quizzgame.core.designsystem.tv.media

import android.animation.ValueAnimator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.core.media.android.CatalogMediaLoader
import com.quizzgame.core.ui.LocalQuizzCatalogColors
import com.quizzgame.core.ui.QuizzSceneIllustration
import com.quizzgame.core.ui.R as UiR

val LocalTvMediaLoader = staticCompositionLocalOf<CatalogMediaLoader> {
    error("Image loader must be provided by the application")
}

/** Изображение декоративное: доступное имя предоставляет карточка или вкладка. */
@Composable
fun QuizzTvMedia(
    media: MediaSpec,
    modifier: Modifier = Modifier,
    localResource: Int? = null,
    placeholderBrush: Brush? = null,
) {
    val mediaLoader = if (media is MediaSpec.Remote || media is MediaSpec.Embedded) LocalTvMediaLoader.current else null
    val context = LocalContext.current
    val embedded by produceState<ByteArray?>(null, media) {
        value = (media as? MediaSpec.Embedded)?.let { mediaLoader?.embeddedSource(it) }
    }
    val source = when (media) {
        is MediaSpec.Remote -> mediaLoader?.remoteSource(media.url)
        is MediaSpec.Embedded -> embedded
        else -> null
    }
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
    val catalogColors = LocalQuizzCatalogColors.current
    val brush = placeholderBrush ?: Brush.linearGradient(listOf(catalogColors.artworkStart, catalogColors.artworkEnd))
    BoxWithConstraints(
        modifier.background(brush),
        contentAlignment = Alignment.Center,
    ) {
        val illustrationSize = minOf(maxWidth, maxHeight) * 0.8f
        QuizzSceneIllustration(
            colors.primary,
            colors.tertiary,
            Modifier.size(illustrationSize),
        ) {
            Icon(
                painterResource(UiR.drawable.ic_quizz_quiz),
                null,
                Modifier.size(illustrationSize * 0.28f),
                tint = colors.primary,
            )
        }
        if (source != null) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(source)
                    .crossfade(
                        if (media.presentation.transition != "none" && ValueAnimator.areAnimatorsEnabled()) {
                            media.presentation.durationMs
                        } else {
                            0
                        },
                    ).build(),
                imageLoader = checkNotNull(mediaLoader).loader,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(media.presentation.blurRadius.dp),
                contentScale = if (media.presentation.contain) ContentScale.Fit else ContentScale.Crop,
            )
        }
    }
}
