package com.quizzgame.feature.auth.mobile.oauth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YandexAppLinkTest {
    @Test
    fun onlyTheConfiguredHttpsHostPathAndSinglePublicIdAreAccepted() {
        assertEquals("attempt-1", YandexAppLink.parse("login.example.com", BASE))
        listOf(
            BASE.replace("https://", "http://"),
            BASE.replace("login.example.com", "login.example.com.attacker.test"),
            BASE.replace("login.example.com", "user@login.example.com"),
            "$BASE&attemptId=spoof",
            "$BASE&attemptSecret=secret",
            "$BASE#token",
            BASE.replace("/auth/yandex", "/oauth/yandex"),
        ).forEach { assertNull(YandexAppLink.parse("login.example.com", it)) }
    }

    private companion object {
        const val BASE = "https://login.example.com/auth/yandex?attemptId=attempt-1"
    }
}
