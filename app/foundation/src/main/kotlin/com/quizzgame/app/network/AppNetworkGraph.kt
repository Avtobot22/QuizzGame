package com.quizzgame.app.network

import com.quizzgame.core.network.client.NetworkClientFactory
import com.quizzgame.core.network.result.NetworkResultAdapter
import com.quizzgame.core.session.SessionManager
import com.quizzgame.core.session.SessionRefreshCoordinator
import com.quizzgame.core.session.api.RefreshClient
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class ApiClientHandle(val retrofit: Retrofit, val results: NetworkResultAdapter)

class AppNetworkGraph(val api: ApiClientHandle, private val resources: Closeable) : Closeable {
    private val closed = AtomicBoolean(false)
    override fun close() {
        if (closed.compareAndSet(false, true)) resources.close()
    }
}

fun interface AppNetworkGraphFactory {
    fun open(session: SessionManager, json: Json): AppNetworkGraph
}

fun realAppNetworkFactory(settings: AppNetworkSettings): AppNetworkGraphFactory =
    AppNetworkGraphFactory { session, json -> openRealAppNetwork(settings, session, json) }

internal fun openRealAppNetwork(settings: AppNetworkSettings, session: SessionManager, json: Json): AppNetworkGraph {
    check(settings.networkMode == "REAL")
    val configuration = settings.realNetworkConfiguration()
    val refresh = NetworkClientFactory(configuration, json).createRefreshClient()
    var coordinator: SessionRefreshCoordinator? = null
    var api: OkHttpClient? = null
    var acquired = false
    try {
        coordinator = SessionRefreshCoordinator(
            session,
            RefreshClient(refresh.retrofit, settings.refreshEndpoint, refresh.results),
            refresh.httpClient.dispatcher.executorService,
        )
        val client = NetworkClientFactory(configuration, json, session, coordinator).createApiClient()
        api = client.httpClient
        val owner = requireNotNull(coordinator)
        val graph = AppNetworkGraph(ApiClientHandle(client.retrofit, client.results)) {
            owner.close()
            closeHttpClient(client.httpClient)
            closeHttpClient(refresh.httpClient)
        }
        acquired = true
        return graph
    } finally {
        if (!acquired) {
            coordinator?.close()
            api?.let(::closeHttpClient)
            closeHttpClient(refresh.httpClient)
        }
    }
}

private fun closeHttpClient(client: OkHttpClient) {
    client.dispatcher.cancelAll()
    client.connectionPool.evictAll()
    client.dispatcher.executorService.shutdown()
}
