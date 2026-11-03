package com.quizzgame.debug.network

import android.content.Context
import com.quizzgame.app.network.ApiClientHandle
import com.quizzgame.app.network.AppNetworkGraph
import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.app.network.AppNetworkSettings
import com.quizzgame.debug.network.mocks.DebugMockAssets
import dev.androidmock.core.dsl.BodyAssetResolver
import dev.androidmock.core.dsl.mockRules
import dev.androidmock.core.rule.Rule
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

fun debugAppNetworkFactory(
    context: Context,
    settings: AppNetworkSettings,
    refreshFixture: String,
    failNetworkOnce: Boolean,
    platformRules: (BodyAssetResolver, Json) -> List<Rule>,
): AppNetworkGraphFactory {
    val failOnce = AtomicBoolean(failNetworkOnce)
    return AppNetworkGraphFactory { session, json ->
        if (failOnce.getAndSet(false)) throw IOException("Simulated local transport startup failure")
        val mode = DebugNetworkMode.valueOf(settings.networkMode)
        if (mode == DebugNetworkMode.REAL) settings.realNetworkConfiguration()
        val assets = DebugMockAssets.from(context.assets)
        val rules = if (mode == DebugNetworkMode.MOCK) {
            appMockRules(assets, settings, refreshFixture, json, platformRules)
        } else {
            emptyList()
        }
        val graph = DebugNetworkGraph.openWithSession(
            DebugNetworkConfiguration(
                mode,
                settings.headers,
                realBaseUrl = settings.apiBaseUrl.takeIf { mode == DebugNetworkMode.REAL },
                timeouts = settings.timeouts,
            ),
            json,
            session,
            settings.refreshEndpoint,
            rules,
        )
        AppNetworkGraph(ApiClientHandle(graph.api.retrofit, graph.api.results), graph)
    }
}

private fun appMockRules(
    assets: BodyAssetResolver,
    settings: AppNetworkSettings,
    refreshFixture: String,
    json: Json,
    platformRules: (BodyAssetResolver, Json) -> List<Rule>,
): List<Rule> = try {
    mockRules(assets) {
        rule("app-refresh", "/${settings.refreshEndpoint.path}") {
            match { method("POST") }
            respond { bodyJson(json.parseToJsonElement(assets.read(refreshFixture).decodeToString())) }
        }
    } + platformRules(assets, json)
} catch (failure: IOException) {
    throw IllegalStateException("Packaged debug fixtures are unavailable", failure)
}
