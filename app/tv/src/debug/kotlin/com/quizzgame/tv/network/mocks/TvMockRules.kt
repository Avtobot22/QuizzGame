package com.quizzgame.tv.network.mocks

import dev.androidmock.core.dsl.BodyAssetResolver
import dev.androidmock.core.dsl.mockRules
import dev.androidmock.core.rule.Rule
import kotlinx.serialization.json.Json

internal fun tvMockRules(assets: BodyAssetResolver): List<Rule> = mockRules(assets) {
    rule("launch", "/api/tv/launch") {
        match { method("GET") }
        respond {
            bodyJson(Json.parseToJsonElement(assets.read("mock/bodies/launch.json").decodeToString()))
        }
    }
}
