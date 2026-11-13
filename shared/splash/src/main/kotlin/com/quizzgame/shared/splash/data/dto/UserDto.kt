package com.quizzgame.shared.splash.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val account: AccountDto,
    val profile: ProfileDto,
    val roles: List<String>,
)
