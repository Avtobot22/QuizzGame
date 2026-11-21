package com.quizzgame.feature.play.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.feature.play.mobile.R
import com.quizzgame.feature.play.mobile.presentation.MobilePlayIntent
import com.quizzgame.feature.play.mobile.presentation.MobilePlayState
import com.quizzgame.feature.play.mobile.presentation.MobilePlayValidation
import com.quizzgame.feature.play.mobile.ui.tags.MobilePlayTags
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.core.ui.R as UiR
import com.quizzgame.shared.room.R as RoomR

@Composable
internal fun MobilePlayConnection(
    state: MobilePlayState,
    onIntent: (MobilePlayIntent) -> Unit,
    onScan: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (state.activeRoom != null) {
                QuizzButton(
                    { onIntent(MobilePlayIntent.Resume) },
                    Modifier.fillMaxWidth().testTag(MobilePlayTags.RESUME),
                ) {
                    Text(stringResource(RoomR.string.room_resume))
                }
            } else {
                MobilePlayFields(state, onIntent)
                MobilePlayFeedback(state)
                val canConnect = !state.joining && state.retryAfter == 0L && !state.checkingIdentity
                val needsSignIn = state.failure?.reason == RoomFailureReason.PARTICIPANT_AUTH_REQUIRED &&
                    state.account == null
                QuizzButton(
                    if (needsSignIn) onSignIn else ({ onIntent(MobilePlayIntent.Connect) }),
                    Modifier.fillMaxWidth().testTag(MobilePlayTags.CONNECT),
                    enabled = canConnect,
                ) {
                    val label = when {
                        state.joining -> R.string.play_joining
                        needsSignIn -> R.string.play_sign_in
                        else -> R.string.play_connect
                    }
                    Text(stringResource(label))
                }
                MobilePlayQrOption(onScan, enabled = !state.joining)
            }
        }
    }
}

@Composable
private fun MobilePlayFields(state: MobilePlayState, onIntent: (MobilePlayIntent) -> Unit) {
    val focus = LocalFocusManager.current
    val codeError = state.validation == MobilePlayValidation.CODE
    OutlinedTextField(
        state.code,
        { onIntent(MobilePlayIntent.EditCode(it)) },
        Modifier.fillMaxWidth().testTag(MobilePlayTags.CODE),
        label = { Text(stringResource(R.string.play_code)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (state.showNickname) ImeAction.Next else ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
        textStyle = MaterialTheme.typography.titleLarge,
        shape = MaterialTheme.shapes.medium,
        enabled = !state.joining,
        isError = codeError,
        supportingText = if (codeError) {
            { MobilePlayValidationText(stringResource(R.string.play_invalid_code)) }
        } else {
            null
        },
    )
    val profileName = state.profileName
    when {
        profileName != null -> MobilePlayProfileName(profileName)
        state.showNickname -> NicknameField(state, onIntent)
        else -> Text(
            stringResource(R.string.play_loading_profile),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun NicknameField(state: MobilePlayState, onIntent: (MobilePlayIntent) -> Unit) {
    val error = when {
        state.validation == MobilePlayValidation.NICKNAME -> R.string.play_invalid_nickname
        state.failure?.reason == RoomFailureReason.NAME_TAKEN -> R.string.play_nickname_taken
        else -> null
    }
    OutlinedTextField(
        state.nickname,
        { onIntent(MobilePlayIntent.EditNickname(it)) },
        Modifier.fillMaxWidth().testTag(MobilePlayTags.NICKNAME),
        label = { Text(stringResource(R.string.play_nickname)) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        enabled = !state.joining,
        isError = error != null,
        supportingText = error?.let { label -> { MobilePlayValidationText(stringResource(label)) } },
    )
}

@Composable
private fun MobilePlayProfileName(name: String) {
    val colors = MaterialTheme.colorScheme
    Surface(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer,
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            QuizzIconLabel(UiR.drawable.ic_quizz_profile, tint = LocalContentColor.current) {
                Text(
                    stringResource(R.string.play_signed_in_as, name),
                    modifier = Modifier.testTag(MobilePlayTags.PROFILE_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
internal fun MobilePlayValidationText(message: String) {
    Text(
        message,
        Modifier.testTag(MobilePlayTags.VALIDATION).semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun MobilePlayQrOption(onScan: () -> Unit, enabled: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), color = colors.outlineVariant)
        Text(
            stringResource(R.string.play_or),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        HorizontalDivider(Modifier.weight(1f), color = colors.outlineVariant)
    }
    QuizzButton(
        onScan,
        Modifier.fillMaxWidth().testTag(MobilePlayTags.SCAN),
        style = QuizzButtonStyle.OUTLINED,
        enabled = enabled,
    ) {
        QuizzIconLabel(UiR.drawable.ic_quizz_scan, tint = LocalContentColor.current) {
            Text(stringResource(R.string.play_scan))
        }
    }
}
