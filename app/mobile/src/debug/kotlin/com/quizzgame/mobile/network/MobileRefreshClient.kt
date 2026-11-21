package com.quizzgame.mobile.network

import com.quizzgame.core.session.refresh.api.RefreshClient
import com.quizzgame.core.session.refresh.api.RefreshEndpoint
import com.quizzgame.debug.network.DebugNetworkGraph

/**
 * Borrows the graph's isolated refresh transport. The Mobile backend path must be supplied by its caller.
 */
fun DebugNetworkGraph.mobileRefreshClient(endpoint: RefreshEndpoint): RefreshClient =
    RefreshClient(refresh.retrofit, endpoint, refresh.results)
