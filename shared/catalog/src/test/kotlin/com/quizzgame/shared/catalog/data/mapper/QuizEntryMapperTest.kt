package com.quizzgame.shared.catalog.data.mapper

import com.quizzgame.shared.catalog.data.dto.entry.QuizEntryDto
import com.quizzgame.shared.catalog.data.dto.entry.QuizStatsDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class QuizEntryMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun fixtureKeepsDetailsAndOnlyProvidesAnActionLabel() {
        val entry = json.decodeFromString<QuizEntryDto>(fixture()).toDomain()
        assertEquals("58e2b5b5-05e6-43f5-1580-8e88ec7130fd", entry.entryId)
        assertEquals(null, entry.questionCount)
        assertEquals("Продолжить", entry.actionTitle)
    }

    @Test
    fun invalidRequiredIdentifiersAvailabilityAndStatsAreRejected() {
        listOf(
            fixture().replace("58e2b5b5-05e6-43f5-1580-8e88ec7130fd", "invalid"),
            fixture().replace("\"available\"", "\"unknown\""),
        ).forEach { input ->
            assertThrows(SerializationException::class.java) { json.decodeFromString<QuizEntryDto>(input).toDomain() }
        }
    }

    @Test
    fun negativeOptionalStatsAreRejected() {
        val dto = json.decodeFromString<QuizEntryDto>(fixture()).copy(stats = QuizStatsDto(questionCount = -1))
        assertThrows(SerializationException::class.java) { dto.toDomain() }
    }

    private fun fixture(): String = checkNotNull(javaClass.getResource("/quiz-entry.json")).readText()
}
