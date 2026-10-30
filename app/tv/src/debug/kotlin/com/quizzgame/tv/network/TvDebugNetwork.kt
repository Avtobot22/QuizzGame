package com.quizzgame.tv.network

import com.quizzgame.core.network.CredentialsProvider
import com.quizzgame.core.network.createNetworkJson
import com.quizzgame.core.session.RefreshRevocationPolicy
import com.quizzgame.core.session.SessionManager
import com.quizzgame.core.session.api.RefreshEndpoint
import com.quizzgame.debug.network.DebugNetworkConfiguration
import com.quizzgame.debug.network.DebugNetworkGraph
import com.quizzgame.debug.network.DebugNetworkMode
import com.quizzgame.tv.network.mocks.tvMockRules
import dev.androidmock.core.dsl.BodyAssetResolver

fun openTvDebugNetwork(
    configuration: DebugNetworkConfiguration,
    bodyAssets: BodyAssetResolver? = null,
    credentialsProvider: CredentialsProvider = CredentialsProvider.guest(),
): DebugNetworkGraph {
    val json = createNetworkJson()
    val rules = if (configuration.mode == DebugNetworkMode.MOCK) {
        tvMockRules(requireNotNull(bodyAssets), json)
    } else {
        emptyList()
    }
    return DebugNetworkGraph.open(configuration, json, mockRules = rules, credentialsProvider = credentialsProvider)
}

fun openTvDebugNetwork(
    configuration: DebugNetworkConfiguration,
    session: SessionManager,
    bodyAssets: BodyAssetResolver? = null,
    revocationPolicy: RefreshRevocationPolicy = RefreshRevocationPolicy.never(),
): DebugNetworkGraph {
    val json = createNetworkJson()
    val rules = if (configuration.mode == DebugNetworkMode.MOCK) {
        tvMockRules(requireNotNull(bodyAssets), json)
    } else {
        emptyList()
    }
    return DebugNetworkGraph.openWithSession(
        configuration,
        json,
        session,
        RefreshEndpoint("api/tv/auth/refresh"),
        rules,
        revocationPolicy,
    )
}
