package com.quizzgame.feature.account.tv.ui.editor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvActionLabel
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.feature.account.tv.R
import com.quizzgame.feature.account.tv.ui.tags.TvProfileTags
import com.quizzgame.shared.account.domain.entity.PlayerNickname
import com.quizzgame.shared.account.presentation.NicknameEditorState
import com.quizzgame.shared.account.presentation.ProfileIntent

@Composable
internal fun TvNicknameEditor(
    state: NicknameEditorState,
    returnFocus: FocusRequester,
    onIntent: (ProfileIntent) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = { if (!state.saving) onIntent(ProfileIntent.CancelNickname) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.padding(horizontal = 24.dp).widthIn(max = 570.dp).fillMaxWidth()
                .testTag(TvProfileTags.NICKNAME_EDITOR),
            shape = MaterialTheme.shapes.extraLarge,
            colors = SurfaceDefaults.colors(containerColor = colors.surface, contentColor = colors.onSurface),
        ) {
            TvNicknameForm(state, onIntent)
        }
    }
    DisposableEffect(returnFocus) {
        onDispose {
            try {
                returnFocus.requestFocus()
            } catch (_: IllegalStateException) {
                // Если экран профиля закрывается вместе с редактором, цели фокуса уже нет.
            }
        }
    }
}

@Composable
private fun TvNicknameForm(state: NicknameEditorState, onIntent: (ProfileIntent) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val keyboard = LocalSoftwareKeyboardController.current
    val canSave = !state.saving && state.value.isNotBlank() && state.issue != NicknameEditorState.Issue.ACCOUNT_CHANGED
    val save = {
        keyboard?.hide()
        onIntent(ProfileIntent.SaveNickname)
    }
    Column(
        Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.profile_nickname_label), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.profile_nickname_description),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        TvNicknameInput(state, { onIntent(ProfileIntent.ChangeNickname(it)) }) { if (canSave) save() }
        val issue = when (state.issue) {
            NicknameEditorState.Issue.INVALID_NAME -> R.string.profile_nickname_invalid
            NicknameEditorState.Issue.ACCOUNT_CHANGED -> R.string.profile_nickname_account_changed
            NicknameEditorState.Issue.SAVE_FAILURE -> R.string.profile_nickname_save_failed
            null -> null
        }
        Text(
            if (issue == null) {
                stringResource(R.string.profile_nickname_hint, PlayerNickname.MAX_LENGTH)
            } else {
                stringResource(issue)
            },
            Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.labelMedium,
            color = if (issue == null) colors.onSurfaceVariant else colors.error,
        )
        TvNicknameActions(state, canSave, save, onIntent)
    }
}

@Composable
private fun TvNicknameInput(
    state: NicknameEditorState,
    onChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var focused by remember { mutableStateOf(false) }
    val outline = when {
        focused -> colors.onSurface
        state.issue == NicknameEditorState.Issue.INVALID_NAME -> colors.error
        else -> colors.border
    }
    val label = stringResource(R.string.profile_nickname_label)
    BasicTextField(
        value = state.value,
        onValueChange = onChange,
        enabled = !state.saving,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier.fillMaxWidth()
            .testTag(TvProfileTags.NICKNAME_FIELD)
            .onFocusChanged { focused = it.isFocused }
            .border(if (focused) 2.dp else 1.dp, outline, MaterialTheme.shapes.medium)
            .semantics { contentDescription = label }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    )
}

@Composable
private fun TvNicknameActions(
    state: NicknameEditorState,
    canSave: Boolean,
    onSave: () -> Unit,
    onIntent: (ProfileIntent) -> Unit,
) {
    val initialFocus = remember { FocusRequester() }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        QuizzTvButton(
            onSave,
            Modifier.focusRequester(initialFocus).testTag(TvProfileTags.SAVE_NICKNAME),
            enabled = canSave,
        ) {
            val label = if (state.saving) R.string.profile_nickname_saving else R.string.profile_nickname_save
            QuizzTvActionLabel(stringResource(label))
        }
        QuizzTvButton(
            { onIntent(ProfileIntent.SuggestNickname) },
            style = QuizzTvButtonStyle.SECONDARY,
            enabled = !state.saving,
        ) { QuizzTvActionLabel(stringResource(R.string.profile_nickname_suggest)) }
        QuizzTvButton(
            { onIntent(ProfileIntent.CancelNickname) },
            style = QuizzTvButtonStyle.QUIET,
            enabled = !state.saving,
        ) { QuizzTvActionLabel(stringResource(R.string.profile_nickname_cancel)) }
    }
    LaunchedEffect(Unit) { if (canSave) initialFocus.requestFocus() }
}
