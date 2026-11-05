package com.quizzgame.tv.network

import com.quizzgame.core.session.refresh.api.RefreshClient
import com.quizzgame.core.session.refresh.api.RefreshEndpoint
import com.quizzgame.debug.network.DebugNetworkGraph

/** Borrows the graph's isolated refresh transport; closing the graph cancels its calls. */
fun DebugNetworkGraph.tvRefreshClient(): RefreshClient =
    RefreshClient(refresh.retrofit, RefreshEndpoint("api/tv/auth/refresh"), refresh.results)
