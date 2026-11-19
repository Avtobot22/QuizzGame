package com.quizzgame.tv.network.mocks

import com.quizzgame.debug.network.mocks.SplashMockScenario
import com.quizzgame.debug.network.mocks.authMockRules
import dev.androidmock.core.dsl.BodyAssetResolver
import dev.androidmock.core.dsl.mockRules
import dev.androidmock.core.response.ResponseSpec
import dev.androidmock.core.rule.Rule
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

internal fun tvMockRules(assets: BodyAssetResolver, json: Json): List<Rule> = mockRules(assets) {
    authMockRules(json, "tv")
    rule("splash-feature-toggles", "/api/tv/feature-toggles") {
        match { method("GET") }
        respondWith { _, _ ->
            SplashMockScenario.response(SplashMockScenario.Stage.FLAGS, assets.read("mock/bodies/feature-toggles.json"))
        }
    }
    rule("splash-user", "/api/tv/user") {
        match { method("GET") }
        respondWith { _, _ ->
            SplashMockScenario.response(SplashMockScenario.Stage.USER, assets.read("mock/bodies/user.json"))
        }
    }

    rule("catalog-dashboard", "/api/tv/dashboard") {
        match { method("GET") }
        respond {
            bodyJson(json.parseToJsonElement(assets.read("mock/bodies/dashboard.json").decodeToString()))
        }
    }
    feedMockRules(assets, json)
    rule("catalog-quiz-entry", "/api/tv/quiz-entries/58e2b5b5-05e6-43f5-1580-8e88ec7130fd") {
        match { method("GET") }
        respond {
            bodyJson(json.parseToJsonElement(assets.read("mock/bodies/quiz-entry.json").decodeToString()))
        }
    }
    rule("launch", "/api/tv/launch") {
        match { method("GET") }
        respondWith { _, _ ->
            SplashMockScenario.response(SplashMockScenario.Stage.LAUNCH, assets.read("mock/bodies/launch.json"))
        }
    }
    rule("refresh", "/api/tv/auth/refresh") {
        match { method("POST") }
        respond {
            bodyJson(json.parseToJsonElement(assets.read("mock/bodies/refresh.json").decodeToString()))
        }
    }
    rule("session-recovery-probe", "/debug/tv/session") {
        match { method("GET") }
        respondWith { request, _ ->
            val ready = request.headers.values("Authorization") == listOf("Bearer fixture-tv-access-2")
            ResponseSpec(status = if (ready) HTTP_OK else HTTP_UNAUTHORIZED)
        }
    }
}
