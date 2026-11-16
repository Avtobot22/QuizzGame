package com.quizzgame.feature.feed.tv.data.dto

import com.quizzgame.shared.catalog.data.mapper.requireCatalog
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

internal object CursorPageInfoSerializer : JsonTransformingSerializer<CursorPageInfoDto>(
    CursorPageInfoDto.serializer(),
) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        requireCatalog(element is JsonObject && "hasMore" in element && "nextCursor" in element)
        return element
    }
}
