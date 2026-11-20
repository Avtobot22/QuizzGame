package com.quizzgame.shared.account.presentation

data class NicknameEditorState(
    val value: String,
    val saving: Boolean = false,
    val issue: Issue? = null,
) {
    enum class Issue { INVALID_NAME, ACCOUNT_CHANGED, SAVE_FAILURE }

    override fun toString(): String = "NicknameEditorState(saving=$saving, issue=$issue)"
}
