package com.quizzgame.feature.feed.tv.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.designsystem.tv.media.QuizzTvBadge
import com.quizzgame.core.designsystem.tv.media.QuizzTvMedia
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard

@Composable
internal fun TvQuizCardArtwork(card: QuizCard) {
    val colors = MaterialTheme.colorScheme
    Box {
        QuizzTvMedia(
            card.image,
            Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            placeholderBrush = Brush.linearGradient(listOf(colors.primaryContainer, colors.surface)),
        )
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(QuizzTvLayout.cardImageFadeHeight.dp)
                .background(Brush.verticalGradient(listOf(colors.surface.copy(alpha = 0f), colors.surface))),
        )
        card.badge?.let {
            QuizzTvBadge(
                it.text,
                it.live,
                Modifier.align(Alignment.TopStart).padding(10.dp),
                foreground = it.foreground,
                background = it.background,
            )
        }
    }
}
