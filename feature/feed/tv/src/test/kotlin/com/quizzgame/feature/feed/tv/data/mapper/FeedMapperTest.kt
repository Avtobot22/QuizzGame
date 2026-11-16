package com.quizzgame.feature.feed.tv.data.mapper

import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.feature.feed.tv.data.dto.FeedDto
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
        val card = decode(fixture()).rails.single().cards.single()
        assertNotEquals(card.id, card.entryId)
        assertEquals("58e2b5b5-05e6-43f5-1580-8e88ec7130fd", card.entryId)
    }

    @Test
    fun unknownSectionAndCardKindsAreSerializationErrors() {
        assertThrows(SerializationException::class.java) { decode("""{"sections":[{"kind":"future"}]}""") }
        val unknownCard = """{"sections":[{"kind":"rail","id":"58e2b5b5-05e6-43f5-9580-8e88ec7130fd",
          "items":[{"kind":"future"}]}]}"""
        assertThrows(SerializationException::class.java) { decode(unknownCard) }
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
        assertEquals(emptyList<Any>(), decode("""{"sections":[]}""").rails)
    }

    private fun decode(input: String) = mapper.map(json.decodeFromString<FeedDto>(input))
    private fun fixture(): String = checkNotNull(javaClass.getResource("/feed.json")).readText()
}
