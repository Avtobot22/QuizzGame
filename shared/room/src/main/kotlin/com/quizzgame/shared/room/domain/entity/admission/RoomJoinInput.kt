package com.quizzgame.shared.room.domain.entity.admission

import com.quizzgame.shared.account.domain.entity.PlayerNickname

/** Ограничения wire-контракта. PIN остаётся строкой, включая ведущие нули. */
object RoomJoinInput {
    const val MAX_CODE_LENGTH = 10
    private val codePattern = Regex("[0-9]{4,$MAX_CODE_LENGTH}")

    fun validCode(code: String): Boolean = codePattern.matches(code)

    fun nickname(input: String): String? = PlayerNickname.normalize(input)
}
