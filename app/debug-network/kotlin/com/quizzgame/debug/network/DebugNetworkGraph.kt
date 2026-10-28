package com.quizzgame.debug.network

import com.quizzgame.core.network.ApiBaseUrl
import com.quizzgame.core.network.NetworkClient
import com.quizzgame.core.network.NetworkClientFactory
import com.quizzgame.core.network.NetworkConfiguration
import dev.androidmock.core.engine.RuleEngine
import dev.androidmock.core.rule.Rule
import dev.androidmock.okhttp.server.LoopbackMockServer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class DebugNetworkClient internal constructor(val httpClient: OkHttpClient, val retrofit: Retrofit)

class DebugNetworkGraph private constructor(
    val mode: DebugNetworkMode,
    val api: DebugNetworkClient,
    val refresh: DebugNetworkClient,
    private val closed: AtomicBoolean,
    private val mockServer: LoopbackMockServer?,
) : Closeable {

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        mockServer.use {
            closeClient(api.httpClient)
            closeClient(refresh.httpClient)
        }
    }

    companion object {

        fun open(
            configuration: DebugNetworkConfiguration,
            mockRules: List<Rule> = emptyList(),
        ): DebugNetworkGraph {
            val server = if (configuration.mode == DebugNetworkMode.MOCK) {
                val engine = RuleEngine()
                engine.replaceRules(mockRules)
                LoopbackMockServer(engine).also { it.start() }
            } else {
                null
            }
            val clients = mutableListOf<DebugNetworkClient>()
            var acquired = false
            try {
                val baseUrl = server?.baseUrl?.let { ApiBaseUrl.debugLoopback(it.toString()) }
                    ?: ApiBaseUrl.https(requireNotNull(configuration.realBaseUrl))
                val factory = NetworkClientFactory(
                    NetworkConfiguration(baseUrl, configuration.headers, configuration.timeouts),
                )
                val closed = AtomicBoolean(false)
                val api = decorate(factory.createApiClient(), closed, server != null).also(clients::add)
                val refresh = decorate(factory.createRefreshClient(), closed, server != null).also(clients::add)
                val graph = DebugNetworkGraph(configuration.mode, api, refresh, closed, server)
                acquired = true
                return graph
            } finally {
                if (!acquired) {
                    clients.forEach { closeClient(it.httpClient) }
                    server?.close()
                }
            }
        }

        private fun decorate(base: NetworkClient, closed: AtomicBoolean, mock: Boolean): DebugNetworkClient {
            val builder = base.httpClient.newBuilder().addInterceptor { chain ->
                if (closed.get()) throw IOException("Debug network graph is closed")
                chain.proceed(chain.request())
            }
            if (mock) builder.addInterceptor(DebugMockFailureInterceptor())
            val client = builder.build()
            return DebugNetworkClient(client, base.retrofit.newBuilder().client(client).build())
        }
    }
}

private fun closeClient(client: OkHttpClient) {
    client.dispatcher.cancelAll()
    client.connectionPool.evictAll()
    client.dispatcher.executorService.shutdownNow()
}
