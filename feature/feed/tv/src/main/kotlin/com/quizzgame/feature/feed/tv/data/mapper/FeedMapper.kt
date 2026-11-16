package com.quizzgame.feature.feed.tv.data.mapper

import com.quizzgame.core.media.AppearanceMapper
import com.quizzgame.feature.feed.tv.data.dto.FeedDto
import com.quizzgame.feature.feed.tv.data.dto.item.IgnoredFeedItemDto
import com.quizzgame.feature.feed.tv.data.dto.item.QuizCardDto
import com.quizzgame.feature.feed.tv.data.dto.pagination.CursorPageInfoDto
import com.quizzgame.feature.feed.tv.data.dto.section.IgnoredFeedSectionDto
import com.quizzgame.feature.feed.tv.data.dto.section.RailDto
import com.quizzgame.feature.feed.tv.data.dto.section.RailItemsDto
import com.quizzgame.feature.feed.tv.domain.entity.CursorPageInfo
import com.quizzgame.feature.feed.tv.domain.entity.Feed
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import com.quizzgame.feature.feed.tv.domain.entity.RailItems
import com.quizzgame.shared.catalog.data.mapper.catalogId
import com.quizzgame.shared.catalog.data.mapper.requireCatalog
import com.quizzgame.shared.catalog.data.mapper.toCatalogBadge
import com.quizzgame.shared.catalog.data.mapper.toCatalogMedia
import com.quizzgame.shared.catalog.data.mapper.toQuizUserState
import javax.inject.Inject

internal class FeedMapper @Inject constructor() {
    fun map(dto: FeedDto): Feed {
        val rails = dto.sections.mapNotNull { section ->
            when (section) {
                is RailDto -> mapRail(section)
                IgnoredFeedSectionDto -> null
            }
        }
        requireCatalog(rails.map { it.id }.distinct().size == rails.size)
        return Feed(rails, mapPage(dto.page, dto.sections.isEmpty()))
    }

    fun mapItems(dto: RailItemsDto, sectionId: String): RailItems {
        requireCatalog(catalogId(dto.sectionId) == sectionId)
        val rail = mapRail(RailDto(dto.sectionId, dto.items, dto.page))
        return RailItems(rail.id, rail.cards, rail.itemsPage)
    }

    private fun mapPage(dto: CursorPageInfoDto, empty: Boolean): CursorPageInfo {
        requireCatalog(
            if (dto.hasMore) {
                !dto.nextCursor.isNullOrBlank() && dto.nextCursor.length <= MAX_CURSOR_LENGTH
            } else {
                dto.nextCursor == null
            },
        )
        requireCatalog(!empty || !dto.hasMore)
        return CursorPageInfo(dto.hasMore, dto.nextCursor)
    }

    private fun mapRail(dto: RailDto): QuizRail {
        val cards = dto.items.mapNotNull { item ->
            when (item) {
                is QuizCardDto -> mapCard(item)
                IgnoredFeedItemDto -> null
            }
        }
        requireCatalog(cards.map { it.id }.distinct().size == cards.size)
        return QuizRail(catalogId(dto.id), dto.title, dto.subtitle, cards, mapPage(dto.itemsPage, dto.items.isEmpty()))
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

private const val MAX_CURSOR_LENGTH = 2048
