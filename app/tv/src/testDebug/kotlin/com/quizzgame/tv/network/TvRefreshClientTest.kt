package com.quizzgame.tv.network

import com.quizzgame.core.network.CredentialsProvider
import com.quizzgame.core.session.api.RefreshException
import com.quizzgame.core.session.api.RefreshFailure
import dev.androidmock.core.dsl.mockRules
import dev.androidmock.core.response.ResponseSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.concurrent.atomic.AtomicInteger

private const val INSTALLATION_ID = "e4a508be-bb66-46d3-91f6-7b4cabfc977b"

class TvRefreshClientTest : DebugNetworkTestFixture() {
    @Test
    fun packagedRefreshFixtureUsesTheSameSDKServerWithoutReadingAccessCredentials() {
        val graph = openMock(credentialsProvider = CredentialsProvider { error("Refresh must not read access") })
        assertEquals(graph.api.retrofit.baseUrl(), graph.refresh.retrofit.baseUrl())
        val pair = graph.tvRefreshClient().newCall("fixture-tv-refresh-1", INSTALLATION_ID).execute()
        assertEquals("fixture-tv-access-2", pair.access)
        assertEquals("fixture-tv-refresh-2", pair.refresh)
    }

    @Test
    fun typedRefreshUsesTheExactTVContractAndDoesNotAuthenticateItsOwn401() {
        val calls = AtomicInteger()
        val graph = openMock(
            credentialsProvider = CredentialsProvider { error("Refresh must not read access") },
            rules = mockRules {
                rule("refresh", "/api/tv/auth/refresh") {
                    match {
                        method("POST")
                        bodyExactUtf8(
                            """{"refreshToken":"fixture-tv-refresh-1","deviceId":"$INSTALLATION_ID"}""",
                        )
                    }
                    respondWith { request, _ ->
                        calls.incrementAndGet()
                        assertTrue(request.headers.values("Authorization").isEmpty())
                        assertEquals(listOf("test-tv"), request.headers.values("X-Client-Platform"))
                        ResponseSpec(status = HTTP_UNAUTHORIZED)
                    }
                }
            },
        )
        val failure = assertThrows(RefreshException::class.java) {
            graph.tvRefreshClient().newCall("fixture-tv-refresh-1", INSTALLATION_ID).execute()
        }
        assertEquals(RefreshFailure.Http(HTTP_UNAUTHORIZED), failure.failure)
        assertEquals(1, calls.get())
    }
}
