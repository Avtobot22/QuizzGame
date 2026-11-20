package com.quizzgame.feature.account.mobile.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.feature.account.mobile.R
import com.quizzgame.feature.account.mobile.ui.tags.MobileProfileTags
import com.quizzgame.shared.account.domain.entity.PlayerNickname
import com.quizzgame.shared.account.presentation.NicknameEditorState
import com.quizzgame.shared.account.presentation.ProfileIntent

@Composable
internal fun MobileNicknameEditor(state: NicknameEditorState, onIntent: (ProfileIntent) -> Unit) {
    val canSave = !state.saving && state.value.isNotBlank() && state.issue != NicknameEditorState.Issue.ACCOUNT_CHANGED
    val save = { onIntent(ProfileIntent.SaveNickname) }
    val cancel = { onIntent(ProfileIntent.CancelNickname) }
    AlertDialog(
        modifier = Modifier.testTag(MobileProfileTags.NICKNAME_EDITOR),
        onDismissRequest = { if (!state.saving) cancel() },
        title = { Text(stringResource(R.string.profile_nickname_label)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.profile_nickname_description))
                OutlinedTextField(
                    value = state.value,
                    onValueChange = { onIntent(ProfileIntent.ChangeNickname(it)) },
                    modifier = Modifier.fillMaxWidth().testTag(MobileProfileTags.NICKNAME_FIELD),
                    label = { Text(stringResource(R.string.profile_nickname_label)) },
                    singleLine = true,
                    enabled = !state.saving,
                    isError = state.issue == NicknameEditorState.Issue.INVALID_NAME,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (canSave) save() }),
                )
                NicknameEditorHint(state.issue)
                QuizzButton(
                    { onIntent(ProfileIntent.SuggestNickname) },
                    style = QuizzButtonStyle.TEXT,
                    enabled = !state.saving,
                ) { Text(stringResource(R.string.profile_nickname_suggest)) }
            }
        },
        confirmButton = {
            QuizzButton(save, Modifier.testTag(MobileProfileTags.SAVE_NICKNAME), enabled = canSave) {
                val label = if (state.saving) R.string.profile_nickname_saving else R.string.profile_nickname_save
                Text(stringResource(label))
            }
        },
        dismissButton = {
            QuizzButton(cancel, style = QuizzButtonStyle.TEXT, enabled = !state.saving) {
                Text(stringResource(R.string.profile_nickname_cancel))
            }
        },
    )
}

@Composable
private fun NicknameEditorHint(issue: NicknameEditorState.Issue?) {
    val message = when (issue) {
        NicknameEditorState.Issue.INVALID_NAME    -> stringResource(R.string.profile_nickname_invalid)
        NicknameEditorState.Issue.ACCOUNT_CHANGED -> stringResource(R.string.profile_nickname_account_changed)
        NicknameEditorState.Issue.SAVE_FAILURE    -> stringResource(R.string.profile_nickname_save_failed)
        null                                     -> stringResource(
            R.string.profile_nickname_hint,
            PlayerNickname.MAX_LENGTH,
        )
    }
    val colors = MaterialTheme.colorScheme
    Text(
        message,
        Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        color = if (issue == null) colors.onSurfaceVariant else colors.error,
        style = MaterialTheme.typography.bodySmall,
    )
}
