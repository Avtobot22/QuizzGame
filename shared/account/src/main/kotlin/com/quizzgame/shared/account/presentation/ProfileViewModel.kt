package com.quizzgame.shared.account.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.account.domain.entity.PlayerNickname
import com.quizzgame.shared.account.domain.entity.PlayerProfile
import com.quizzgame.shared.account.domain.entity.SavePlayerNicknameResult
import com.quizzgame.shared.account.domain.usecase.GeneratePlayerNicknameUseCase
import com.quizzgame.shared.account.domain.usecase.LoadPlayerProfileUseCase
import com.quizzgame.shared.account.domain.usecase.SavePlayerNicknameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val loadPlayerProfileUseCase: LoadPlayerProfileUseCase,
    private val savePlayerNicknameUseCase: SavePlayerNicknameUseCase,
    private val generatePlayerNicknameUseCase: GeneratePlayerNicknameUseCase,
) : BaseViewModel<ProfileState, ProfileIntent, Nothing>() {
    private var request: Job? = null
    private var appeared = false

    init { load() }

    override fun createInitialState(): ProfileState = ProfileState.Loading

    override fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Visible -> refreshOnReturn()
            ProfileIntent.Retry -> load()
            ProfileIntent.EditNickname -> openEditor()
            ProfileIntent.CancelNickname -> editDraft { null }
            ProfileIntent.SuggestNickname -> suggestNickname()
            is ProfileIntent.ChangeNickname -> editDraft { it.copy(value = intent.value, issue = null) }
            ProfileIntent.SaveNickname -> saveNickname()
        }
    }

    private fun refreshOnReturn() {
        if (appeared) load()
        appeared = true
    }

    private fun load() {
        val ready = state.value as? ProfileState.Ready
        if (request?.isActive == true || ready?.editor != null) return
        updateReady { it.copy(refreshing = true, editor = null) }
        request = viewModelScope.launch {
            val profile = loadPlayerProfileUseCase()
            updateState { ProfileState.Ready(profile) }
        }
    }

    private fun openEditor() {
        val ready = state.value as? ProfileState.Ready ?: return
        val user = (ready.profile as? PlayerProfile.SignedIn)?.user
        if (user == null || ready.refreshing || ready.editor != null) return
        val initial = user.displayName?.let(PlayerNickname::normalize) ?: generatePlayerNicknameUseCase()
        updateReady { it.copy(editor = NicknameEditorState(initial)) }
    }

    private fun suggestNickname() {
        val suggested = generatePlayerNicknameUseCase()
        editDraft { it.copy(value = suggested, issue = null) }
    }

    private fun editDraft(transform: (NicknameEditorState) -> NicknameEditorState?) = updateReady { ready ->
        val editor = ready.editor
        if (editor != null && !editor.saving) ready.copy(editor = transform(editor)) else ready
    }

    private fun saveNickname() {
        val ready = state.value as? ProfileState.Ready
        val owner = ready?.profile as? PlayerProfile.SignedIn ?: return
        val user = owner.user
        val editor = ready.editor
        if (user == null || editor == null || request?.isActive == true) return
        updateReady { it.copy(editor = editor.copy(saving = true, issue = null)) }
        request = viewModelScope.launch {
            val outcome = savePlayerNicknameUseCase(user.id, owner.generation, editor.value)
            updateReady {
                when (outcome) {
                    is SavePlayerNicknameResult.Saved -> it.copy(
                        profile = owner.copy(user = outcome.user),
                        editor = null,
                    )
                    else -> it.copy(editor = editor.copy(issue = outcome.editorIssue()))
                }
            }
        }
    }

    private fun updateReady(transform: (ProfileState.Ready) -> ProfileState.Ready) =
        updateState { if (it is ProfileState.Ready) transform(it) else it }
}

private fun SavePlayerNicknameResult.editorIssue(): NicknameEditorState.Issue = when (this) {
    SavePlayerNicknameResult.InvalidName -> NicknameEditorState.Issue.INVALID_NAME
    SavePlayerNicknameResult.AccountChanged -> NicknameEditorState.Issue.ACCOUNT_CHANGED
    is SavePlayerNicknameResult.Failed -> NicknameEditorState.Issue.SAVE_FAILURE
    is SavePlayerNicknameResult.Saved -> error("Saved nickname has no editor issue")
}
