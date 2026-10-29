package com.quizzgame.core.session.api

import com.quizzgame.core.network.NetworkTimeouts
import com.quizzgame.core.session.FakeSessionStore
import com.quizzgame.core.session.SessionManager
import com.quizzgame.core.session.TokenPair
import com.quizzgame.core.session.api.dto.RefreshRequestDto
import com.quizzgame.core.session.api.dto.RefreshResponseDto
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_MOVED_TEMP
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.HttpURLConnection.HTTP_UNAVAILABLE
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal const val DEVICE_ID = "e4a508be-bb66-46d3-91f6-7b4cabfc977b"
internal const val REFRESH_PATH = "api/tv/auth/refresh"
internal const val VALID_PAIR = """{"access":"fixture-access-2","refresh":"fixture-refresh-2","extra":true}"""

class RefreshClientTest : RefreshTestFixture() {

    @Test
    fun `refresh sends the exact contract without reading access credentials`() {
        val fixture = open()
        fixture.server.enqueue(MockResponse().setBody(VALID_PAIR))
        val tokens = fixture.client.newCall("fixture-sensitive-refresh\"\\", DEVICE_ID).execute()
        assertEquals("fixture-access-2", tokens.access)
        assertEquals("fixture-refresh-2", tokens.refresh)
        val request = fixture.server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("POST", request.method)
        assertEquals("/$REFRESH_PATH", request.path)
        assertNull(request.getHeader("Authorization"))
        assertEquals("application/json", request.getHeader("Accept"))
        assertEquals("application/json; charset=utf-8", request.getHeader("Content-Type"))
        assertEquals("test-tv", request.getHeader("X-Client-Platform"))
        assertEquals("refresh-tests/1", request.getHeader("User-Agent"))
        assertEquals("1", request.getHeader("X-Endpoint-Version"))
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals(setOf("refreshToken", "deviceId"), body.keys)
        assertEquals("fixture-sensitive-refresh\"\\", body["refreshToken"]!!.jsonPrimitive.content)
        assertEquals(DEVICE_ID, body["deviceId"]!!.jsonPrimitive.content)
        assertEquals(1, fixture.server.requestCount)
        assertEquals(1, fixture.releasedConnections.get())
    }

    @Test
    fun `HTTP failure and redirect never retry refresh or expose the error body`() {
        listOf(HTTP_UNAUTHORIZED, HTTP_INTERNAL_ERROR, HTTP_UNAVAILABLE, HTTP_MOVED_TEMP).forEach { status ->
            val fixture = open()
            fixture.server.enqueue(
                MockResponse().setResponseCode(status).setHeader("Retry-After", "0")
                    .setHeader("Location", "/another-endpoint").setBody("fixture-sensitive-error"),
            )
            fixture.server.enqueue(MockResponse().setBody(VALID_PAIR))
            val failure = failure { fixture.client.newCall("fixture-sensitive-refresh", DEVICE_ID).execute() }
            assertEquals(RefreshFailure.Http(status), failure.failure)
            assertEquals(1, fixture.server.requestCount)
            assertEquals(1, fixture.releasedConnections.get())
        }
    }

    @Test
    fun `invalid response never replaces any part of the current session`() = runTest {
        val fixture = open()
        val store = FakeSessionStore(TokenPair("saved-access", "saved-refresh"))
        SessionManager.open(store).use { manager ->
            val before = manager.snapshot()
            listOf(
                "",
                "null",
                "{}",
                """{"access":"fixture-sensitive"}""",
                """{"access":null,"refresh":"fixture-sensitive"}""",
                """{"access":42,"refresh":"fixture-sensitive"}""",
                """{"access":"fixture-sensitive","refresh":" "}""",
                """{"access":"","refresh":"fixture-sensitive"}""",
                """{"access":"fixture-sensitive\n","refresh":"fixture-sensitive"}""",
                """{"access":"fixture-sensitive","refresh":"unfinished""",
            ).forEach { body ->
                fixture.server.enqueue(MockResponse().setBody(body))
                val failure = failure {
                    fixture.client.newCall("fixture-sensitive-refresh", DEVICE_ID).execute()
                }
                assertEquals(RefreshFailure.InvalidResponse, failure.failure)
                assertEquals(before.accessToken, manager.snapshot().accessToken)
                assertEquals(before.sessionGeneration, manager.snapshot().sessionGeneration)
                assertEquals("saved-refresh", store.tokens!!.refresh)
                assertEquals(0, store.writes)
            }
            fixture.server.enqueue(MockResponse().setResponseCode(HTTP_NO_CONTENT))
            assertEquals(
                RefreshFailure.InvalidResponse,
                failure { fixture.client.newCall("fixture-sensitive-refresh", DEVICE_ID).execute() }.failure,
            )
        }
    }

    @Test
    fun `timeout is distinct and does not retry`() {
        val fixture = open(NetworkTimeouts(read = 250.milliseconds, call = 2.seconds))
        fixture.server.enqueue(MockResponse().setBody(VALID_PAIR).setBodyDelay(2, TimeUnit.SECONDS))
        assertEquals(
            RefreshFailure.Timeout,
            failure { fixture.client.newCall("fixture-sensitive-refresh", DEVICE_ID).execute() }.failure,
        )
        assertEquals(1, fixture.server.requestCount)
    }

    @Test
    fun `call deadline is a timeout rather than explicit cancellation`() {
        val fixture = open(NetworkTimeouts(read = 5.seconds, call = 250.milliseconds))
        fixture.server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        assertEquals(
            RefreshFailure.Timeout,
            failure { fixture.client.newCall("fixture-sensitive-refresh", DEVICE_ID).execute() }.failure,
        )
        assertEquals(1, fixture.server.requestCount)
    }

    @Test
    fun `disconnect after the request never replays refresh`() {
        val fixture = open()
        fixture.server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        fixture.server.enqueue(MockResponse().setBody(VALID_PAIR))
        assertEquals(
            RefreshFailure.Io,
            failure { fixture.client.newCall("fixture-sensitive-refresh", DEVICE_ID).execute() }.failure,
        )
        assertEquals(1, fixture.server.requestCount)
    }

    @Test
    fun `cancellation of an active HTTP call propagates without a response or retry`() {
        val fixture = open()
        fixture.server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val call = fixture.client.newCall("fixture-sensitive-refresh", DEVICE_ID)
        val executor = Executors.newSingleThreadExecutor()
        try {
            val pending = executor.submit<CancellationException?> {
                try {
                    call.execute()
                    null
                } catch (cancelled: CancellationException) {
                    cancelled
                }
            }
            assertEquals("/$REFRESH_PATH", fixture.server.takeRequest(2, TimeUnit.SECONDS)!!.path)
            call.cancel()
            assertTrue(pending.get(2, TimeUnit.SECONDS) is CancellationException)
            assertEquals(1, fixture.server.requestCount)
        } finally {
            call.cancel()
            executor.shutdownNow()
        }
    }

    @Test
    fun `unsafe endpoints and invalid request identity fail locally with safe diagnostics`() {
        val fixture = open()
        listOf(
            "https://fixture-sensitive.example/refresh",
            "//fixture-sensitive.example/refresh",
            "/api/tv/auth/refresh",
            "../refresh",
            "api/%2e%2e/refresh",
            "api/refresh?token=fixture-sensitive",
            "api/refresh#fixture-sensitive",
            "api\\refresh",
        ).forEach { endpoint ->
            val invalid = assertThrows(IllegalArgumentException::class.java) { RefreshEndpoint(endpoint) }
            assertFalse(invalid.toString().contains("fixture-sensitive"))
        }
        val badId = assertThrows(IllegalArgumentException::class.java) {
            fixture.client.newCall("fixture-sensitive-refresh", "fixture-sensitive-id")
        }
        assertFalse(badId.toString().contains("fixture-sensitive"))
        assertNull(badId.cause)
        assertThrows(IllegalArgumentException::class.java) { fixture.client.newCall(" ", DEVICE_ID) }
        assertEquals(0, fixture.server.requestCount)
        assertFalse(RefreshRequestDto("fixture-sensitive", DEVICE_ID).toString().contains("fixture-sensitive"))
        val response = RefreshResponseDto("fixture-sensitive", "fixture-sensitive")
        assertFalse(response.toString().contains("fixture-sensitive"))
    }

    private fun failure(action: () -> Unit): RefreshException {
        val failure = assertThrows(RefreshException::class.java, action)
        assertFalse(failure.toString().contains("fixture-sensitive"))
        assertNull(failure.cause)
        assertTrue(failure.suppressed.isEmpty())
        return failure
    }
}
