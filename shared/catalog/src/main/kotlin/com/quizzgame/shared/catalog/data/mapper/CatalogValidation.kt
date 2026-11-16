package com.quizzgame.shared.catalog.data.mapper

import kotlinx.serialization.SerializationException
import java.util.UUID

fun requireCatalog(value: Boolean) {
    if (!value) throw SerializationException("Invalid catalog response")
}

fun catalogId(value: String): String {
    val valid = runCatching { UUID.fromString(value).toString() == value.lowercase() }.getOrDefault(false)
    requireCatalog(valid)
    return value
}
