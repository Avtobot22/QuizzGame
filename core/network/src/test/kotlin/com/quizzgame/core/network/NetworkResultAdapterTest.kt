package com.quizzgame.core.network

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import okhttp3.EventListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Tag
import java.net.ConnectException
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import okhttp3.Call as OkHttpCall

class NetworkResultAdapterTest {
    private val server = MockWebServer()
    private val clients = mutableListOf<NetworkClient>()
    private lateinit var client: NetworkClient
    private lateinit var api: Api

    @Before
    fun setUp() {
        server.start()
        client = createClient()
        api = client.retrofit.create(Api::class.java)
    }

    @After
    fun tearDown() {
        clients.forEach {
            it.httpClient.dispatcher.cancelAll()
            it.httpClient.connectionPool.evictAll()
            it.httpClient.dispatcher.executorService.shutdown()
        }
        server.shutdown()
    }

    @Test
    fun `execute and both suspend styles produce the same success and HTTP details`() = runBlocking {
        repeat(3) { server.enqueue(MockResponse().setBody("""{"value":"sensitive"}""")) }
        assertEquals(NetworkResult.Success(Payload("sensitive")), client.results.execute(api.sync()))
        assertEquals(NetworkResult.Success(Payload("sensitive")), client.results.call { api.body() })
        assertEquals(NetworkResult.Success(Payload("sensitive")), client.results.response { api.response() })
        repeat(3) {
            server.enqueue(MockResponse().setResponseCode(500).setBody(BACKEND_ERROR))
        }
        val expected = NetworkResult.Failure(NetworkFailure.Http(500, BackendErrorDetails(7, "secret", "private")))
        assertEquals(expected, client.results.execute(api.sync()))
        assertEquals(expected, client.results.call { api.body() })
        assertEquals(expected, client.results.response { api.response() })
        assertFalse(expected.toString().contains("secret"))
        assertFalse(expected.toString().contains("private"))
        assertFalse(NetworkResult.Success("sensitive").toString().contains("sensitive"))
    }

    @Test
    fun `malformed empty or incomplete backend JSON preserves HTTP status`() {
        listOf("{", "", "{}", """{"reasonCode":"wrong","debugMessage":"x"}""").forEach { body ->
            server.enqueue(MockResponse().setResponseCode(403).setBody(body))
            assertEquals(NetworkResult.Failure(NetworkFailure.Http(403)), client.results.execute(api.sync()))
        }
        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))
        assertEquals(NetworkResult.Failure(NetworkFailure.Http(401)), client.results.execute(api.sync()))
    }

    @Test
    fun `malformed successful JSON and missing mandatory fields are invalid responses`() = runBlocking {
        listOf("{", "{}", """{"value":null}""").forEach { body ->
            server.enqueue(MockResponse().setBody(body))
            assertEquals(NetworkResult.Failure(NetworkFailure.InvalidResponse), client.results.execute(api.sync()))
            server.enqueue(MockResponse().setBody(body))
            assertEquals(NetworkResult.Failure(NetworkFailure.InvalidResponse), client.results.call { api.body() })
        }
        listOf(204, 205).forEach { status ->
            server.enqueue(MockResponse().setResponseCode(status))
            assertEquals(NetworkResult.Failure(NetworkFailure.InvalidResponse), client.results.execute(api.sync()))
            server.enqueue(MockResponse().setResponseCode(status))
            assertEquals(NetworkResult.Failure(NetworkFailure.InvalidResponse), client.results.call { api.body() })
            server.enqueue(MockResponse().setResponseCode(status))
            assertEquals(NetworkResult.Success(Unit), client.results.call { api.unit() })
        }
    }

    @Test
    fun `required authentication is distinct from an HTTP unauthorized response`() {
        val policy = RequestPolicy(auth = AuthPolicy.REQUIRED)
        assertEquals(
            NetworkResult.Failure(NetworkFailure.AuthenticationRequired),
            client.results.execute(api.sync(policy)),
        )
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `read and whole call timeouts remain timeouts`() {
        listOf(
            NetworkTimeouts(read = 100.milliseconds, call = 2.seconds),
            NetworkTimeouts(read = 2.seconds, call = 100.milliseconds),
        ).forEach { timeouts ->
            val network = createClient(timeouts)
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            assertEquals(
                NetworkResult.Failure(NetworkFailure.Timeout),
                network.results.execute(network.retrofit.create(Api::class.java).sync()),
            )
        }
    }

    @Test
    fun `connection and technical boundaries retain their exact cause type`() = runTest {
        assertEquals(NetworkResult.Failure(NetworkFailure.Connection), client.results.call { throw ConnectException() })
        assertEquals(
            NetworkResult.Failure(NetworkFailure.SessionRevoked),
            client.results.call { throw SessionRevokedException() },
        )
    }

    @Test
    fun `unknown exceptions and cancellation are never mapped to connection`() = runBlocking {
        val unexpected = IllegalArgumentException("programming failure")
        assertSame(
            unexpected,
            assertThrows(IllegalArgumentException::class.java) {
                runBlocking { client.results.call<Payload> { throw unexpected } }
            },
        )
        val cancelled = CancellationException("cancelled")
        assertSame(
            cancelled,
            assertThrows(CancellationException::class.java) {
                runBlocking { client.results.call<Payload> { throw cancelled } }
            },
        )
        val call = api.sync()
        call.cancel()
        assertThrows(CancellationException::class.java) { client.results.execute(call) }
        val entered = CompletableDeferred<Unit>()
        var completed = false
        val pending = async {
            client.results.call<Payload> {
                entered.complete(Unit)
                CompletableDeferred<Payload>().await()
            }
            completed = true
        }
        entered.await()
        pending.cancelAndJoin()
        assertFalse(completed)
    }

    @Test
    fun `application null pointer errors remain exceptions`() {
        listOf(NullPointerException("programming failure"), KotlinNullPointerException("programming failure"))
            .forEach { failure ->
                assertSame(
                    failure,
                    assertThrows(NullPointerException::class.java) {
                        runBlocking { client.results.call<Payload> { throw failure } }
                    },
                )
            }
    }

    @Test
    fun `cancelling an active Retrofit suspend call does not produce a failure result`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        var completed = false
        val pending = async {
            client.results.call { api.body() }
            completed = true
        }
        kotlinx.coroutines.yield()
        assertTrue(server.takeRequest(5, TimeUnit.SECONDS) != null)
        pending.cancelAndJoin()
        assertTrue(pending.isCancelled)
        assertFalse(completed)
    }

    @Test
    fun `explicit cancellation during a blocking Retrofit body read stays cancellation`() {
        val reading = CountDownLatch(1)
        val transport = client.httpClient.newBuilder().eventListener(object : EventListener() {
            override fun responseBodyStart(call: OkHttpCall) { reading.countDown() }
        }).build()
        val blockingApi = client.retrofit.newBuilder().client(transport).build().create(Api::class.java)
        server.enqueue(MockResponse().setBody("""{"value":"late"}""").throttleBody(1, 100, TimeUnit.MILLISECONDS))
        val call = blockingApi.sync()
        val executor = Executors.newSingleThreadExecutor()
        try {
            val pending = executor.submit<Boolean> {
                try {
                    client.results.execute(call)
                    false
                } catch (_: CancellationException) {
                    true
                }
            }
            assertTrue(reading.await(5, TimeUnit.SECONDS))
            call.cancel()
            assertTrue(pending.get(5, TimeUnit.SECONDS))
        } finally {
            call.cancel()
            executor.shutdownNow()
        }
    }

    private fun createClient(timeouts: NetworkTimeouts = NetworkTimeouts()): NetworkClient = NetworkClientFactory(
        NetworkConfiguration(
            ApiBaseUrl.debugLoopback(server.url("/").newBuilder().host("127.0.0.1").build().toString()),
            PlatformHeaders("test", "network-result-tests/1", 1),
            timeouts,
        ),
        createNetworkJson(),
    ).createApiClient().also(clients::add)

    interface Api {
        @GET("probe")
        fun sync(@Tag policy: RequestPolicy? = null): Call<Payload>

        @GET("probe")
        suspend fun body(): Payload

        @GET("probe")
        suspend fun response(): Response<Payload>

        @GET("probe")
        suspend fun unit(): Unit
    }

    @Serializable
    data class Payload(val value: String)

    companion object {
        private const val BACKEND_ERROR =
            """{"reasonCode":7,"debugMessage":"secret","userFriendlyMessage":"private","extra":true}"""
    }
}
