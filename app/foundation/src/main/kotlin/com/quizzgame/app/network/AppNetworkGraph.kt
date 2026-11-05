package com.quizzgame.app.network

import com.quizzgame.core.network.api.ApiRuntime
import com.quizzgame.core.network.client.NetworkClientFactory
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.refresh.RefreshRevocationPolicy
import com.quizzgame.core.session.refresh.SessionRefreshCoordinator
import com.quizzgame.core.session.refresh.api.RefreshClient
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class AppNetworkGraph(val api: ApiRuntime, private val resources: Closeable) : Closeable {
    private val closed = AtomicBoolean(false)
    internal val isClosed: Boolean get() = closed.get()

    override fun close() {
        if (closed.compareAndSet(false, true)) resources.close()
    }
}

fun interface AppNetworkGraphFactory {
    fun open(session: SessionManager, json: Json): AppNetworkGraph
}

fun realAppNetworkFactory(
    settings: AppNetworkSettings,
    revocationPolicy: RefreshRevocationPolicy,
): AppNetworkGraphFactory =
    AppNetworkGraphFactory { session, json -> openRealAppNetwork(settings, session, json, revocationPolicy) }

internal fun openRealAppNetwork(
    settings: AppNetworkSettings,
    session: SessionManager,
    json: Json,
    revocationPolicy: RefreshRevocationPolicy,
): AppNetworkGraph {
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
            revocationPolicy,
        )
        val client = NetworkClientFactory(configuration, json, session, coordinator).createApiClient()
        api = client.httpClient
        val owner = requireNotNull(coordinator)
        val graph = AppNetworkGraph(ApiRuntime(client.retrofit, client.results)) {
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
