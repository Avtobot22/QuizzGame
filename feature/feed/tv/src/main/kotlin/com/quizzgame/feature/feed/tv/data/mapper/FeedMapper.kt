package com.quizzgame.feature.feed.tv.data.mapper

import com.quizzgame.core.media.AppearanceMapper
import com.quizzgame.feature.feed.tv.data.dto.FeedDto
import com.quizzgame.feature.feed.tv.data.dto.QuizCardDto
import com.quizzgame.feature.feed.tv.data.dto.RailDto
import com.quizzgame.feature.feed.tv.domain.entity.Feed
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import com.quizzgame.shared.catalog.data.mapper.catalogId
import com.quizzgame.shared.catalog.data.mapper.requireCatalog
import com.quizzgame.shared.catalog.data.mapper.toCatalogBadge
import com.quizzgame.shared.catalog.data.mapper.toCatalogMedia
import com.quizzgame.shared.catalog.data.mapper.toQuizUserState
import javax.inject.Inject

internal class FeedMapper @Inject constructor() {
    fun map(dto: FeedDto): Feed {
        val rails = dto.sections.map { section ->
            when (section) {
                is RailDto -> mapRail(section)
            }
        }
        requireCatalog(rails.map { it.id }.distinct().size == rails.size)
        return Feed(rails.filter { it.cards.isNotEmpty() })
    }

    private fun mapRail(dto: RailDto): QuizRail {
        val cards = dto.items.map { item ->
            when (item) {
                is QuizCardDto -> mapCard(item)
            }
        }
        requireCatalog(cards.map { it.id }.distinct().size == cards.size)
        return QuizRail(catalogId(dto.id), dto.title, dto.subtitle, cards)
    }

    private fun mapCard(dto: QuizCardDto): QuizCard {
        requireCatalog(dto.title.isNotBlank())
        return QuizCard(
            catalogId(dto.id), catalogId(dto.entryId), dto.title, dto.subtitle, dto.image.toCatalogMedia(),
            AppearanceMapper.color(dto.style?.titleColor), AppearanceMapper.color(dto.style?.subtitleColor),
            dto.badge?.toCatalogBadge(), dto.user.toQuizUserState(),
        )
    }
}
