package com.quizzgame.feature.play.mobile.presentation

import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.session.RoomId

internal data class MobilePlayState(
    val code: String = "",
    val nickname: String = "",
    val identity: MobilePlayJoinIdentity = MobilePlayJoinIdentity.Guest,
    val editProfileName: Boolean = false,
    val joining: Boolean = false,
    val validation: MobilePlayValidation? = null,
    val failure: RoomFailure? = null,
    val retryAfter: Long = 0,
    val activeRoom: RoomId? = null,
    val destination: RoomId? = null,
) {
    val account: MobilePlayJoinIdentity.Account? get() = identity as? MobilePlayJoinIdentity.Account
    val checkingIdentity: Boolean get() = identity == MobilePlayJoinIdentity.Checking || account?.refreshing == true
    val profileName: String? get() = account?.profileName?.takeUnless { editProfileName }
    val showNickname: Boolean get() = identity != MobilePlayJoinIdentity.Checking && profileName == null
}

internal enum class MobilePlayValidation { CODE, NICKNAME, QR_INVALID, CAMERA_DENIED, CAMERA_UNAVAILABLE }
