package com.quizzgame.debug.network.mocks

import dev.androidmock.core.dsl.RulesDsl
import dev.androidmock.core.request.HeaderEntry
import dev.androidmock.core.request.RequestBodySnapshot
import dev.androidmock.core.request.RequestSnapshot
import dev.androidmock.core.response.ResponseBody
import dev.androidmock.core.response.ResponseSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.time.Instant

internal const val AUTH_PATH = "/api/auth"
private const val CHALLENGE = "mock-otp"
private const val RESENT = "mock-resent"
private const val OTP = "012345"
private const val EXPIRED_CODE = "000000"
private const val RATE_LIMITED_CODE = "999999"
internal const val MOCK_SECRET = "mock-attempt-secret"
private const val CONTENT_TYPE = "application/json"
private const val RATE_LIMIT = 10
private const val EXPIRES_IN = 300L
private val INVALID_IDENTIFIER = 400 to 22004
internal val OAUTH_INVALID_ATTEMPT = 409 to 22007
private val OTP_EXPIRED = 410 to 22002
private val OTP_RATE_LIMITED = 429 to 22003
private val OTP_REJECTED = 422 to 22001

fun RulesDsl.authMockRules(json: Json, surface: String) {
    rule("auth-methods", "$AUTH_PATH/methods") {
        match { method("GET") }
        respondWith { request, _ ->
            if (request.url.query.any { it.name == "clientSurface" && it.value == surface }) {
                authJson(methods(surface))
            } else {
                authError(INVALID_IDENTIFIER)
            }
        }
    }
    rule("auth-challenge", "$AUTH_PATH/otp/challenges") {
        match { method("POST") }
        respondWith { request, _ -> challenge(CHALLENGE, readField(json, request, "method") == "otp.sms") }
    }
    otpRules(json, CHALLENGE, RESENT)
    otpRules(json, RESENT, CHALLENGE)
    if (surface == "tv") deviceAuthRules(json)
}

private fun RulesDsl.otpRules(json: Json, id: String, next: String) {
    rule("auth-verify-$id", "$AUTH_PATH/otp/challenges/$id/verify") {
        match { method("POST") }
        respondWith { request, _ -> verify(json, request) }
    }
    rule("auth-resend-$id", "$AUTH_PATH/otp/challenges/$id/resend") {
        match { method("POST") }
        respondWith { _, _ -> challenge(next, false) }
    }
}

private fun verify(json: Json, request: RequestSnapshot): ResponseSpec {
    if (request.headers.values("Idempotency-Key").singleOrNull().isNullOrBlank()) return authError(INVALID_IDENTIFIER)
    return when (readField(json, request, "code")) {
        OTP -> MOCK_TOKEN_PAIR
        EXPIRED_CODE -> authError(OTP_EXPIRED)
        RATE_LIMITED_CODE -> authError(OTP_RATE_LIMITED, RATE_LIMIT)
        else -> authError(OTP_REJECTED)
    }
}

private fun methods(surface: String): String {
    val otp = """{"id":"otp.email","enabled":true,"displayName":"Email"},""" +
        """{"id":"otp.sms","enabled":true,"displayName":"SMS"}"""
    val google = if (surface == "tv") """,{"id":"oauth.google","enabled":true,"displayName":"Google"}""" else ""
    return """{"methods":[$otp$google],"guestCatalogAvailable":true}"""
}

internal fun readField(json: Json, request: RequestSnapshot, name: String): String? {
    val body = request.body as? RequestBodySnapshot.Buffered ?: return null
    return runCatching {
        (json.parseToJsonElement(body.bytes.decodeToString()).jsonObject[name] as? JsonPrimitive)?.content
    }.getOrNull()
}

private fun challenge(id: String, sms: Boolean): ResponseSpec {
    val mask = if (sms) "+1 *** **45" else "g***@example.test"
    return authJson(
        """{"challengeId":"$id","maskedDestination":"$mask","codeLength":6,""" +
            """"expiresAt":"${expiresAt()}","resendAfterSeconds":5,"attemptsRemaining":5}""",
    )
}

internal val MOCK_TOKEN_PAIR = authJson("""{"access":"mock-auth-access","refresh":"mock-auth-refresh"}""")
internal fun expiresAt() = Instant.now().plusSeconds(EXPIRES_IN)
internal fun authError(error: Pair<Int, Int>, retryAfter: Int? = null) =
    authJson("""{"reasonCode":${error.second},"debugMessage":"fixture"}""", error.first, retryAfter)

internal fun authJson(body: String, status: Int = 200, retryAfter: Int? = null) = ResponseSpec(
    status = status,
    headers = listOfNotNull(
        HeaderEntry("Content-Type", CONTENT_TYPE),
        retryAfter?.let { HeaderEntry("Retry-After", it.toString()) },
    ),
    body = ResponseBody.Bytes(body.encodeToByteArray()),
)
