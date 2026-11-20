package com.quizzgame.feature.play.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.feature.play.mobile.presentation.MobilePlayJoinIdentity
import com.quizzgame.feature.play.mobile.presentation.MobilePlayState
import com.quizzgame.feature.play.mobile.presentation.MobilePlayValidation
import com.quizzgame.shared.room.domain.entity.session.RoomId
import java.util.UUID

internal class MobilePlayPreviewProvider : PreviewParameterProvider<MobilePlayState> {
    private val draft = MobilePlayState(code = "031482", nickname = "Саша")
    private val signedIn = draft.copy(nickname = "", identity = MobilePlayJoinIdentity.Account(1, "Майя"))
    override val values = sequenceOf(
        MobilePlayState(),
        draft,
        draft.copy(identity = MobilePlayJoinIdentity.Checking),
        signedIn,
        signedIn.copy(identity = MobilePlayJoinIdentity.Account(1, null)),
        signedIn.copy(editProfileName = true),
        draft.copy(joining = true),
        draft.copy(validation = MobilePlayValidation.CAMERA_DENIED),
        draft.copy(validation = MobilePlayValidation.NICKNAME),
        draft.copy(activeRoom = RoomId(UUID(0, 1))),
    )
}
