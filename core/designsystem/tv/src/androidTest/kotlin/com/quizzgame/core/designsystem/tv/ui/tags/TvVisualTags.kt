package com.quizzgame.core.designsystem.tv.ui.tags

internal object TvVisualTags {
    const val ROOT = "visual-root"
    const val SIGN_OUT = "visual-sign-out"
    const val PRIMARY_ACTION = "visual-primary-action"
    const val SECONDARY_ACTION = "visual-secondary-action"
    const val DANGER_ACTION = "visual-danger-action"
    fun tab(label: String): String = "visual-tab-$label"
}
