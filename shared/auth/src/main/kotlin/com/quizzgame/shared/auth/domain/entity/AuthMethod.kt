package com.quizzgame.shared.auth.domain.entity

enum class AuthMethod(val wireId: String, val usesOtp: Boolean = false) {
    EMAIL("otp.email", usesOtp = true),
    SMS("otp.sms", usesOtp = true),
    GOOGLE("oauth.google"),
    YANDEX("oauth.yandex"),
}
