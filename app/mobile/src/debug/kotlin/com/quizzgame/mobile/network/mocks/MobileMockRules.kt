package com.quizzgame.mobile.network.mocks

import dev.androidmock.core.dsl.BodyAssetResolver
import dev.androidmock.core.dsl.mockRules
import dev.androidmock.core.response.ResponseSpec
import dev.androidmock.core.rule.Rule
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

internal fun mobileMockRules(assets: BodyAssetResolver, json: Json): List<Rule> = mockRules(assets) {
    rule("mobile-transport-probe", "/debug/mobile/probe") {
        match { method("GET") }
        respond {
            bodyJson(json.parseToJsonElement(assets.read("mock/bodies/mobile-probe.json").decodeToString()))
        }
    }
    rule("mobile-refresh-probe", "/debug/mobile/auth/refresh") {
        match { method("POST") }
        respond {
            bodyJson(json.parseToJsonElement(assets.read("mock/bodies/mobile-refresh.json").decodeToString()))
        }
    }
    rule("mobile-session-recovery-probe", "/debug/mobile/session") {
        match { method("GET") }
        respondWith { request, _ ->
            val ready = request.headers.values("Authorization") == listOf("Bearer fixture-mobile-access-2")
            ResponseSpec(status = if (ready) HTTP_OK else HTTP_UNAUTHORIZED)
        }
    }
}
