package com.quizzgame.shared.splash.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val createdAt: String,
    val email: String? = null,
    val phoneNumber: String? = null,
)
