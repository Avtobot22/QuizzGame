package com.quizzgame.shared.feed.data.dto.section

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object FeedSectionSerializer : JsonContentPolymorphicSerializer<FeedSectionDto>(FeedSectionDto::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<FeedSectionDto> =
        when (element.jsonObject["kind"]?.jsonPrimitive?.content) {
            "hero" -> HeroDto.serializer()
            "rail" -> RailDto.serializer()
            else -> IgnoredFeedSectionDto.serializer()
        }
}
