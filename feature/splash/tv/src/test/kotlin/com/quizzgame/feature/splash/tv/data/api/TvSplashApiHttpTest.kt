package com.quizzgame.feature.splash.tv.data.api

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.CredentialsProvider
import com.quizzgame.core.network.auth.CredentialsSnapshot
import com.quizzgame.core.network.client.NetworkClient
import com.quizzgame.core.network.client.NetworkClientFactory
import com.quizzgame.core.network.config.ApiBaseUrl
import com.quizzgame.core.network.config.NetworkConfiguration
import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.feature.splash.tv.data.TvSplashRemoteDataSource
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import java.net.InetAddress
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class TvSplashApiHttpTest {
    private val server = MockWebServer()
    private lateinit var client: NetworkClient

    @Before
    fun setup() { server.start(InetAddress.getByName("127.0.0.1"), 0) }

    @After
    fun cleanup() {
        if (::client.isInitialized) {
            client.httpClient.dispatcher.cancelAll()
            client.httpClient.connectionPool.evictAll()
            client.httpClient.dispatcher.executorService.shutdownNow()
        }
        server.shutdown()
    }

    @Test
    fun guestLaunchUsesStaticPlatformPathAndHeadersWithoutBearerOrBody() = runTest {
        val source = source(CredentialsProvider.guest())
        server.enqueue(MockResponse().setBody("""{"id":"61d779f2-e9cd-42f0-ae96-f69a6ae2926f"}"""))
        source.launch()
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/tv/launch", request.path)
        assertEquals("android-tv", request.getHeader("X-Client-Platform"))
        assertEquals("1", request.getHeader("X-Endpoint-Version"))
        assertEquals(0L, request.bodySize)
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun currentUserUsesStaticPlatformPathAndMapsTheSharedProfile() = runTest {
        val source = source(CredentialsProvider { CredentialsSnapshot("fixture-access", 8) })
        server.enqueue(
            MockResponse().setBody(
                """{"id":"719278b4-e7af-4e50-af40-25701f2b2b27",""" +
                    """"account":{"createdAt":"2026-11-05T10:30:00Z"},"profile":{},"roles":[]}""",
            ),
        )
        val result = source.currentUser() as ApiCallResult.Executed<CurrentUser>
        val user = (result.result as NetworkResult.Success<CurrentUser>).value
        assertEquals("719278b4-e7af-4e50-af40-25701f2b2b27", user.id.toString())
        val request = server.takeRequest()
        assertEquals("/api/tv/user", request.path)
        assertEquals("1", request.getHeader("X-Endpoint-Version"))
        assertEquals("Bearer fixture-access", request.getHeader("Authorization"))
        assertEquals(0L, request.bodySize)
    }

    private fun source(credentials: CredentialsProvider): TvSplashRemoteDataSource {
        client = NetworkClientFactory(
            NetworkConfiguration(
                ApiBaseUrl.debugLoopback(server.url("/").newBuilder().host("127.0.0.1").build().toString()),
                PlatformHeaders("android-tv", "QuizzGame-test"),
            ),
            createNetworkJson(),
            credentials,
        ).createApiClient()
        val executor = object : ApiCallExecutor {
            override suspend fun <S : Any, T> call(service: Class<S>, request: suspend S.() -> T): ApiCallResult<T> =
                ApiCallResult.Executed(client.results.call { request(client.retrofit.create(service)) })

            override suspend fun <S : Any, T> response(
                service: Class<S>,
                request: suspend S.() -> Response<T>,
            ): ApiCallResult<T> = error("Unexpected response call")
        }
        return TvSplashRemoteDataSource(executor)
    }
}
