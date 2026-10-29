package com.quizzgame.core.session.api

import com.quizzgame.core.network.ApiBaseUrl
import com.quizzgame.core.network.CredentialsProvider
import com.quizzgame.core.network.NetworkClient
import com.quizzgame.core.network.NetworkClientFactory
import com.quizzgame.core.network.NetworkConfiguration
import com.quizzgame.core.network.NetworkTimeouts
import com.quizzgame.core.network.PlatformHeaders
import okhttp3.Call
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

abstract class RefreshTestFixture {

    private val fixtures = mutableListOf<Fixture>()

    @After
    fun closeFixtures() {
        fixtures.forEach { fixture ->
            fixture.network.httpClient.dispatcher.cancelAll()
            fixture.network.httpClient.connectionPool.evictAll()
            fixture.network.httpClient.dispatcher.executorService.shutdownNow()
            fixture.server.shutdown()
        }
    }

    protected fun open(timeouts: NetworkTimeouts = NetworkTimeouts()): Fixture {
        val server = MockWebServer()
        server.start(InetAddress.getByName("127.0.0.1"), 0)
        val network = NetworkClientFactory(
            NetworkConfiguration(
                ApiBaseUrl.debugLoopback(server.url("/").newBuilder().host("127.0.0.1").build().toString()),
                PlatformHeaders("test-tv", "refresh-tests/1", 1),
                timeouts,
            ),
            CredentialsProvider { error("Refresh must not read the access provider") },
        ).createRefreshClient()
        val released = AtomicInteger()
        val http = network.httpClient.newBuilder().eventListener(
            object : EventListener() {
                override fun connectionReleased(call: Call, connection: Connection) {
                    released.incrementAndGet()
                }
            },
        ).build()
        return Fixture(
            server,
            network,
            RefreshClient(network.retrofit.newBuilder().client(http).build(), RefreshEndpoint(REFRESH_PATH)),
            released,
        ).also(fixtures::add)
    }

    protected class Fixture(
        val server: MockWebServer,
        val network: NetworkClient,
        val client: RefreshClient,
        val releasedConnections: AtomicInteger,
    )
}
