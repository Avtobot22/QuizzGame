package com.quizzgame.shared.feed.data.paging

sealed interface CursorKey {
    data object Start : CursorKey
    data class After(val cursor: String) : CursorKey {
        override fun toString(): String = "After(<redacted>)"
    }
}
