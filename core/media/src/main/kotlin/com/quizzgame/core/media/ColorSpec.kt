package com.quizzgame.core.media

sealed interface ColorSpec {
    data class Token(val name: String) : ColorSpec
    data class Hex(val light: Long, val dark: Long) : ColorSpec
}
