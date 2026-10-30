package com.quizzgame.mobile.network

import com.quizzgame.core.session.api.RefreshClient
import com.quizzgame.core.session.api.RefreshEndpoint
import com.quizzgame.debug.network.DebugNetworkGraph

/**
 * Borrows the graph's isolated refresh transport. The Mobile backend path must be supplied by its caller.
 * The packaged MOCK rule uses debug/mobile/auth/refresh only as a debug fixture, not as a backend contract.
 */
fun DebugNetworkGraph.mobileRefreshClient(endpoint: RefreshEndpoint): RefreshClient =
    RefreshClient(refresh.retrofit, endpoint, refresh.results)
