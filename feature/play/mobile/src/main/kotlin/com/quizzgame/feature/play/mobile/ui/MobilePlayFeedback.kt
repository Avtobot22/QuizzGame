package com.quizzgame.feature.play.mobile.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.mobile.QuizzNoticeBanner
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.feature.play.mobile.R
import com.quizzgame.feature.play.mobile.presentation.MobilePlayState
import com.quizzgame.feature.play.mobile.presentation.MobilePlayValidation
import com.quizzgame.feature.play.mobile.ui.tags.MobilePlayTags
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason

@Composable
internal fun MobilePlayFeedback(state: MobilePlayState) {
    if (state.retryAfter > 0) {
        Text(
            stringResource(R.string.play_wait_to_retry, state.retryAfter),
            Modifier.testTag(MobilePlayTags.VALIDATION),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }
    val scanError = when (state.validation) {
        MobilePlayValidation.QR_INVALID -> R.string.play_invalid_qr
        MobilePlayValidation.CAMERA_DENIED -> R.string.play_permission_denied
        MobilePlayValidation.CAMERA_UNAVAILABLE -> R.string.play_camera_unavailable
        else -> null
    }
    val failure = state.failure
    when {
        scanError != null -> PlayNotice(stringResource(scanError), QuizzStatusIcon.ROOM)
        failure == null || failure.reason == RoomFailureReason.NAME_TAKEN -> Unit
        failure.reason == RoomFailureReason.PARTICIPANT_AUTH_REQUIRED ->
            PlayNotice(stringResource(R.string.play_auth_required), QuizzStatusIcon.AUTH, QuizzNoticeTone.INFORMATION)
        else -> {
            val label = failure.reason.failureLabel()
            if (label != null) {
                PlayNotice(stringResource(label), QuizzStatusIcon.ROOM)
            } else {
                val context = ErrorOperationContext(OperationKind.USER_COMMAND, true, RetrySafety.SAFE_TO_REPEAT)
                val model = errorUiModel(DefaultErrorDecisionPolicy().decide(failure.cause, context))
                PlayNotice(stringResource(R.string.play_join_failed), model.icon, message = model.title)
            }
        }
    }
}

@Composable
private fun PlayNotice(
    text: String,
    icon: QuizzStatusIcon,
    tone: QuizzNoticeTone = QuizzNoticeTone.ERROR,
    message: String? = null,
) {
    QuizzNoticeBanner(
        Modifier.testTag(MobilePlayTags.VALIDATION),
        tone = tone,
        icon = icon,
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall)
        message?.let {
            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun RoomFailureReason?.failureLabel(): Int? = when (this) {
    RoomFailureReason.NOT_FOUND -> R.string.play_room_not_found
    RoomFailureReason.FULL -> R.string.play_room_full
    RoomFailureReason.EXPIRED, RoomFailureReason.CLOSED -> R.string.play_room_closed
    RoomFailureReason.RATE_LIMITED -> R.string.play_rate_limited
    RoomFailureReason.ALREADY_IN_ROOM -> R.string.play_existing_room
    RoomFailureReason.ACCOUNT_CHANGED -> R.string.play_account_changed
    else -> null
}
