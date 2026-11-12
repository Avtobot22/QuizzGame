package com.quizzgame.app.routes

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AppRoutesTest {
    @Test
    fun applicationRoutesHaveGeneratedSerializers() {
        assertEquals(SplashRoute, roundTrip(SplashRoute.serializer(), SplashRoute))
        assertEquals(SignInRoute, roundTrip(SignInRoute.serializer(), SignInRoute))
        assertEquals(JoinRoomRoute, roundTrip(JoinRoomRoute.serializer(), JoinRoomRoute))
        assertEquals(DashboardRoute, roundTrip(DashboardRoute.serializer(), DashboardRoute))
    }

    private fun <T> roundTrip(serializer: KSerializer<T>, value: T): T =
        Json.decodeFromString(serializer, Json.encodeToString(serializer, value))
}
