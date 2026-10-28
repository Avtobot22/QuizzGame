package com.quizzgame.mobile.network.mocks

import dev.androidmock.core.dsl.BodyAssetResolver
import dev.androidmock.core.dsl.mockRules
import dev.androidmock.core.rule.Rule
import kotlinx.serialization.json.Json

internal fun mobileMockRules(assets: BodyAssetResolver): List<Rule> = mockRules(assets) {
    rule("mobile-transport-probe", "/debug/mobile/probe") {
        match { method("GET") }
        respond {
            bodyJson(Json.parseToJsonElement(assets.read("mock/bodies/mobile-probe.json").decodeToString()))
        }
    }
}
