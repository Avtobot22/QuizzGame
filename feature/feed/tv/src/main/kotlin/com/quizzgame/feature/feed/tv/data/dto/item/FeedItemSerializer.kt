package com.quizzgame.feature.feed.tv.data.dto.item

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object FeedItemSerializer : JsonContentPolymorphicSerializer<FeedItemDto>(FeedItemDto::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<FeedItemDto> =
        when (element.jsonObject["kind"]?.jsonPrimitive?.content) {
            "quiz" -> QuizCardDto.serializer()
            else -> IgnoredFeedItemDto.serializer()
        }
}
