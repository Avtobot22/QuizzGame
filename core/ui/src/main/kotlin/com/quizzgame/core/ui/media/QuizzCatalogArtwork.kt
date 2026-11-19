package com.quizzgame.core.ui.media

import android.animation.ValueAnimator
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.core.media.android.CatalogMediaLoader
import com.quizzgame.core.ui.LocalQuizzCatalogColors
import com.quizzgame.core.ui.QuizzSceneIllustration
import com.quizzgame.core.ui.R
import com.quizzgame.core.ui.tokens.QuizzCatalogDimensions

/** Декоративная обложка каталога с общей заглушкой и параметрами изображения из MediaSpec. */
@Composable
fun QuizzCatalogArtwork(
    media: MediaSpec,
    mediaLoader: CatalogMediaLoader?,
    primary: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    placeholderBrush: Brush? = null,
    fadeTo: Color? = null,
) {
    val catalogColors = LocalQuizzCatalogColors.current
    val brush = placeholderBrush
        ?: Brush.linearGradient(listOf(catalogColors.artworkStart, fadeTo ?: catalogColors.artworkEnd))
    BoxWithConstraints(modifier.background(brush), contentAlignment = Alignment.Center) {
        val illustrationSize = minOf(maxWidth, maxHeight) * 0.8f
        QuizzSceneIllustration(primary, accent, Modifier.size(illustrationSize)) {
            Image(
                painterResource(R.drawable.ic_quizz_quiz),
                contentDescription = null,
                modifier = Modifier.size(illustrationSize * 0.28f),
                colorFilter = ColorFilter.tint(primary),
            )
        }
        if (mediaLoader != null) CatalogImage(media, mediaLoader)
        if (fadeTo != null) {
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .height(QuizzCatalogDimensions.artworkFadeHeight.dp)
                    .background(Brush.verticalGradient(listOf(fadeTo.copy(alpha = 0f), fadeTo))),
            )
        }
    }
}

@Composable
private fun CatalogImage(media: MediaSpec, mediaLoader: CatalogMediaLoader) {
    val context = LocalContext.current
    val embedded by produceState<ByteArray?>(null, media) {
        value = (media as? MediaSpec.Embedded)?.let { mediaLoader.embeddedSource(it) }
    }
    val source = when (media) {
        is MediaSpec.Remote -> mediaLoader.remoteSource(media.url)
        is MediaSpec.Embedded -> embedded
        else -> null
    } ?: return
    val presentation = media.presentation
    val duration = if (presentation.transition != "none" && ValueAnimator.areAnimatorsEnabled()) {
        presentation.durationMs
    } else {
        0
    }
    AsyncImage(
        model = ImageRequest.Builder(context).data(source).crossfade(duration).build(),
        imageLoader = mediaLoader.loader,
        contentDescription = null,
        modifier = Modifier.fillMaxSize().blur(presentation.blurRadius.dp),
        contentScale = if (presentation.contain) ContentScale.Fit else ContentScale.Crop,
    )
}
