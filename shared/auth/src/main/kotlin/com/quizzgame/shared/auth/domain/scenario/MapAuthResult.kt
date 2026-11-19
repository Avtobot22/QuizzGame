package com.quizzgame.shared.auth.domain.scenario

import com.quizzgame.shared.auth.domain.entity.AuthResult

internal inline fun <T, R> AuthResult<T>.map(transform: (T) -> R): AuthResult<R> = when (this) {
    is AuthResult.Success -> AuthResult.Success(transform(value))
    is AuthResult.Failed -> this
}
