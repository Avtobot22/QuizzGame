package com.quizzgame.core.network

import okhttp3.Authenticator
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Tag
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class RequestPolicyHttpTest {
    private val server = MockWebServer()
    private val clients = mutableListOf<NetworkClient>()

    @Before
    fun startServer() {
        server.start(InetAddress.getByName("127.0.0.1"), 0)
    }

    @After
    fun closeResources() {
        clients.forEach {
            it.httpClient.dispatcher.cancelAll()
            it.httpClient.connectionPool.evictAll()
            it.httpClient.dispatcher.executorService.shutdownNow()
        }
        server.shutdown()
    }

    @Test
    fun `six auth cases enforce bearer policy and read one snapshot only when needed`() {
        val cases = AuthPolicy.entries.flatMap { auth -> listOf(false, true).map { auth to it } }
        cases.forEach { (auth, available) ->
            val reads = AtomicInteger()
            val snapshot = CredentialsSnapshot(if (available) "fixture-access" else null, 42)
            val provider = CredentialsProvider {
                reads.incrementAndGet()
                snapshot
            }
            val api = create(provider).retrofit.create(PolicyApi::class.java)
            val policy = RequestPolicy(auth, ReplayPolicy.AFTER_REFRESH_ONCE)
            val previousRequests = server.requestCount
            if (auth == AuthPolicy.REQUIRED && !available) {
                val failure = assertThrows(AuthenticationRequiredException::class.java) {
                    api.get(policy, "stale-supplied-authorization").execute()
                }
                assertEquals("Authentication is required", failure.message)
                assertEquals(previousRequests, server.requestCount)
            } else {
                server.enqueue(MockResponse().setBody("ok"))
                val response = api.get(policy, "stale-supplied-authorization").execute()
                response.body()?.close()
                val recorded = server.takeRequest(1, TimeUnit.SECONDS)!!
                val expectedAccess = if (auth != AuthPolicy.NONE && available) "Bearer fixture-access" else null
                assertEquals(expectedAccess, recorded.getHeader("Authorization"))
                val effective = response.raw().request.tag(RequestPolicy::class.java)!!
                assertEquals(auth, effective.auth)
                val context = response.raw().request.tag(RequestAuthContext::class.java)
                if (auth == AuthPolicy.NONE) {
                    assertEquals(ReplayPolicy.NEVER, effective.replay)
                    assertNull(context)
                } else {
                    assertEquals(ReplayPolicy.AFTER_REFRESH_ONCE, effective.replay)
                    assertEquals(42L, context!!.observedCredentials.sessionGeneration)
                    assertEquals(snapshot.accessToken, context.observedCredentials.accessToken)
                }
                assertEquals(previousRequests + 1, server.requestCount)
            }
            assertEquals(if (auth == AuthPolicy.NONE) 0 else 1, reads.get())
        }
    }

    @Test
    fun `untagged requests remove stale authorization and foreign auth context without reading credentials`() {
        val api = create(CredentialsProvider { error("Credentials must not be read") })
            .retrofit.create(PolicyApi::class.java)
        val staleContext = RequestAuthContext(CredentialsSnapshot("stale-context-access", 2))
        server.enqueue(MockResponse().setBody("ok"))
        val response = api.get(authorization = "stale-header", context = staleContext).execute()
        response.body()?.close()
        assertNull(server.takeRequest(1, TimeUnit.SECONDS)!!.getHeader("Authorization"))
        assertEquals(RequestPolicy(), response.raw().request.tag(RequestPolicy::class.java))
        assertNull(response.raw().request.tag(RequestAuthContext::class.java))
    }

    @Test
    fun `refresh forces anonymous policies and context without installing access interceptor or authenticator`() {
        val refresh = create(CredentialsProvider { error("Refresh must not read credentials") }, refresh = true)
        val api = refresh.retrofit.create(PolicyApi::class.java)
        val policy = RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE)
        val context = RequestAuthContext(CredentialsSnapshot("stale-context-access", 2))
        server.enqueue(MockResponse().setResponseCode(401))
        val response = api.get(policy, "stale-header", context).execute()
        response.errorBody()?.close()
        assertEquals(401, response.code())
        assertNull(server.takeRequest(1, TimeUnit.SECONDS)!!.getHeader("Authorization"))
        assertEquals(RequestPolicy(), response.raw().request.tag(RequestPolicy::class.java))
        assertNull(response.raw().request.tag(RequestAuthContext::class.java))
        assertFalse(refresh.httpClient.interceptors.any { it is AccessTokenInterceptor })
        assertSame(Authenticator.NONE, refresh.httpClient.authenticator)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `wrong replay tag on POST stays NEVER with the original 401 and one request`() {
        val api = create(CredentialsProvider { CredentialsSnapshot("fixture-access", 3) })
            .retrofit.create(PolicyApi::class.java)
        server.enqueue(MockResponse().setResponseCode(401))
        val policy = RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE)
        val response = api.post("fixture".toRequestBody(), policy).execute()
        response.errorBody()?.close()
        assertEquals(401, response.code())
        assertEquals(ReplayPolicy.NEVER, response.raw().request.tag(RequestPolicy::class.java)!!.replay)
        val recorded = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("POST", recorded.method)
        assertEquals("Bearer fixture-access", recorded.getHeader("Authorization"))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `typed effective policy and original generation reach the native test Authenticator`() {
        val reads = AtomicInteger()
        val current = AtomicReference(CredentialsSnapshot("observed-fixture", 7))
        val base = create(
            CredentialsProvider {
                reads.incrementAndGet()
                current.get()
            },
        )
        val requested = RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE)
        val client = base.httpClient.newBuilder().authenticator { _, response ->
            current.set(CredentialsSnapshot("new-fixture", 8))
            assertEquals(requested, response.request.tag(RequestPolicy::class.java))
            val observed = response.request.tag(RequestAuthContext::class.java)!!.observedCredentials
            assertEquals("observed-fixture", observed.accessToken)
            assertEquals(7L, observed.sessionGeneration)
            null
        }.build()
        val api = base.retrofit.newBuilder().client(client).build().create(PolicyApi::class.java)
        server.enqueue(MockResponse().setResponseCode(401))
        val response = api.get(requested).execute()
        response.errorBody()?.close()
        assertEquals(401, response.code())
        assertEquals(1, reads.get())
        val recorded = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("Bearer observed-fixture", recorded.getHeader("Authorization"))
        assertFalse(recorded.headers.names().any { it.contains("Policy") || it.contains("Generation") })
    }

    private fun create(provider: CredentialsProvider, refresh: Boolean = false): NetworkClient {
        val factory = NetworkClientFactory(
            NetworkConfiguration(
                ApiBaseUrl.debugLoopback(server.url("/").newBuilder().host("127.0.0.1").build().toString()),
                PlatformHeaders("test-platform", "test-host/1", 1),
            ),
            createNetworkJson(),
            provider,
        )
        return (if (refresh) factory.createRefreshClient() else factory.createApiClient()).also(clients::add)
    }

    interface PolicyApi {
        @GET("probe")
        fun get(
            @Tag policy: RequestPolicy? = null,
            @Header("Authorization") authorization: String? = null,
            @Tag context: RequestAuthContext? = null,
        ): Call<ResponseBody>

        @POST("probe")
        fun post(@Body body: okhttp3.RequestBody, @Tag policy: RequestPolicy): Call<ResponseBody>
    }
}
