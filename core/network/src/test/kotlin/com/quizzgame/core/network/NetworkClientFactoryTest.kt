package com.quizzgame.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Invocation
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Tag
import retrofit2.http.Url
import java.io.IOException
import java.net.HttpURLConnection.HTTP_UNAVAILABLE
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class NetworkClientFactoryTest {
    private val server = MockWebServer()
    private val clients = mutableListOf<NetworkClient>()

    @Before
    fun startServer() {
        server.start(InetAddress.getByName("127.0.0.1"), 0)
    }

    @After
    fun closeResources() {
        clients.forEach {
            it.httpClient.dispatcher.executorService.shutdownNow()
            it.httpClient.connectionPool.evictAll()
        }
        server.shutdown()
    }

    @Test
    fun `GET uses supplied base path headers and no body`() {
        server.enqueue(jsonResponse("""{"value":"ready","extra":42,"nullable":null}"""))
        val marker = Marker("test")
        val response = client().retrofit.create(TestApi::class.java).get(marker).execute()
        assertEquals(Payload("ready", null), response.body())
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/prefix/probe", request.path)
        assertEquals("GET", request.method)
        assertEquals(0L, request.bodySize)
        assertEquals("application/json", request.getHeader("Accept"))
        assertEquals("1", request.getHeader("X-Endpoint-Version"))
        assertEquals("test-platform", request.getHeader("X-Client-Platform"))
        assertEquals("test-host/1", request.getHeader("User-Agent"))
        assertNull(request.getHeader("Authorization"))
        assertEquals(marker, response.raw().request.tag(Marker::class.java))
        assertEquals("get", response.raw().request.tag(Invocation::class.java)!!.method().name)
    }

    @Test
    fun `missing required field and null nonnullable field fail decoding`() {
        val api = client().retrofit.create(TestApi::class.java)
        listOf("""{"nullable":null}""", """{"value":null,"nullable":null}""").forEach {
            server.enqueue(jsonResponse(it))
            assertThrows(SerializationException::class.java) { api.get().execute() }
        }
    }

    @Test
    fun `explicit null is accepted but missing required nullable field is rejected`() {
        val api = client().retrofit.create(TestApi::class.java)
        server.enqueue(jsonResponse("""{"value":"ready","nullable":null}"""))
        assertEquals(Payload("ready", null), api.get().execute().body())
        server.enqueue(jsonResponse("""{"value":"ready"}"""))
        assertThrows(SerializationException::class.java) { api.get().execute() }
    }

    @Test
    fun `refresh client is isolated and removes supplied authorization`() {
        val factory = factory()
        val api = factory.createApiClient().also(clients::add)
        val refresh = factory.createRefreshClient().also(clients::add)
        assertNotSame(api.httpClient.connectionPool, refresh.httpClient.connectionPool)
        assertNotSame(api.httpClient.dispatcher, refresh.httpClient.dispatcher)
        server.enqueue(MockResponse().setResponseCode(401))
        val response = refresh.retrofit.create(TestApi::class.java).post(Payload("test", null), "test").execute()
        assertEquals(401, response.code())
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertNull(request.getHeader("Authorization"))
        assertEquals("application/json", request.getHeader("Accept"))
        assertEquals("application/json; charset=utf-8", request.getHeader("Content-Type"))
        assertEquals("test-platform", request.getHeader("X-Client-Platform"))
        assertEquals("1", request.getHeader("X-Endpoint-Version"))
        assertEquals("test-host/1", request.getHeader("User-Agent"))
        assertEquals("""{"value":"test","nullable":null}""", request.body.readUtf8())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `503 retry instruction is returned unchanged without repeating GET or POST`() {
        val api = client().retrofit.create(TestApi::class.java)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                MockResponse().setResponseCode(HTTP_UNAVAILABLE).setHeader("Retry-After", "0").setBody("busy")
        }
        listOf(api.get(), api.post(Payload("test", null))).forEachIndexed { index, call ->
            val response = call.execute()
            assertEquals(HTTP_UNAVAILABLE, response.code())
            assertEquals("0", response.headers()["Retry-After"])
            assertEquals("busy", response.errorBody()!!.use { it.string() })
            assertEquals(index + 1, server.requestCount)
        }
    }

    @Test
    fun `redirect and HTTP408 never repeat POST`() {
        val api = client().retrofit.create(TestApi::class.java)
        listOf(302, 307, 308, 408).forEachIndexed { index, status ->
            server.enqueue(MockResponse().setResponseCode(status).setHeader("Location", server.url("/redirect")))
            val response = api.post(Payload("test", null)).execute()
            assertEquals(status, response.code())
            response.errorBody()?.close()
            assertEquals(index + 1, server.requestCount)
        }
    }

    @Test
    fun `disconnect after POST request never retries the body`() {
        val api = client().retrofit.create(TestApi::class.java)
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        assertThrows(IOException::class.java) { api.post(Payload("test", null)).execute() }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `whole call timeout is enforced without retry`() {
        val api = client(NetworkTimeouts(read = 5.seconds, call = 500.milliseconds))
            .retrofit.create(TestApi::class.java)
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        assertThrows(IOException::class.java) { api.get().execute() }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `absolute URL cannot escape configured origin`() {
        val api = client().retrofit.create(TestApi::class.java)
        assertThrows(IOException::class.java) { api.at("http://example.com/probe").execute() }
        assertThrows(IOException::class.java) { api.at("https://example.com/probe").execute() }
        assertThrows(IOException::class.java) { api.at("http://127.0.0.1:1/probe").execute() }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `timeouts and automatic transport policies are configured for both clients`() {
        val factory = factory(NetworkTimeouts(1.seconds, 2.seconds, 3.seconds, 4.seconds))
        listOf(factory.createApiClient(), factory.createRefreshClient()).forEach {
            clients += it
            assertEquals(1000, it.httpClient.connectTimeoutMillis)
            assertEquals(2000, it.httpClient.readTimeoutMillis)
            assertEquals(3000, it.httpClient.writeTimeoutMillis)
            assertEquals(4000, it.httpClient.callTimeoutMillis)
            assertFalse(it.httpClient.retryOnConnectionFailure)
            assertFalse(it.httpClient.followRedirects)
            assertFalse(it.httpClient.followSslRedirects)
        }
    }

    private fun client(timeouts: NetworkTimeouts = NetworkTimeouts()): NetworkClient =
        factory(timeouts).createApiClient().also(clients::add)

    private fun factory(timeouts: NetworkTimeouts = NetworkTimeouts()): NetworkClientFactory =
        NetworkClientFactory(
            NetworkConfiguration(
                baseUrl = ApiBaseUrl.debugLoopback(
                    server.url("/prefix/").newBuilder().host("127.0.0.1").build().toString(),
                ),
                headers = PlatformHeaders("test-platform", "test-host/1", 1),
                timeouts = timeouts,
            ),
        )

    private fun jsonResponse(body: String): MockResponse =
        MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    interface TestApi {
        @GET("probe")
        fun get(@Tag marker: Marker? = null): Call<Payload>

        @POST("probe")
        fun post(@Body body: Payload, @Header("Authorization") authorization: String? = null): Call<Payload>

        @GET
        fun at(@Url url: String): Call<Payload>
    }

    @Serializable
    data class Payload(val value: String, val nullable: String?)

    data class Marker(val value: String)
}
