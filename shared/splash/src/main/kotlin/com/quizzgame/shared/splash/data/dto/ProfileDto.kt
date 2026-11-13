package com.quizzgame.shared.splash.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(val displayName: String? = null, val avatarUrl: String? = null)
