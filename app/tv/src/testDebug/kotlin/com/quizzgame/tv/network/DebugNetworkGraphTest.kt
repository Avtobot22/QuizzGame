package com.quizzgame.tv.network

import com.quizzgame.core.network.AuthPolicy
import com.quizzgame.core.network.CredentialsProvider
import com.quizzgame.core.network.CredentialsSnapshot
import com.quizzgame.core.network.NetworkTimeouts
import com.quizzgame.core.network.ReplayPolicy
import com.quizzgame.core.network.RequestPolicy
import com.quizzgame.debug.network.DebugMockFailure
import com.quizzgame.debug.network.DebugMockFailureInterceptor
import com.quizzgame.debug.network.DebugMockTransportException
import com.quizzgame.debug.network.DebugNetworkConfiguration
import com.quizzgame.debug.network.DebugNetworkGraph
import com.quizzgame.debug.network.DebugNetworkMode
import dev.androidmock.core.dsl.mockRules
import dev.androidmock.core.response.ResponseSpec
import dev.androidmock.okhttp.server.MockServerFailureProtocol
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Tag
import java.io.IOException
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val INVALID_HTTP_STATUS = 999
private const val TEST_WAIT_SECONDS = 5L
private const val SOCKET_TIMEOUT_MILLIS = 1000

class DebugNetworkGraphTest : DebugNetworkTestFixture() {

    @Test
    fun bothRetrofitClientsUseTheLiveLoopbackServerAndPackagedLaunchFixture() {
        val graph = openMock()
        val api = graph.api.retrofit.create(ProbeApi::class.java)
        val refresh = graph.refresh.retrofit.create(ProbeApi::class.java)
        assertEquals("127.0.0.1", graph.api.retrofit.baseUrl().host)
        assertEquals("http", graph.api.retrofit.baseUrl().scheme)
        assertTrue(graph.api.retrofit.baseUrl().port > 0)
        assertEquals(graph.api.retrofit.baseUrl(), graph.refresh.retrofit.baseUrl())
        assertNotSame(graph.api.httpClient.connectionPool, graph.refresh.httpClient.connectionPool)
        assertEquals(
            "00000000-0000-4000-8000-000000000001",
            api.launch().execute().body()!!["id"]!!.jsonPrimitive.content,
        )
        assertEquals(api.launch().execute().body(), refresh.launch().execute().body())
    }

    @Test
    fun nativeAuthenticatorRefreshesThroughTheSameServerAndFollowsUpOnce() {
        val calls = CopyOnWriteArrayList<String>()
        val authentications = AtomicInteger()
        val graph = openMock(
            credentialsProvider = CredentialsProvider { CredentialsSnapshot("fixture-access-1", 1) },
            rules = mockRules {
                rule("private", "/test/private") {
                    match { method("GET") }
                    respondWith { request, _ ->
                        val authorization = request.headers.values("Authorization").singleOrNull()
                        calls += "GET $authorization"
                        assertEquals(listOf("test-tv"), request.headers.values("X-Client-Platform"))
                        if (authorization == "Bearer fixture-access-2") {
                            jsonResponse("""{"ok":true}""")
                        } else {
                            ResponseSpec(status = HTTP_UNAUTHORIZED)
                        }
                    }
                }
                rule("refresh", "/test/refresh") {
                    match {
                        method("POST")
                        bodyExactUtf8("""{"fixture":"refresh"}""")
                    }
                    respondWith { request, _ ->
                        assertTrue(request.headers.values("Authorization").isEmpty())
                        calls += "POST refresh"
                        jsonResponse("""{"access":"fixture-access-2"}""")
                    }
                }
            },
        )
        val refresh = graph.refresh.retrofit.create(ProbeApi::class.java)
        val policy = RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE)
        val client = graph.api.httpClient.newBuilder().authenticator { _, response ->
            if (response.priorResponse != null || response.request.tag(RequestPolicy::class.java) != policy) {
                null
            } else {
                authentications.incrementAndGet()
                val payload = buildJsonObject { put("fixture", "refresh") }
                val result = refresh.refresh(payload, "must-be-removed").execute()
                val access = result.body()!!["access"]!!.jsonPrimitive.content
                response.request.newBuilder().header("Authorization", "Bearer $access").build()
            }
        }.build()
        val api = graph.api.retrofit.newBuilder().client(client).build().create(ProbeApi::class.java)
        val response = api.privateResource("stale-supplied-header", policy).execute()
        assertTrue(response.isSuccessful)
        assertEquals(HTTP_UNAUTHORIZED, response.raw().priorResponse!!.code)
        assertEquals(1, authentications.get())
        assertEquals(listOf("GET Bearer fixture-access-1", "POST refresh", "GET Bearer fixture-access-2"), calls)
    }

    @Test
    fun repeatedHTTP401StopsWhenTheTestAuthenticatorRejectsAnotherFollowUp() {
        val requests = AtomicInteger()
        val graph = openMock(
            credentialsProvider = CredentialsProvider { CredentialsSnapshot("initial-fixture", 1) },
            rules = mockRules {
                rule("reject", "/test/private") {
                    respondWith { _, _ ->
                        requests.incrementAndGet()
                        ResponseSpec(status = HTTP_UNAUTHORIZED)
                    }
                }
            },
        )
        val policy = RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE)
        val client = graph.api.httpClient.newBuilder().authenticator { _, response ->
            if (response.priorResponse == null && response.request.tag(RequestPolicy::class.java) == policy) {
                response.request.newBuilder().header("Authorization", "fixture").build()
            } else {
                null
            }
        }.build()
        val api = graph.api.retrofit.newBuilder().client(client).build().create(ProbeApi::class.java)
        val response = api.privateResource("stale-supplied-header", policy).execute()
        assertEquals(HTTP_UNAUTHORIZED, response.code())
        response.errorBody()?.close()
        assertEquals(2, requests.get())
    }

    @Test
    fun missingRuleBecomesTypedMockConfigurationFailureWithoutRemoteFallback() {
        val graph = openMock()
        val api = graph.api.retrofit.create(ProbeApi::class.java)
        val failure = assertThrows(DebugMockTransportException::class.java) {
            api.privateResource("fixture").execute()
        }
        assertEquals(DebugMockFailure.NO_MATCHING_RULE, failure.failure)
    }

    @Test
    fun unmarkedHTTP599AndMarkedHTTP500RemainOrdinaryHTTPResponses() {
        listOf(MockServerFailureProtocol.STATUS_CODE, HTTP_INTERNAL_ERROR).forEach { status ->
            val graph = openMock(
                rules = mockRules {
                    rule("backend", "/api/tv/launch") {
                        respond {
                            status(status)
                            if (status == HTTP_INTERNAL_ERROR) {
                                header(MockServerFailureProtocol.HEADER_NAME, "NoMatchingRule")
                            }
                            bodyText("backend failure")
                        }
                    }
                },
            )
            val response = graph.api.retrofit.create(ProbeApi::class.java).launch().execute()
            assertEquals(status, response.code())
            assertEquals("backend failure", response.errorBody()!!.use { it.string() })
        }
    }

    @Test
    fun callbackAndInvalidResponseFailuresRetainTheirSDKDiagnosticTypes() {
        val graph = openMock(
            rules = mockRules {
                rule("callback", "/api/tv/launch") {
                    respondWith { _, _ -> error("fixture callback failure") }
                }
                rule("invalid", "/test/private") {
                    respondWith { _, _ -> ResponseSpec(status = INVALID_HTTP_STATUS) }
                }
            },
        )
        val api = graph.api.retrofit.create(ProbeApi::class.java)
        assertEquals(
            DebugMockFailure.CALLBACK_FAILURE,
            assertThrows(DebugMockTransportException::class.java) { api.launch().execute() }.failure,
        )
        assertEquals(
            DebugMockFailure.INVALID_RESPONSE,
            assertThrows(DebugMockTransportException::class.java) { api.privateResource("fixture").execute() }.failure,
        )
    }

    @Test
    fun sdkResponseDelayObeysCallTimeoutWithoutRetry() {
        val requests = AtomicInteger()
        val graph = openMock(
            timeouts = NetworkTimeouts(read = 5.seconds, call = 500.milliseconds),
            rules = mockRules {
                rule("slow", "/api/tv/launch") {
                    respondWith { _, _ ->
                        requests.incrementAndGet()
                        jsonResponse("""{"id":"fixture"}""", delay = 5.seconds)
                    }
                }
            },
        )
        assertThrows(IOException::class.java) { graph.api.retrofit.create(ProbeApi::class.java).launch().execute() }
        assertEquals(1, requests.get())
    }

    @Test
    fun closingGraphCancelsActiveCallShutsServerAndRejectsReuse() {
        val entered = CountDownLatch(1)
        val graph = openMock(
            rules = mockRules {
                rule("slow", "/api/tv/launch") {
                    respondWith { _, _ ->
                        entered.countDown()
                        jsonResponse("""{"id":"fixture"}""", delay = 30.seconds)
                    }
                }
            },
        )
        val executor = Executors.newSingleThreadExecutor()
        try {
            val api = graph.api.retrofit.create(ProbeApi::class.java)
            val result = executor.submit<IOException?> {
                try {
                    api.launch().execute()
                    null
                } catch (failure: IOException) {
                    failure
                }
            }
            assertTrue(entered.await(TEST_WAIT_SECONDS, TimeUnit.SECONDS))
            graph.close()
            graph.close()
            assertTrue(result.get(TEST_WAIT_SECONDS, TimeUnit.SECONDS) is IOException)
            assertThrows(IOException::class.java) { api.launch().execute() }
            assertTrue(graph.api.httpClient.dispatcher.executorService.isShutdown)
            assertTrue(graph.refresh.httpClient.dispatcher.executorService.isShutdown)
            assertEquals(0, graph.api.httpClient.connectionPool.connectionCount())
            assertThrows(IOException::class.java) {
                Socket().use {
                    it.connect(InetSocketAddress("127.0.0.1", graph.api.retrofit.baseUrl().port), SOCKET_TIMEOUT_MILLIS)
                }
            }
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun switchingModesClosesAndRebuildsBothClientsWithoutLoadingMockAssetsInREAL() {
        val first = openMock()
        first.close()
        val real = openTvDebugNetwork(
            DebugNetworkConfiguration(DebugNetworkMode.REAL, headers, "https://example.invalid/"),
            bodyAssets = { error("REAL mode must not read fixtures") },
        ).also(graphs::add)
        assertEquals("https://example.invalid/", real.api.retrofit.baseUrl().toString())
        assertEquals(real.api.retrofit.baseUrl(), real.refresh.retrofit.baseUrl())
        assertNotSame(first.api.httpClient, real.api.httpClient)
        assertNotSame(first.refresh.httpClient, real.refresh.httpClient)
        assertFalse(real.api.httpClient.interceptors.any { it is DebugMockFailureInterceptor })
        real.close()
        val second = openMock()
        assertNotSame(real.api.httpClient, second.api.httpClient)
        assertNotSame(real.refresh.httpClient, second.refresh.httpClient)
        assertTrue(second.api.retrofit.create(ProbeApi::class.java).launch().execute().isSuccessful)
    }

    @Test
    fun realRequiresAnExternalHTTPSURLAndMOCKRejectsAFallbackURL() {
        assertThrows(IllegalArgumentException::class.java) { DebugNetworkConfiguration(DebugNetworkMode.REAL, headers) }
        assertThrows(IllegalArgumentException::class.java) {
            DebugNetworkGraph.open(DebugNetworkConfiguration(DebugNetworkMode.REAL, headers, "http://127.0.0.1/"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            DebugNetworkConfiguration(DebugNetworkMode.MOCK, headers, "https://example.invalid/")
        }
    }

    interface ProbeApi {

        @GET("api/tv/launch")
        fun launch(): Call<JsonObject>

        @GET("test/private")
        fun privateResource(
            @Header("Authorization") authorization: String,
            @Tag policy: RequestPolicy? = null,
        ): Call<JsonObject>

        @POST("test/refresh")
        fun refresh(@Body payload: JsonObject, @Header("Authorization") authorization: String): Call<JsonObject>
    }
}
