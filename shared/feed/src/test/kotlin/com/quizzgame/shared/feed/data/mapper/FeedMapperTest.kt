package com.quizzgame.shared.feed.data.mapper

import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.shared.feed.data.dto.FeedDto
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FeedMapperTest {
    private val json = createNetworkJson()
    private val mapper = FeedMapper()

    @Test
    fun fixturePreservesEntryIdentifierSeparatelyFromSnippet() {
        val card = decode(fixture()).sections.filterIsInstance<QuizRail>().single().cards.single()
        assertNotEquals(card.id, card.entryId)
        assertEquals("58e2b5b5-05e6-43f5-1580-8e88ec7130fd", card.entryId)
    }

    @Test
    fun unknownSectionAndCardKindsAreSkipped() {
        val unknown = """{"sections":[{"kind":"future"}],"page":{"hasMore":false,"nextCursor":null}}"""
        assertEquals(emptyList<Any>(), decode(unknown).sections.filterIsInstance<QuizRail>())
        val card = fixture().replace("\"kind\": \"quiz\"", "\"kind\": \"future\"")
        assertEquals(emptyList<Any>(), decode(card).sections.filterIsInstance<QuizRail>().single().cards)
    }

    @Test
    fun malformedKnownPayloadAndUnsupportedMediaAreErrors() {
        assertThrows(
            SerializationException::class.java,
        ) { decode(fixture().replace("\"title\":", "\"removedTitle\":")) }
        assertThrows(SerializationException::class.java) { decode(fixture().replace("\"remote\"", "\"appleRemote\"")) }
    }

    @Test
    fun emptyFeedRemainsAValidResult() {
        assertEquals(
            emptyList<Any>(),
            decode(
                """{"sections":[],"page":{"hasMore":false,"nextCursor":null}}""",
            ).sections.filterIsInstance<QuizRail>(),
        )
    }

    @Test
    fun pageInvariantsAndEmptyContinuationAreRejected() {
        val badPages = listOf(
            """{"hasMore":false}""",
            """{"hasMore":true,"nextCursor":null}""",
            """{"hasMore":true,"nextCursor":""}""",
            """{"hasMore":false,"nextCursor":"cursor"}""",
            """{"hasMore":true,"nextCursor":"next"}""",
        )
        badPages.forEach { page ->
            assertThrows(kotlinx.serialization.SerializationException::class.java) {
                decode("""{"sections":[],"page":$page}""")
            }
        }
    }

    private fun decode(input: String) = mapper.map(json.decodeFromString<FeedDto>(input))
    private fun fixture(): String = checkNotNull(javaClass.getResource("/feed.json")).readText()
}
