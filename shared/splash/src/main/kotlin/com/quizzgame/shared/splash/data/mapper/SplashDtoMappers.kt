package com.quizzgame.shared.splash.data.mapper

import com.quizzgame.shared.splash.data.dto.AccountDto
import com.quizzgame.shared.splash.data.dto.LaunchDto
import com.quizzgame.shared.splash.data.dto.UserDto
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID
import kotlinx.serialization.SerializationException

fun LaunchDto.validated(): UUID = parseId(id)

fun UserDto.toDomain(): CurrentUser = CurrentUser(
    parseId(id),
    account.instant(),
    account.email,
    account.phoneNumber,
    profile.displayName,
    profile.avatarUrl,
    roles.toList(),
)

fun AccountDto.instant(): Instant = try {
    Instant.parse(createdAt)
} catch (_: DateTimeParseException) {
    throw SerializationException("Invalid account timestamp")
}

private fun parseId(value: String): UUID = try {
    UUID.fromString(value).also {
        require(it.toString().equals(value, ignoreCase = true))
    }
} catch (_: IllegalArgumentException) {
    throw SerializationException("Invalid response identifier")
}
