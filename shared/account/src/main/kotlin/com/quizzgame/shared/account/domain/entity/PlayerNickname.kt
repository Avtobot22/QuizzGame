package com.quizzgame.shared.account.domain.entity

import java.text.Normalizer

/** Имя в профиле и в лобби подчиняется одним ограничениям. */
object PlayerNickname {
    const val MAX_LENGTH = 24

    fun normalize(input: String): String? {
        val value = Normalizer.normalize(input.trim(), Normalizer.Form.NFC)
        return value.takeIf { it.codePointCount(0, it.length) in 1..MAX_LENGTH && it.none(Char::isISOControl) }
    }
}
