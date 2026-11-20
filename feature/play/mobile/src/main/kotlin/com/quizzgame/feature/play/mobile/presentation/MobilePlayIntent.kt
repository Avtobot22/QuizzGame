package com.quizzgame.feature.play.mobile.presentation

internal sealed interface MobilePlayIntent {
    data class EditCode(val value: String) : MobilePlayIntent
    data class EditNickname(val value: String) : MobilePlayIntent
    data class RestoreDraft(val code: String, val nickname: String) : MobilePlayIntent
    data class Qr(val text: String) : MobilePlayIntent
    data class CameraError(val validation: MobilePlayValidation) : MobilePlayIntent
    data object Connect : MobilePlayIntent
    data object Resume : MobilePlayIntent
    data object NavigationHandled : MobilePlayIntent
}
