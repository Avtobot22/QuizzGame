package com.quizzgame.mobile.network

import com.quizzgame.debug.network.DebugNetworkConfiguration
import com.quizzgame.debug.network.DebugNetworkGraph
import com.quizzgame.debug.network.DebugNetworkMode
import com.quizzgame.mobile.network.mocks.mobileMockRules
import dev.androidmock.core.dsl.BodyAssetResolver

fun openMobileDebugNetwork(
    configuration: DebugNetworkConfiguration,
    bodyAssets: BodyAssetResolver? = null,
): DebugNetworkGraph {
    val rules = if (configuration.mode == DebugNetworkMode.MOCK) {
        mobileMockRules(requireNotNull(bodyAssets))
    } else {
        emptyList()
    }
    return DebugNetworkGraph.open(configuration, mockRules = rules)
}
