package com.quizzgame.shared.catalog.data.dto.item

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object FeedItemSerializer : JsonContentPolymorphicSerializer<FeedItemDto>(FeedItemDto::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<FeedItemDto> =
        when (element.jsonObject["kind"]?.jsonPrimitive?.content) {
            "quiz" -> QuizCardDto.serializer()
            else -> IgnoredFeedItemDto.serializer()
        }
}
