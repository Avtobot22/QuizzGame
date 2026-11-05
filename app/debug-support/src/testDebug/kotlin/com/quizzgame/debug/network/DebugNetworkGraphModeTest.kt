package com.quizzgame.debug.network

import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.network.config.createNetworkJson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugNetworkGraphModeTest {
    @Test
    fun mockDiagnosticsAreInstalledOnlyForTheLocalMockTransport() {
        val headers = PlatformHeaders("fixture-platform", "Fixture/1", 1)
        val json = createNetworkJson()
        DebugNetworkGraph.open(DebugNetworkConfiguration(DebugNetworkMode.MOCK, headers), json).use { graph ->
            assertTrue(graph.api.httpClient.interceptors.any { it is DebugMockFailureInterceptor })
            assertTrue(graph.refresh.httpClient.interceptors.any { it is DebugMockFailureInterceptor })
        }
        DebugNetworkGraph.open(
            DebugNetworkConfiguration(DebugNetworkMode.REAL, headers, "https://example.invalid/"),
            json,
        ).use { graph ->
            assertFalse(graph.api.httpClient.interceptors.any { it is DebugMockFailureInterceptor })
            assertFalse(graph.refresh.httpClient.interceptors.any { it is DebugMockFailureInterceptor })
        }
    }
}
