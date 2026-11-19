package com.quizzgame.debug.network.mocks

import dev.androidmock.core.dsl.RulesDsl
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

internal fun RulesDsl.deviceAuthRules(json: Json) {
    val polls = AtomicInteger()
    val path = "$AUTH_PATH/device/requests"
    rule("tv-device-auth-request", path) {
        match { method("POST") }
        respondWith { _, _ ->
            polls.set(0)
            authJson(
                """{"requestId":"$MOCK_TV_REQUEST","userCode":"$MOCK_TV_CODE",""" +
                    """"verificationUri":"$MOCK_TV_URI",""" +
                    """"verificationUriComplete":"$MOCK_TV_URI?user_code=$MOCK_TV_CODE",""" +
                    """"deviceSecret":"$MOCK_SECRET","expiresAt":"${expiresAt()}","pollIntervalSeconds":2}""",
            )
        }
    }
    rule("tv-device-auth-status", "$path/$MOCK_TV_REQUEST") {
        match { method("GET") }
        respondWith { request, _ ->
            val secret = request.headers.values("X-Device-Authorization-Secret").singleOrNull()
            when {
                secret != MOCK_SECRET -> authError(OAUTH_INVALID_ATTEMPT)
                polls.incrementAndGet() == 1 -> authJson("""{"status":"pending"}""")
                else -> authJson("""{"status":"approved"}""")
            }
        }
    }
    rule("tv-device-auth-redeem", "$path/$MOCK_TV_REQUEST/redeem") {
        match { method("POST") }
        respondWith { request, _ ->
            if (readField(json, request, "deviceSecret") == MOCK_SECRET && polls.get() >= 2) {
                MOCK_TOKEN_PAIR
            } else {
                authError(OAUTH_INVALID_ATTEMPT)
            }
        }
    }
}

private const val MOCK_TV_REQUEST = "mock-tv-request"
private const val MOCK_TV_CODE = "ABCD-2345"
private const val MOCK_TV_URI = "https://auth.mock.invalid/device"
