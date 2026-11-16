package com.quizzgame.feature.feed.tv.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.feature.feed.tv.domain.entity.Feed
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge
import com.quizzgame.shared.catalog.domain.entity.PassingStatus
import com.quizzgame.shared.catalog.domain.entity.QuizUserState

internal class TvFeedPreviewProvider : PreviewParameterProvider<TvFeedPreviewState> {
    private val feed = Feed(
        rails = listOf(
            QuizRail(
                id = "popular",
                title = "Популярные квизы",
                subtitle = "Для игры с друзьями",
                cards = PassingStatus.entries.mapIndexed { index, status ->
                    QuizCard(
                        id = "quiz-$index",
                        entryId = "entry-$index",
                        title = if (index == 0) "Кино и сериалы" else "Большой квиз о путешествиях и открытиях",
                        subtitle = "Проверьте свои знания",
                        image = MediaSpec.Placeholder,
                        badge = if (index == 0) CatalogBadge("Новинка", true, null, null) else null,
                        user = QuizUserState(
                            index == 0,
                            if (status == PassingStatus.IN_PROGRESS) 0.45 else 0.0,
                            status,
                        ),
                    )
                },
            ),
        ),
    )

    override val values: Sequence<TvFeedPreviewState> = sequenceOf(
        TvFeedPreviewState.Loading,
        TvFeedPreviewState.Empty,
        TvFeedPreviewState.Error(
            ErrorPresentationSpec(
                ErrorSurface.BLOCKING,
                ErrorCopyKey.CONNECTION_PROBLEM,
                actions = listOf(ErrorActionKind.RETRY),
            ),
        ),
        TvFeedPreviewState.Content(feed),
        TvFeedPreviewState.Content(feed, refreshing = true),
        TvFeedPreviewState.Content(feed, appendingSections = true),
        TvFeedPreviewState.Content(
            feed.copy(rails = feed.rails.map { it.copy(cards = it.cards.take(1)) }),
            appendingRails = setOf("popular"),
        ),
        TvFeedPreviewState.Content(
            feed,
            error = ErrorPresentationSpec(
                ErrorSurface.BANNER,
                ErrorCopyKey.CONNECTION_PROBLEM,
                actions = listOf(ErrorActionKind.RETRY, ErrorActionKind.DISMISS),
            ),
        ),
    )
}
