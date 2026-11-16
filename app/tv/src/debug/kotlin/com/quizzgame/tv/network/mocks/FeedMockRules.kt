package com.quizzgame.tv.network.mocks

import dev.androidmock.core.dsl.BodyAssetResolver
import dev.androidmock.core.dsl.RulesDsl
import dev.androidmock.core.request.HeaderEntry
import dev.androidmock.core.request.RequestSnapshot
import dev.androidmock.core.response.ResponseBody
import dev.androidmock.core.response.ResponseSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_OK

internal fun RulesDsl.feedMockRules(assets: BodyAssetResolver, json: Json) {
    rule("catalog-feed", "/api/tv/feed") {
        match { method("GET") }
        respondWith { request, _ ->
            val query = request.url.query.associate { it.name to it.value }
            when {
                query["limit"] != "6" || query["railLimit"] != "8" -> ResponseSpec(status = HTTP_BAD_REQUEST)
                !query.containsKey("cursor") -> feedAsset(assets, "feed-page-1.json")
                query["cursor"] == "opaque-feed-after-2-sections" -> feedAsset(assets, "feed-page-2.json")
                else -> ResponseSpec(status = HTTP_BAD_REQUEST)
            }.withFeedScenario(request)
        }
    }
    val first = json.parseToJsonElement(assets.read("mock/bodies/feed-page-1.json").decodeToString()).jsonObject
    first.getValue("sections").jsonArray.forEach { section ->
        val id = section.jsonObject.getValue("id").jsonPrimitive.content
        rule("catalog-rail-$id", "/api/tv/feed/sections/$id/items") {
            match { method("GET") }
            respondWith { request, _ ->
                val query = request.url.query.associate { it.name to it.value }
                if (query["limit"] == "12" && query["cursor"] == "opaque-rail-a-after-2" && id == RAIL_A) {
                    feedAsset(assets, "feed-rail-a-page-2.json").withFeedScenario(request)
                } else {
                    ResponseSpec(status = HTTP_BAD_REQUEST)
                }
            }
        }
    }
    val entryIds = listOf("001", "002", "003", "004", "005")
    entryIds.forEach { suffix ->
        val id = "78e2b5b5-05e6-43f5-9580-2e88ec713$suffix"
        rule("catalog-paged-entry-$suffix", "/api/tv/quiz-entries/$id") {
            match { method("GET") }
            respondWith { _, _ ->
                val body = assets.read("mock/bodies/quiz-entry.json").decodeToString()
                    .replace("58e2b5b5-05e6-43f5-1580-8e88ec7130fd", id)
                ResponseSpec(
                    status = HTTP_OK,
                    headers = listOf(HeaderEntry("Content-Type", "application/json")),
                    body = ResponseBody.Bytes(body.encodeToByteArray()),
                )
            }
        }
    }
}

private fun feedAsset(assets: BodyAssetResolver, name: String) = ResponseSpec(
    status = HTTP_OK,
    headers = listOf(HeaderEntry("Content-Type", "application/json")),
    body = ResponseBody.Bytes(assets.read("mock/bodies/$name")),
)

private fun ResponseSpec.withFeedScenario(request: RequestSnapshot): ResponseSpec =
    if (request.url.query.any { it.name == "cursor" }) FeedMockScenario.nextResponse() ?: this else this

private const val RAIL_A = "58e2b5b5-05e6-43f5-9580-8e88ec7130fd"
