package com.quizzgame.mobile.network

import com.quizzgame.core.network.CredentialsProvider
import com.quizzgame.core.network.PlatformHeaders
import com.quizzgame.core.network.createNetworkJson
import com.quizzgame.core.session.api.RefreshEndpoint
import com.quizzgame.core.session.api.RefreshException
import com.quizzgame.core.session.api.RefreshFailure
import com.quizzgame.debug.network.DebugNetworkConfiguration
import com.quizzgame.debug.network.DebugNetworkGraph
import com.quizzgame.debug.network.DebugNetworkMode
import com.quizzgame.debug.network.mocks.DebugMockAssets
import dev.androidmock.core.dsl.mockRules
import dev.androidmock.core.response.ResponseSpec
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.concurrent.atomic.AtomicInteger

private const val INSTALLATION_ID = "e4a508be-bb66-46d3-91f6-7b4cabfc977b"
private const val DEBUG_REFRESH_PATH = "debug/mobile/auth/refresh"

class MobileRefreshClientTest {
    private val graphs = mutableListOf<DebugNetworkGraph>()
    private val configuration = DebugNetworkConfiguration(
        DebugNetworkMode.MOCK,
        PlatformHeaders("test-mobile", "mobile-refresh-tests/1", 1),
    )

    @After
    fun closeGraphs() { graphs.forEach(DebugNetworkGraph::close) }

    @Test
    fun packagedMobileRefreshFixtureUsesTheGraphServerAndExplicitDebugPath() {
        val graph = openMobileDebugNetwork(
            configuration,
            bodyAssets = assets(),
            credentialsProvider = CredentialsProvider { error("Refresh must not read access") },
        ).also(graphs::add)
        assertEquals(graph.api.retrofit.baseUrl(), graph.refresh.retrofit.baseUrl())
        val pair = graph.mobileRefreshClient(RefreshEndpoint(DEBUG_REFRESH_PATH))
            .newCall("fixture-mobile-refresh-1", INSTALLATION_ID).execute()
        assertEquals("fixture-mobile-access-2", pair.access)
        assertEquals("fixture-mobile-refresh-2", pair.refresh)
    }

    @Test
    fun mobileRefreshKeepsItsOwnHeadersBodyAndSingle401WithoutReadingAccess() {
        val calls = AtomicInteger()
        val graph = DebugNetworkGraph.open(
            configuration,
            createNetworkJson(),
            credentialsProvider = CredentialsProvider { error("Refresh must not read access") },
            mockRules = mockRules {
                rule("mobile-refresh", "/$DEBUG_REFRESH_PATH") {
                    match {
                        method("POST")
                        bodyExactUtf8(
                            """{"refreshToken":"fixture-mobile-refresh-1","deviceId":"$INSTALLATION_ID"}""",
                        )
                    }
                    respondWith { request, _ ->
                        calls.incrementAndGet()
                        assertTrue(request.headers.values("Authorization").isEmpty())
                        assertEquals(listOf("test-mobile"), request.headers.values("X-Client-Platform"))
                        ResponseSpec(status = HTTP_UNAUTHORIZED)
                    }
                }
            },
        ).also(graphs::add)
        val failure = assertThrows(RefreshException::class.java) {
            graph.mobileRefreshClient(RefreshEndpoint(DEBUG_REFRESH_PATH))
                .newCall("fixture-mobile-refresh-1", INSTALLATION_ID).execute()
        }
        assertEquals(RefreshFailure.Http(HTTP_UNAUTHORIZED), failure.failure)
        assertEquals(1, calls.get())
    }

    @Test
    fun mobileRefreshGraphsRemainIndependentAndDoNotInheritATVEndpoint() {
        val graphs = List(2) {
            openMobileDebugNetwork(
                configuration,
                bodyAssets = assets(),
            ).also(this.graphs::add)
        }
        assertNotEquals(graphs[0].refresh.retrofit.baseUrl(), graphs[1].refresh.retrofit.baseUrl())
        graphs[0].close()
        val pair = graphs[1].mobileRefreshClient(RefreshEndpoint(DEBUG_REFRESH_PATH))
            .newCall("fixture-mobile-refresh-1", INSTALLATION_ID).execute()
        assertEquals("fixture-mobile-access-2", pair.access)
    }

    private fun assets(): DebugMockAssets = DebugMockAssets { path ->
        checkNotNull(javaClass.classLoader!!.getResourceAsStream(path))
    }
}
