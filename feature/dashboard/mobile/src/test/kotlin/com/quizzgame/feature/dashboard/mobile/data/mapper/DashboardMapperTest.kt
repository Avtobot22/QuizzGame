package com.quizzgame.feature.dashboard.mobile.data.mapper

import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.feature.dashboard.mobile.data.dto.DashboardDto
import com.quizzgame.feature.dashboard.mobile.data.dto.DashboardTabDto
import com.quizzgame.feature.dashboard.mobile.domain.entity.MobileTabId
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DashboardMapperTest {
    private val mapper = DashboardMapper()
    private fun fixture() = createNetworkJson().decodeFromString<DashboardDto>(
        checkNotNull(javaClass.getResource("/mobile-dashboard.json")).readText(),
    )

    @Test
    fun knownOrderIsPreservedAndUnknownTabsAreIgnored() {
        val dto = fixture()
        val remote = dto.copy(tabs = dto.tabs.reversed() + DashboardTabDto("future", "Future", true))
        assertEquals(MobileTabId.entries.reversed(), mapper.map(remote).tabs.map { it.id })
    }

    @Test
    fun missingDisabledPlayAndDuplicateKnownIdsAreInvalid() {
        val dto = fixture()
        listOf(
            dto.copy(tabs = dto.tabs.drop(1)),
            dto.copy(tabs = dto.tabs.map { if (it.id == "play") it.copy(enabled = false) else it }),
            dto.copy(tabs = dto.tabs + dto.tabs.first()),
            dto.copy(tabs = emptyList()),
        ).forEach { invalid -> assertThrows(SerializationException::class.java) { mapper.map(invalid) } }
    }

    @Test
    fun disabledOptionalTabIsHiddenWithoutChangingOtherIds() {
        val dto = fixture().let { it.copy(tabs = it.tabs.map { tab -> tab.copy(enabled = tab.id != "library") }) }
        assertEquals(
            listOf(MobileTabId.PLAY, MobileTabId.EXPLORE, MobileTabId.PROFILE),
            mapper.map(dto).tabs.map { it.id },
        )
    }
}
