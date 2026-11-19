package com.quizzgame.shared.feed.data.mapper

import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.shared.feed.data.dto.FeedDto
import com.quizzgame.shared.feed.data.dto.section.RailItemsDto
import com.quizzgame.shared.feed.domain.entity.section.QuizHero
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileFeedContractTest {
    private val json = createNetworkJson()
    private val mapper = FeedMapper()
    private fun text(name: String) = checkNotNull(javaClass.getResource("/$name")).readText()

    @Test
    fun mobileHeroAndRailReuseTheSameCardContractAsTv() {
        val feed = mapper.map(json.decodeFromString<FeedDto>(text("mobile-feed-page-1.json")))
        assertTrue(feed.sections.first() is QuizHero)
        val rail = feed.sections.last() as QuizRail
        assertNotEquals(rail.cards.single().id, rail.cards.single().entryId)
        assertEquals("opaque-mobile-rail-2", rail.itemsPage.nextCursor)
    }

    @Test
    fun continuationRequiresSectionIdAndChecksItAgainstRequest() {
        val text = text("mobile-feed-rail-page-2.json")
        val dto = json.decodeFromString<RailItemsDto>(text)
        assertEquals(1, mapper.mapItems(dto, dto.sectionId).cards.size)
        assertThrows(SerializationException::class.java) { mapper.mapItems(dto, "wrong-section") }
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<RailItemsDto>(text.replace("sectionId", "removedSectionId"))
        }
    }
}
