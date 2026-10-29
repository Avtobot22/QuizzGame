package com.quizzgame.core.session

import com.quizzgame.core.network.AuthPolicy
import com.quizzgame.core.network.NetworkTimeouts
import com.quizzgame.core.network.RefreshFailedException
import com.quizzgame.core.network.RefreshFailureReason
import com.quizzgame.core.network.ReplayPolicy
import com.quizzgame.core.network.RequestPolicy
import com.quizzgame.core.network.SessionRevokedException
import com.quizzgame.core.session.api.RefreshFailure
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SessionRefreshCoordinatorTest : SessionRecoveryTestFixture() {
    @Test
    fun `ten concurrent GETs share one refresh and persist both tokens before replay`() {
        val initial = CountDownLatch(10)
        val refreshes = AtomicInteger()
        lateinit var fixture: RecoveryFixture
        fixture = openRecovery(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path == RECOVERY_REFRESH_PATH -> {
                    refreshes.incrementAndGet()
                    assertNull(request.getHeader("Authorization"))
                    assertTrue(request.body.readUtf8().contains(RECOVERY_REFRESH_1))
                    pairResponse()
                }
                request.getHeader("Authorization") == "Bearer $RECOVERY_ACCESS_2" -> {
                    assertEquals(RECOVERY_REFRESH_2, fixture.store.tokens!!.refresh)
                    assertEquals(1, fixture.store.writes)
                    MockResponse().setBody("ok")
                }
                else -> {
                    initial.countDown()
                    await(initial)
                    MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                }
            }
        })
        val before = fixture.manager.snapshot()
        val calls = List(10) { executeAsync(newCall(fixture, "/private")) }
        calls.forEach { assertEquals(HTTP_OK, result(it).status) }
        assertEquals(1, refreshes.get())
        assertEquals(before.sessionGeneration, fixture.manager.snapshot().sessionGeneration)
        assertTrue(fixture.manager.snapshot().credentialsRevision > before.credentialsRevision)
    }

    @Test
    fun `403 NONE POST and NEVER never refresh or replay`() {
        val refreshes = AtomicInteger()
        val fixture = openRecovery(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.path == RECOVERY_REFRESH_PATH) refreshes.incrementAndGet()
                val code = if (request.path == "/forbidden") HTTP_FORBIDDEN else HTTP_UNAUTHORIZED
                return MockResponse().setResponseCode(code).setBody("original-error")
            }
        })
        listOf("/forbidden", "/none", "/post", "/never").forEach { path ->
            val auth = if (path == "/none") AuthPolicy.NONE else AuthPolicy.REQUIRED
            val replay = if (path == "/never") ReplayPolicy.NEVER else ReplayPolicy.AFTER_REFRESH_ONCE
            val request = Request.Builder().url(checkNotNull(fixture.api.retrofit.baseUrl().resolve(path)))
                .tag(RequestPolicy::class.java, RequestPolicy(auth, replay))
                .apply { if (path == "/post") post("fixture".toRequestBody()) }.build()
            fixture.api.httpClient.newCall(request).execute().use {
                assertEquals(if (path == "/forbidden") HTTP_FORBIDDEN else HTTP_UNAUTHORIZED, it.code)
                assertEquals("original-error", it.body!!.string())
            }
        }
        assertEquals(0, refreshes.get())
        assertEquals(4, fixture.server.requestCount)
        assertEquals(0, fixture.store.writes)
    }

    @Test
    fun `a second GET 401 stops with the rotated pair retained`() {
        val refreshes = AtomicInteger()
        val gets = AtomicInteger()
        val fixture = openRecovery(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = if (request.path == RECOVERY_REFRESH_PATH) {
                refreshes.incrementAndGet()
                pairResponse()
            } else {
                gets.incrementAndGet()
                MockResponse().setResponseCode(HTTP_UNAUTHORIZED).setBody("still-unauthorized")
            }
        })
        assertEquals(HTTP_UNAUTHORIZED, execute(newCall(fixture, "/private")).status)
        assertEquals(1, refreshes.get())
        assertEquals(2, gets.get())
        assertEquals(RECOVERY_REFRESH_2, fixture.store.tokens!!.refresh)
    }

    @Test
    fun `temporary HTTP invalid response timeout and disconnect preserve the old pair`() {
        val cases = listOf(
            MockResponse().setResponseCode(HTTP_INTERNAL_ERROR).setBody("fixture-sensitive") to
                RefreshFailureReason.Http(HTTP_INTERNAL_ERROR),
            MockResponse().setResponseCode(HTTP_UNAUTHORIZED) to RefreshFailureReason.Http(HTTP_UNAUTHORIZED),
            MockResponse().setBody("""{"access":"fixture-sensitive"}""") to RefreshFailureReason.InvalidResponse,
            MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE) to RefreshFailureReason.Timeout,
            MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST) to RefreshFailureReason.Io,
        )
        cases.forEach { (response, expected) ->
            val fixture = openRecovery(
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        if (request.path == RECOVERY_REFRESH_PATH) {
                            response
                        } else {
                            MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                        }
                },
                timeouts = NetworkTimeouts(read = 250.milliseconds, call = 3.seconds),
            )
            val failure = execute(newCall(fixture, "/private")).failure
            assertTrue(failure is RefreshFailedException)
            assertEquals(expected, (failure as RefreshFailedException).reason)
            assertNull(failure.cause)
            assertTrue(failure.suppressed.isEmpty())
            assertFalse(failure.toString().contains("fixture-sensitive"))
            assertEquals(RECOVERY_REFRESH_1, fixture.store.tokens!!.refresh)
            assertEquals(0, fixture.store.writes)
        }
    }

    @Test
    fun `only configured definitive revocation expires the current session and keeps its UUID`() {
        val fixture = openRecovery(
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
            },
            revocationPolicy = RefreshRevocationPolicy { it == RefreshFailure.Http(HTTP_UNAUTHORIZED) },
        )
        val id = fixture.manager.deviceIdProvider.get()
        assertTrue(execute(newCall(fixture, "/private")).failure is SessionRevokedException)
        assertEquals(SessionState.Expired, fixture.manager.state.value)
        assertNull(fixture.store.tokens)
        assertEquals(id, fixture.manager.deviceIdProvider.get())
    }

    @Test
    fun `logout another login and close during refresh prevent applying or replaying the old result`() {
        repeat(3) { action ->
            val entered = gate()
            val release = gate()
            val gets = AtomicInteger()
            val fixture = openRecovery(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    if (request.path == RECOVERY_REFRESH_PATH) {
                        entered.countDown()
                        await(release)
                        pairResponse()
                    } else {
                        gets.incrementAndGet()
                        MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
                    }
            })
            val pending = executeAsync(newCall(fixture, "/private"))
            await(entered)
            runBlocking {
                when (action) {
                    0 -> fixture.manager.logout()
                    1 -> fixture.manager.startSession(TokenPair("another-access", "another-refresh"))
                    else -> fixture.manager.close()
                }
            }
            release.countDown()
            assertNull(result(pending).status)
            assertEquals(1, gets.get())
            assertEquals(
                if (action == 0) null else if (action == 1) "another-refresh" else RECOVERY_REFRESH_1,
                fixture.store.tokens?.refresh,
            )
        }
    }

    @Test
    fun `independent session coordinators never share their refresh credentials`() {
        fun dispatcher(expected: String) = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path == RECOVERY_REFRESH_PATH -> {
                    assertTrue(request.body.readUtf8().contains(expected))
                    pairResponse()
                }
                request.getHeader("Authorization") == "Bearer $RECOVERY_ACCESS_2" -> MockResponse().setBody("ok")
                else -> MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
            }
        }
        val first = openRecovery(dispatcher(RECOVERY_REFRESH_1))
        val second = openRecovery(dispatcher("other-refresh"))
        runBlocking { second.manager.startSession(TokenPair("other-access", "other-refresh")) }
        val pending = listOf(first, second).map { executeAsync(newCall(it, "/private")) }
        pending.forEach { assertEquals(HTTP_OK, result(it).status) }
        assertEquals(RECOVERY_REFRESH_2, first.store.tokens!!.refresh)
        assertEquals(RECOVERY_REFRESH_2, second.store.tokens!!.refresh)
        assertEquals(3, first.server.requestCount)
        assertEquals(3, second.server.requestCount)
    }

    @Test
    fun `storage failure retains the old pair and surfaces a safe distinct failure`() {
        val fixture = openRecovery(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = if (request.path == RECOVERY_REFRESH_PATH) {
                pairResponse()
            } else {
                MockResponse().setResponseCode(HTTP_UNAUTHORIZED)
            }
        })
        fixture.store.failWrite = true
        val failure = execute(newCall(fixture, "/private")).failure as RefreshFailedException
        assertEquals(RefreshFailureReason.Storage, failure.reason)
        assertEquals(RECOVERY_ACCESS_1, fixture.manager.snapshot().accessToken)
        assertEquals(RECOVERY_REFRESH_1, fixture.store.tokens!!.refresh)
    }
}
