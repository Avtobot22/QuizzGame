package com.quizzgame.tv.network

import com.quizzgame.core.network.NetworkTimeouts
import com.quizzgame.core.network.PlatformHeaders
import com.quizzgame.debug.network.DebugNetworkConfiguration
import com.quizzgame.debug.network.DebugNetworkGraph
import com.quizzgame.debug.network.DebugNetworkMode
import com.quizzgame.debug.network.mocks.DebugMockAssets
import dev.androidmock.core.request.HeaderEntry
import dev.androidmock.core.response.ResponseBody
import dev.androidmock.core.response.ResponseSpec
import dev.androidmock.core.rule.Rule
import org.junit.After
import kotlin.time.Duration

abstract class DebugNetworkTestFixture {

    protected val graphs = mutableListOf<DebugNetworkGraph>()
    protected val headers = PlatformHeaders("test-tv", "test-host/1", 1)

    @After
    fun closeGraphs() {
        graphs.forEach(DebugNetworkGraph::close)
    }

    protected fun openMock(
        timeouts: NetworkTimeouts = NetworkTimeouts(),
        rules: List<Rule>? = null,
    ): DebugNetworkGraph {
        val configuration = DebugNetworkConfiguration(DebugNetworkMode.MOCK, headers, timeouts = timeouts)
        val graph = if (rules == null) {
            val assets = DebugMockAssets { path -> checkNotNull(javaClass.classLoader!!.getResourceAsStream(path)) }
            openTvDebugNetwork(configuration, bodyAssets = assets)
        } else {
            DebugNetworkGraph.open(configuration, mockRules = rules)
        }
        return graph.also(graphs::add)
    }
}

internal fun jsonResponse(body: String, delay: Duration = Duration.ZERO): ResponseSpec = ResponseSpec(
    headers = listOf(HeaderEntry("Content-Type", "application/json")),
    body = ResponseBody.Bytes(body.toByteArray()),
    delay = delay,
)
