package com.quizzgame.feature.feed.mobile.ui.preview

import com.quizzgame.core.media.MediaSpec
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge
import com.quizzgame.shared.catalog.domain.entity.PassingStatus
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.catalog.domain.entity.QuizUserState
import com.quizzgame.shared.feed.domain.entity.Feed
import com.quizzgame.shared.feed.domain.entity.section.QuizHero
import com.quizzgame.shared.feed.domain.entity.section.QuizRail

internal object MobileFeedPreviewFixtures {
    val rail = QuizRail(
        id = "popular",
        title = "Популярные викторины",
        subtitle = "Для вечера с друзьями",
        cards = PassingStatus.entries.mapIndexed { index, status ->
            QuizCard(
                id = "quiz-$index",
                entryId = "entry-$index",
                title = if (index == 0) "Кино и сериалы" else "Большой квиз о путешествиях и открытиях",
                subtitle = if (index == 0) "От киноклассики до сериалов" else "Страны, города и маршруты",
                image = MediaSpec.Asset("quiz.cover"),
                badge = if (index == 0) CatalogBadge("Новинка", true, null, null) else null,
                user = QuizUserState(index != 0, if (status == PassingStatus.IN_PROGRESS) 0.45 else 0.0, status),
            )
        },
    )
    val feed = Feed(
        listOf(
            QuizHero("featured", rail.cards.first().copy(id = "hero-card", entryId = "hero-entry")),
            rail,
        ),
    )
}
