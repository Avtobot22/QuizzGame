package com.quizzgame.feature.feed.tv.data.dto.section

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object FeedSectionSerializer : JsonContentPolymorphicSerializer<FeedSectionDto>(FeedSectionDto::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<FeedSectionDto> =
        when (element.jsonObject["kind"]?.jsonPrimitive?.content) {
            "rail" -> RailDto.serializer()
            else -> IgnoredFeedSectionDto.serializer()
        }
}
