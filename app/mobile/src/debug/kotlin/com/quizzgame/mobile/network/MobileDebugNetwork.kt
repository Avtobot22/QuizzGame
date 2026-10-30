package com.quizzgame.mobile.network

import com.quizzgame.core.network.CredentialsProvider
import com.quizzgame.core.network.createNetworkJson
import com.quizzgame.core.session.RefreshRevocationPolicy
import com.quizzgame.core.session.SessionManager
import com.quizzgame.core.session.api.RefreshEndpoint
import com.quizzgame.debug.network.DebugNetworkConfiguration
import com.quizzgame.debug.network.DebugNetworkGraph
import com.quizzgame.debug.network.DebugNetworkMode
import com.quizzgame.mobile.network.mocks.mobileMockRules
import dev.androidmock.core.dsl.BodyAssetResolver

fun openMobileDebugNetwork(
    configuration: DebugNetworkConfiguration,
    bodyAssets: BodyAssetResolver? = null,
    credentialsProvider: CredentialsProvider = CredentialsProvider.guest(),
): DebugNetworkGraph {
    val json = createNetworkJson()
    val rules = if (configuration.mode == DebugNetworkMode.MOCK) {
        mobileMockRules(requireNotNull(bodyAssets), json)
    } else {
        emptyList()
    }
    return DebugNetworkGraph.open(configuration, json, mockRules = rules, credentialsProvider = credentialsProvider)
}

fun openMobileDebugNetwork(
    configuration: DebugNetworkConfiguration,
    session: SessionManager,
    refreshEndpoint: RefreshEndpoint,
    bodyAssets: BodyAssetResolver? = null,
    revocationPolicy: RefreshRevocationPolicy = RefreshRevocationPolicy.never(),
): DebugNetworkGraph {
    val json = createNetworkJson()
    val rules = if (configuration.mode == DebugNetworkMode.MOCK) {
        mobileMockRules(requireNotNull(bodyAssets), json)
    } else {
        emptyList()
    }
    return DebugNetworkGraph.openWithSession(configuration, json, session, refreshEndpoint, rules, revocationPolicy)
}
