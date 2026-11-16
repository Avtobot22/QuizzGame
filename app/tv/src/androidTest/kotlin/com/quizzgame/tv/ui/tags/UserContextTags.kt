package com.quizzgame.tv.ui.tags

internal object UserContextTags {
    fun screen(id: Enum<*>): String = "user-context:${id.name}"
}
