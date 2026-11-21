package com.quizzgame.feature.play.mobile.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.mobile.components.QuizzMobileErrorInline
import com.quizzgame.feature.play.mobile.R
import com.quizzgame.feature.play.mobile.presentation.MobilePlayIntent
import com.quizzgame.feature.play.mobile.presentation.MobilePlayState
import com.quizzgame.feature.play.mobile.presentation.MobilePlayValidation
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason

@Composable
internal fun MobilePlayFeedback(
    state: MobilePlayState,
    onIntent: (MobilePlayIntent) -> Unit,
    onSignIn: () -> Unit,
) {
    val validation = state.validation.validationLabel()
    validation?.let { MobilePlayValidationText(stringResource(it)) }
    if (state.retryAfter > 0) {
        MobilePlayValidationText(stringResource(R.string.play_wait_to_retry, state.retryAfter))
        return
    }
    val failure = state.failure ?: return
    val label = failure.reason.failureLabel()
    if (label != null) {
        MobilePlayValidationText(stringResource(label))
    } else {
        val context = ErrorOperationContext(OperationKind.USER_COMMAND, true, RetrySafety.SAFE_TO_REPEAT)
        val spec = DefaultErrorDecisionPolicy().decide(failure.cause, context)
        val actions = spec.actions.filter { it == ErrorActionKind.RETRY }
        QuizzMobileErrorInline(spec.copy(surface = ErrorSurface.INLINE, actions = actions), {
            onIntent(MobilePlayIntent.Connect)
        })
    }
    if (failure.reason == RoomFailureReason.PARTICIPANT_AUTH_REQUIRED && state.account == null) {
        QuizzButton(onSignIn, style = QuizzButtonStyle.TONAL) { Text(stringResource(R.string.play_sign_in)) }
    }
}

private fun MobilePlayValidation?.validationLabel(): Int? = when (this) {
    MobilePlayValidation.CODE -> R.string.play_invalid_code
    MobilePlayValidation.NICKNAME -> R.string.play_invalid_nickname
    MobilePlayValidation.QR_INVALID -> R.string.play_invalid_qr
    MobilePlayValidation.CAMERA_DENIED -> R.string.play_permission_denied
    MobilePlayValidation.CAMERA_UNAVAILABLE -> R.string.play_camera_unavailable
    null -> null
}

private fun RoomFailureReason?.failureLabel(): Int? = when (this) {
    RoomFailureReason.PARTICIPANT_AUTH_REQUIRED -> R.string.play_auth_required
    RoomFailureReason.NOT_FOUND -> R.string.play_room_not_found
    RoomFailureReason.FULL -> R.string.play_room_full
    RoomFailureReason.NAME_TAKEN -> R.string.play_nickname_taken
    RoomFailureReason.EXPIRED, RoomFailureReason.CLOSED -> R.string.play_room_closed
    RoomFailureReason.RATE_LIMITED -> R.string.play_rate_limited
    RoomFailureReason.ALREADY_IN_ROOM -> R.string.play_existing_room
    RoomFailureReason.ACCOUNT_CHANGED -> R.string.play_account_changed
    else -> null
}
