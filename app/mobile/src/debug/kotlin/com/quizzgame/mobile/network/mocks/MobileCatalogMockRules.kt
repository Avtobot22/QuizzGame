package com.quizzgame.mobile.network.mocks

import dev.androidmock.core.dsl.BodyAssetResolver
import dev.androidmock.core.dsl.RulesDsl
import dev.androidmock.core.request.HeaderEntry
import dev.androidmock.core.response.ResponseBody
import dev.androidmock.core.response.ResponseSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun RulesDsl.mobileCatalogMockRules(assets: BodyAssetResolver, json: Json) {
    rule("catalog-dashboard", "/api/mobile/dashboard") {
        match { method("GET") }
        respondWith { _, _ ->
            MobileCatalogMockScenario.dashboardFailure() ?: catalogAsset(assets, "mobile-dashboard.json")
        }
    }
    rule("catalog-feed", "/api/mobile/feed") {
        match { method("GET") }
        respondWith { request, _ ->
            val query = request.url.query.associate { it.name to it.value }
            when {
                query["limit"] != "5" || query["railLimit"] != "6" -> ResponseSpec(status = 400)
                !query.containsKey("cursor") -> {
                    MobileCatalogMockScenario.recordFeedLoad()
                    catalogAsset(assets, "mobile-feed-page-1.json")
                }
                query["cursor"] == "opaque-mobile-feed-2" -> MobileCatalogMockScenario.appendFailure()
                    ?: catalogAsset(assets, "mobile-feed-page-2.json")
                else -> ResponseSpec(status = 400)
            }
        }
    }
    rule("catalog-rail", "/api/mobile/feed/sections/ebc9675e-2cd9-4ed7-a4b4-1d279d2ed004/items") {
        match { method("GET") }
        respondWith { request, _ ->
            val query = request.url.query.associate { it.name to it.value }
            if (query["cursor"] == "opaque-mobile-rail-2" && query["limit"] == "12") {
                MobileCatalogMockScenario.appendFailure() ?: catalogAsset(assets, "mobile-feed-rail-page-2.json")
            } else {
                ResponseSpec(status = 400)
            }
        }
    }
    quizEntryMockRules(assets, json)
}

private fun RulesDsl.quizEntryMockRules(assets: BodyAssetResolver, json: Json) {
    val first = json.parseToJsonElement(assets.read("mock/bodies/mobile-feed-page-1.json").decodeToString()).jsonObject
    val more = json.parseToJsonElement(
        assets.read("mock/bodies/mobile-feed-rail-page-2.json").decodeToString(),
    ).jsonObject
    val cards = first.getValue("sections").jsonArray.flatMap { section ->
        val fields = section.jsonObject
        fields["items"]?.jsonArray?.toList() ?: listOfNotNull(fields["item"])
    } + more.getValue("items").jsonArray
    cards.forEach { card ->
        val id = card.jsonObject.getValue("entryId").jsonPrimitive.content
        rule("catalog-entry-$id", "/api/mobile/quiz-entries/$id") {
            match { method("GET") }
            respondWith { _, _ ->
                val body = assets.read("mock/bodies/mobile-quiz-entry.json").decodeToString()
                    .replace("ebc9675e-2cd9-4ed7-a4b4-1d279d2ed008", id)
                    .replace("История", card.jsonObject.getValue("title").jsonPrimitive.content)
                catalogResponse(body.encodeToByteArray())
            }
        }
    }
}

private fun catalogAsset(assets: BodyAssetResolver, name: String) = catalogResponse(assets.read("mock/bodies/$name"))
private fun catalogResponse(bytes: ByteArray) = ResponseSpec(
    status = 200,
    headers = listOf(HeaderEntry("Content-Type", "application/json")),
    body = ResponseBody.Bytes(bytes),
)
