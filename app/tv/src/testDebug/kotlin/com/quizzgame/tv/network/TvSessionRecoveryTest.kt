package com.quizzgame.tv.network

import com.quizzgame.core.network.AuthPolicy
import com.quizzgame.core.network.MockFailureReason
import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.network.NetworkResult
import com.quizzgame.core.network.RefreshFailureReason
import com.quizzgame.core.network.ReplayPolicy
import com.quizzgame.core.network.RequestPolicy
import com.quizzgame.core.network.createNetworkJson
import com.quizzgame.core.session.SessionManager
import com.quizzgame.core.session.SessionRecord
import com.quizzgame.core.session.SessionStore
import com.quizzgame.core.session.TokenPair
import com.quizzgame.core.session.api.RefreshEndpoint
import com.quizzgame.debug.network.DebugNetworkConfiguration
import com.quizzgame.debug.network.DebugNetworkGraph
import com.quizzgame.debug.network.DebugNetworkMode
import com.quizzgame.debug.network.mocks.DebugMockAssets
import dev.androidmock.core.dsl.mockRules
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Tag
import java.io.IOException
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

private const val RECOVERY_WAIT_SECONDS = 5L

class TvSessionRecoveryTest : DebugNetworkTestFixture() {
    @Test
    fun missingSDKRulesRemainDiagnosticsOnAPIAndDuringRefresh() = runBlocking {
        val diagnostic = NetworkFailure.MockConfiguration(MockFailureReason.NO_MATCHING_RULE)
        val graph = openMock()
        val api = graph.api.retrofit.create(ResultApi::class.java)
        assertEquals(NetworkResult.Failure(diagnostic), graph.api.results.call { api.get(null) })
        SessionManager.open(TvRecoveryStore()).use { manager ->
            val recovering = DebugNetworkGraph.openWithSession(
                DebugNetworkConfiguration(DebugNetworkMode.MOCK, headers),
                createNetworkJson(),
                manager,
                RefreshEndpoint("api/tv/auth/refresh"),
                mockRules {
                    rule("private", "/private") {
                        respond { status(HTTP_UNAUTHORIZED) }
                    }
                },
            ).also(graphs::add)
            val protectedApi = recovering.api.retrofit.create(ResultApi::class.java)
            val result = recovering.api.results.call {
                protectedApi.get(RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE))
            }
            assertEquals(
                NetworkResult.Failure(NetworkFailure.RefreshFailed(RefreshFailureReason.Diagnostic(diagnostic))),
                result,
            )
            assertEquals("fixture-tv-access-1", manager.snapshot().accessToken)
        }
    }

    interface ResultApi {
        @GET("private")
        suspend fun get(@Tag policy: RequestPolicy?): JsonObject
    }

    @Test
    fun tvOptInGraphRecoversThroughPackagedSDKRulesAndBorrowsTheSession() = runBlocking {
        val store = TvRecoveryStore()
        SessionManager.open(store).use { manager ->
            val graph = openTvDebugNetwork(
                DebugNetworkConfiguration(DebugNetworkMode.MOCK, headers),
                manager,
                DebugMockAssets { path -> checkNotNull(javaClass.classLoader!!.getResourceAsStream(path)) },
            ).also(graphs::add)
            val request = Request.Builder().url(checkNotNull(graph.api.retrofit.baseUrl().resolve("debug/tv/session")))
                .tag(RequestPolicy::class.java, RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE))
                .build()
            graph.api.httpClient.newCall(request).execute().use { assertEquals(HTTP_OK, it.code) }
            assertEquals("fixture-tv-access-2", manager.snapshot().accessToken)
            assertEquals("fixture-tv-refresh-2", store.record.tokens!!.refresh)
            assertEquals(store.record.deviceId, manager.deviceIdProvider.get())
            graph.close()
            assertEquals("fixture-tv-access-2", manager.snapshot().accessToken)
        }
    }

    @Test
    fun closingSDKGraphCancelsPendingRecoveryWithoutClosingTheBorrowedSession() = runBlocking {
        val entered = CountDownLatch(1)
        val done = CountDownLatch(1)
        val gets = AtomicInteger()
        val refreshes = AtomicInteger()
        val store = TvRecoveryStore()
        SessionManager.open(store).use { manager ->
            val graph = DebugNetworkGraph.openWithSession(
                DebugNetworkConfiguration(DebugNetworkMode.MOCK, headers),
                createNetworkJson(),
                manager,
                RefreshEndpoint("api/tv/auth/refresh"),
                mockRules {
                    rule("private", "/private") {
                        respondWith { _, _ ->
                            gets.incrementAndGet()
                            dev.androidmock.core.response.ResponseSpec(status = HTTP_UNAUTHORIZED)
                        }
                    }
                    rule("refresh", "/api/tv/auth/refresh") {
                        respondWith { _, _ ->
                            refreshes.incrementAndGet()
                            entered.countDown()
                            jsonResponse("""{"access":"unused-access","refresh":"unused-refresh"}""", 30.seconds)
                        }
                    }
                },
            ).also(graphs::add)
            val request = Request.Builder().url(checkNotNull(graph.api.retrofit.baseUrl().resolve("private")))
                .tag(RequestPolicy::class.java, RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE))
                .build()
            graph.api.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) { done.countDown() }
                override fun onResponse(call: Call, response: Response) { response.close() }
            })
            assertTrue(entered.await(RECOVERY_WAIT_SECONDS, TimeUnit.SECONDS))
            graph.close()
            assertTrue(done.await(RECOVERY_WAIT_SECONDS, TimeUnit.SECONDS))
            assertEquals(1, gets.get())
            assertEquals(1, refreshes.get())
            assertEquals("fixture-tv-access-1", manager.snapshot().accessToken)
            assertEquals("fixture-tv-refresh-1", store.record.tokens!!.refresh)
        }
    }
}

private class TvRecoveryStore : SessionStore {
    var record = SessionRecord(
        "e4a508be-bb66-46d3-91f6-7b4cabfc977b",
        TokenPair("fixture-tv-access-1", "fixture-tv-refresh-1"),
    )
    override fun read(): SessionRecord = record
    override fun write(tokens: TokenPair?): SessionRecord = SessionRecord(record.deviceId, tokens).also { record = it }
}
