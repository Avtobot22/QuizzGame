package com.quizzgame.app.network

import com.quizzgame.app.startup.AppStartupCoordinator
import com.quizzgame.core.network.api.ApiRuntime
import com.quizzgame.core.network.client.NetworkClient
import com.quizzgame.core.network.client.NetworkClientFactory
import com.quizzgame.core.network.config.ApiBaseUrl
import com.quizzgame.core.network.config.NetworkConfiguration
import com.quizzgame.core.network.config.NetworkTimeouts
import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.refresh.RefreshRevocationPolicy
import com.quizzgame.core.session.refresh.SessionRefreshCoordinator
import com.quizzgame.core.session.refresh.api.RefreshClient
import com.quizzgame.core.session.refresh.api.RefreshEndpoint
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

abstract class ApiExecutorTestFixture {
    private val fixtures = mutableListOf<ExecutorFixture>()

    protected suspend fun fixture(
        tokens: TokenPair? = null,
        policy: RefreshRevocationPolicy = RefreshRevocationPolicy.never(),
        timeouts: NetworkTimeouts = NetworkTimeouts(),
    ): ExecutorFixture {
        val server = MockWebServer().apply { start(InetAddress.getByName("127.0.0.1"), 0) }
        val store = ExecutorStore(tokens)
        val session = SessionManager.open(store)
        val configuration = NetworkConfiguration(
            ApiBaseUrl.debugLoopback(server.url("/").toString()),
            PlatformHeaders("feature-fixture", "FeatureFixture/1"),
            timeouts,
        )
        val json = createNetworkJson()
        val refresh = NetworkClientFactory(configuration, json).createRefreshClient()
        val coordinator = SessionRefreshCoordinator(
            session,
            RefreshClient(refresh.retrofit, RefreshEndpoint("auth/refresh"), refresh.results),
            refresh.httpClient.dispatcher.executorService,
            policy,
        )
        val api = NetworkClientFactory(configuration, json, session, coordinator).createApiClient()
        val graph = AppNetworkGraph(ApiRuntime(api.retrofit, api.results)) {
            coordinator.close()
            listOf(api, refresh).forEach(::closeClient)
        }
        return ExecutorFixture(server, store, session, graph).also(fixtures::add)
    }

    @After
    fun closeFixtures() = runBlocking {
        fixtures.forEach { fixture ->
            fixture.startups.forEach { it.shutdown() }
            fixture.graph.close()
            fixture.session.close()
            fixture.server.shutdown()
        }
    }

    protected class ExecutorFixture(
        val server: MockWebServer,
        val store: ExecutorStore,
        val session: SessionManager,
        val graph: AppNetworkGraph,
    ) {
        val startups = mutableListOf<AppStartupCoordinator>()
        val sessionOpens = AtomicInteger()
        val graphOpens = AtomicInteger()

        fun startup(openSession: suspend () -> SessionManager = { session }): AppStartupCoordinator =
            AppStartupCoordinator(
                openSession = {
                    sessionOpens.incrementAndGet()
                    openSession()
                },
                networkFactory = AppNetworkGraphFactory { _, _ ->
                    graphOpens.incrementAndGet()
                    graph
                },
                dispatcher = Dispatchers.Default,
            ).also(startups::add)
    }

    protected class ExecutorStore(tokens: TokenPair?) : SessionStore {
        private var stored = SessionRecord("123e4567-e89b-12d3-a456-426614174000", tokens)
        val tokens get() = stored.tokens
        var reads = 0
        var writes = 0
        var readFailure = false
        override fun read(): SessionRecord {
            reads++
            if (readFailure) throw java.io.IOException("Fixture read failure")
            return stored
        }

        override fun write(tokens: TokenPair?): SessionRecord {
            return writeRecord(SessionRecord(stored.deviceId, tokens))
        }

        override fun writeRecord(record: SessionRecord): SessionRecord {
            writes++
            stored = record
            return stored
        }
    }
}

private fun closeClient(client: NetworkClient) {
    client.httpClient.dispatcher.cancelAll()
    client.httpClient.connectionPool.evictAll()
    client.httpClient.dispatcher.executorService.shutdown()
}
