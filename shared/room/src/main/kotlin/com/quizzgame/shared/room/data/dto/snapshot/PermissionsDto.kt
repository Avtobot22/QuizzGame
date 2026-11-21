package com.quizzgame.shared.room.data.dto.snapshot

import kotlinx.serialization.Serializable

@Serializable
internal data class PermissionsDto(
    val canStart: Boolean,
    val canClose: Boolean,
    val canKick: Boolean,
    val canReady: Boolean,
)
