package com.quizzgame.shared.auth.domain.entity

enum class AuthMethod(val wireId: String) {
    EMAIL("otp.email"),
    SMS("otp.sms"),
    GOOGLE("oauth.google"),
    YANDEX("oauth.yandex"),
}
