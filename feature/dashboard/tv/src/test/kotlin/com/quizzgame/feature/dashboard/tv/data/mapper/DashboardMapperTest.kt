package com.quizzgame.feature.dashboard.tv.data.mapper

import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.feature.dashboard.tv.data.dto.DashboardDto
import com.quizzgame.feature.dashboard.tv.data.dto.DashboardTabDto
import com.quizzgame.feature.dashboard.tv.data.dto.DashboardTabIdDto
import com.quizzgame.feature.dashboard.tv.data.dto.NavigationAppearanceDto
import com.quizzgame.feature.dashboard.tv.data.dto.NavigationBackgroundDto
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTabId
import com.quizzgame.feature.dashboard.tv.navigation.DashboardTabRoute
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class DashboardMapperTest {
    private val json = createNetworkJson()
    private val mapper = DashboardMapper()

    @Test
    fun fixturePreservesServerOrderAndDisabledTabs() {
        val result = decode(fixture())
        assertEquals(DashboardTabId.entries, result.tabs.map { it.id })
        assertFalse(result.tabs.last().enabled)
    }

    @Test
    fun unknownTabAndDuplicateIdentifiersAreInvalid() {
        val source = json.decodeFromString<DashboardDto>(fixture())
        val tab = source.tabs.first()
        assertThrows(SerializationException::class.java) { mapper.map(source.copy(tabs = listOf(tab, tab))) }
        assertThrows(SerializationException::class.java) {
            decode(fixture().replace("\"feed\"", "\"future\""))
        }
    }

    @Test
    fun mapperAcceptsTypedTabsAndAppearanceWithoutJson() {
        val source = DashboardDto(
            id = "7c324c71-9cd5-4235-887b-a7800c39a401",
            tabs = listOf(DashboardTabDto(id = DashboardTabIdDto.FEED, title = "Feed", enabled = true)),
            navigationAppearance = NavigationAppearanceDto(background = NavigationBackgroundDto.TRANSPARENT),
        )
        assertEquals("transparent", mapper.map(source).appearance.background)
    }

    @Test
    fun unsupportedMediaKindAndAppearanceEnumFailDeserialization() {
        val unknownMedia = fixture().replace("\"kind\": \"icon\"", "\"kind\": \"future\"")
        assertThrows(SerializationException::class.java) { decode(unknownMedia) }
        assertThrows(SerializationException::class.java) { decode(fixture().replace("\"solid\"", "\"future\"")) }
    }

    @Test
    fun everyTypedTabRouteSurvivesSerialization() {
        DashboardTabId.entries.forEach { id ->
            val route = DashboardTabRoute(id)
            assertEquals(route, json.decodeFromString<DashboardTabRoute>(json.encodeToString(route)))
        }
    }

    private fun decode(input: String) = mapper.map(json.decodeFromString<DashboardDto>(input))
    private fun fixture(): String = checkNotNull(javaClass.getResource("/dashboard.json")).readText()
}
