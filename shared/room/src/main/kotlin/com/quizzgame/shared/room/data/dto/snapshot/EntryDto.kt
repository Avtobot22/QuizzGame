package com.quizzgame.shared.room.data.dto.snapshot

import kotlinx.serialization.Serializable

@Serializable
internal data class EntryDto(
    val entryId: String,
    val title: String,
)
