package com.quizzgame.shared.splash.data.dto

import com.quizzgame.shared.splash.data.mapper.instant
import com.quizzgame.shared.splash.data.mapper.toDomain
import com.quizzgame.shared.splash.data.mapper.validated
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class SplashDtosTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun launchAcceptsCanonicalUuidAndIgnoresUnknownFields() {
        val dto = json.decodeFromString<LaunchDto>("""{"id":"$USER_ID","future":true}""")
        assertEquals(USER_ID, dto.validated().toString())
    }

    @Test
    fun launchRejectsMalformedAndAbbreviatedUuid() {
        for (value in listOf("not-a-uuid", "1-1-1-1-1", "")) {
            assertThrows(SerializationException::class.java) { LaunchDto(value).validated() }
        }
    }

    @Test
    fun profileAcceptsEmptyRolesProfileAndNullableContactFields() {
        val user = json.decodeFromString<UserDto>(VALID_USER).toDomain()
        assertEquals(Instant.parse("2026-11-05T10:30:00Z"), user.createdAt)
        assertEquals(emptyList<String>(), user.roles)
        assertEquals(null, user.displayName)
        assertEquals(null, user.email)
    }

    @Test
    fun timestampsRequireIsoStringWithTimezoneAndDoNotAcceptAppleEpoch() {
        for (value in listOf("123456", "2026-11-05T10:30:00", "invalid")) {
            assertThrows(SerializationException::class.java) { AccountDto(value).instant() }
        }
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<UserDto>(VALID_USER.replace("\"2026-11-05T10:30:00Z\"", "123456"))
        }
    }

    @Test
    fun missingAccountAndCreatedAtAreInvalidResponses() {
        val missing = listOf(
            """{"id":"$USER_ID","profile":{},"roles":[]}""",
            """{"id":"$USER_ID","account":{},"profile":{},"roles":[]}""",
        )
        for (payload in missing) {
            assertThrows(SerializationException::class.java) { json.decodeFromString<UserDto>(payload) }
        }
    }

    private companion object {
        const val USER_ID = "719278b4-e7af-4e50-af40-25701f2b2b27"
        val VALID_USER = """{"id":"$USER_ID","account":{"createdAt":"2026-11-05T10:30:00Z"},"profile":{},"roles":[]}"""
    }
}
