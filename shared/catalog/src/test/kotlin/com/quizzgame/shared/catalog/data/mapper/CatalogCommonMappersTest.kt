package com.quizzgame.shared.catalog.data.mapper

import com.quizzgame.shared.catalog.data.dto.PassingStatusDto
import com.quizzgame.shared.catalog.data.dto.UserQuizStateDto
import com.quizzgame.shared.catalog.domain.entity.PassingStatus
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CatalogCommonMappersTest {
    @Test
    fun renderingClampsProgressButUnknownStatusAndNonfiniteProgressAreInvalid() {
        assertEquals(1.0, UserQuizStateDto(true, 2.0, PassingStatusDto.COMPLETED).toQuizUserState().progress, 0.0)
        assertEquals(
            PassingStatus.COMPLETED,
            UserQuizStateDto(true, 1.0, PassingStatusDto.COMPLETED).toQuizUserState().status,
        )
        assertThrows(
            SerializationException::class.java,
        ) { Json.decodeFromString<UserQuizStateDto>("""{"bookmarked":false,"progress":0,"passingStatus":"future"}""") }
        assertThrows(
            SerializationException::class.java,
        ) { UserQuizStateDto(false, Double.NaN, PassingStatusDto.NOT_STARTED).toQuizUserState() }
    }
}
