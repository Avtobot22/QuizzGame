package com.quizzgame.shared.feed.data.mapper

import com.quizzgame.shared.catalog.data.mapper.catalogId
import com.quizzgame.shared.catalog.data.mapper.requireCatalog
import com.quizzgame.shared.catalog.data.mapper.toCursorPage
import com.quizzgame.shared.catalog.data.mapper.toQuizCardOrNull
import com.quizzgame.shared.feed.data.dto.FeedDto
import com.quizzgame.shared.feed.data.dto.section.HeroDto
import com.quizzgame.shared.feed.data.dto.section.IgnoredFeedSectionDto
import com.quizzgame.shared.feed.data.dto.section.RailDto
import com.quizzgame.shared.feed.data.dto.section.RailItemsDto
import com.quizzgame.shared.feed.domain.entity.Feed
import com.quizzgame.shared.feed.domain.entity.RailItems
import com.quizzgame.shared.feed.domain.entity.section.QuizHero
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import javax.inject.Inject

class FeedMapper @Inject constructor() {
    fun map(dto: FeedDto): Feed {
        val sections = dto.sections.mapNotNull { section ->
            when (section) {
                is HeroDto -> section.item.toQuizCardOrNull()?.let { QuizHero(catalogId(section.id), it) }
                is RailDto -> mapRail(section)
                IgnoredFeedSectionDto -> null
            }
        }
        requireCatalog(sections.map { it.id }.distinct().size == sections.size)
        requireCatalog(sections.count { it is QuizHero } <= 1)
        return Feed(sections, dto.page.toCursorPage(dto.sections.isEmpty()))
    }
    fun mapItems(dto: RailItemsDto, sectionId: String): RailItems {
        requireCatalog(catalogId(dto.sectionId) == sectionId)
        val rail = mapRail(RailDto(sectionId, dto.items, dto.page))
        return RailItems(rail.id, rail.cards, rail.itemsPage)
    }
    private fun mapRail(dto: RailDto): QuizRail {
        val cards = dto.items.mapNotNull { it.toQuizCardOrNull() }
        requireCatalog(cards.map { it.id }.distinct().size == cards.size)
        return QuizRail(
            catalogId(dto.id),
            dto.title,
            dto.subtitle,
            cards,
            dto.itemsPage.toCursorPage(dto.items.isEmpty()),
        )
    }
}
