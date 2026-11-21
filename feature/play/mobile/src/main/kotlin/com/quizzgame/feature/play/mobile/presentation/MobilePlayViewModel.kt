package com.quizzgame.feature.play.mobile.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.account.domain.entity.PlayerProfile
import com.quizzgame.shared.account.domain.usecase.LoadPlayerProfileUseCase
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinInput
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinLink
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.repository.RoomAccountState
import com.quizzgame.shared.room.domain.usecase.JoinRoomUseCase
import com.quizzgame.shared.room.domain.usecase.ObserveActiveRoomUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
internal class MobilePlayViewModel @Inject constructor(
    private val saved: SavedStateHandle,
    private val joinRoomUseCase: JoinRoomUseCase,
    private val observeActiveRoomUseCase: ObserveActiveRoomUseCase,
    private val links: RoomJoinLink,
    private val accounts: RoomAccountState,
    private val loadPlayerProfileUseCase: LoadPlayerProfileUseCase,
) : BaseViewModel<MobilePlayState, MobilePlayIntent, Nothing>() {
    private var rateLimit: Job? = null

    init {
        viewModelScope.launch {
            observeActiveRoomUseCase().collect { result ->
                when (result) {
                    is RoomResult.Failed -> updateState { it.copy(failure = result.failure) }
                    is RoomResult.Success -> updateState { it.copy(activeRoom = result.value?.roomId) }
                }
            }
        }
    }

    override fun createInitialState() = MobilePlayState(
        code = saved["code"] ?: "",
        nickname = saved["nickname"] ?: "",
        identity = MobilePlayJoinIdentity.Checking,
    )

    override fun handleIntent(intent: MobilePlayIntent) {
        when (intent) {
            is MobilePlayIntent.EditCode -> editCode(intent.value)
            is MobilePlayIntent.EditNickname -> editNickname(intent.value)
            is MobilePlayIntent.RestoreDraft -> restoreDraft(intent)
            is MobilePlayIntent.Qr -> {
                val code = links.codeFrom(intent.text)
                if (code == null) {
                    updateState { it.copy(validation = MobilePlayValidation.QR_INVALID) }
                } else {
                    editCode(code)
                }
            }
            is MobilePlayIntent.CameraError -> updateState { it.copy(validation = intent.validation) }
            MobilePlayIntent.Connect -> connect()
            MobilePlayIntent.Resume -> updateState { it.copy(destination = it.activeRoom) }
            MobilePlayIntent.NavigationHandled -> updateState { it.copy(destination = null) }
        }
    }

    private fun restoreDraft(intent: MobilePlayIntent.RestoreDraft) {
        if (saved.get<Boolean>("draftApplied") == true) return
        editCode(intent.code)
        editNickname(intent.nickname)
        saved["draftApplied"] = true
    }

    private fun editCode(value: String) {
        if (state.value.joining) return
        val code = value.filter { it in '0'..'9' }.take(RoomJoinInput.MAX_CODE_LENGTH)
        saved["code"] = code
        updateState {
            it.copy(
                code = code,
                validation = null,
                failure = null,
                editProfileName = it.editProfileName && code == it.code,
            )
        }
    }

    private fun editNickname(value: String) {
        if (state.value.joining) return
        saved["nickname"] = value
        updateState { it.copy(nickname = value, validation = null, failure = null) }
    }

    suspend fun observeIdentity() {
        accounts.state().collectLatest(::updateIdentity)
    }

    private suspend fun updateIdentity(session: SessionState) {
        val account = session as? SessionState.Authenticated
        updateState {
            val previous = it.account?.takeIf { existing -> existing.generation == account?.sessionGeneration }
            val identity = when {
                previous != null -> previous.copy(refreshing = true)
                account != null || session == SessionState.Initializing -> MobilePlayJoinIdentity.Checking
                else -> MobilePlayJoinIdentity.Guest
            }
            it.copy(identity = identity, editProfileName = previous != null && it.editProfileName)
        }
        if (account == null) return

        val profile = loadPlayerProfileUseCase() as? PlayerProfile.SignedIn
        val name = profile?.user?.displayName?.let(RoomJoinInput::nickname)
        if (accounts.state().value == account) {
            updateState {
                it.copy(
                    identity = MobilePlayJoinIdentity.Account(account.sessionGeneration, name),
                    editProfileName = it.editProfileName && it.account?.profileName == name,
                )
            }
        }
    }

    private fun connect() {
        val draft = state.value
        val account = accounts.state().value as? SessionState.Authenticated
        val identityReady = !draft.checkingIdentity && draft.account?.generation == account?.sessionGeneration
        val canJoin = !draft.joining && draft.retryAfter == 0L && draft.activeRoom == null
        if (!identityReady || !canJoin) return
        val nickname = draft.profileName ?: RoomJoinInput.nickname(draft.nickname)
        val validation = when {
            !RoomJoinInput.validCode(draft.code) -> MobilePlayValidation.CODE
            nickname == null -> MobilePlayValidation.NICKNAME
            else -> null
        }
        if (validation != null) {
            updateState { it.copy(validation = validation) }
            return
        }
        acquire(draft, requireNotNull(nickname))
    }

    private fun acquire(draft: MobilePlayState, name: String) {
        val accountGeneration = draft.account?.generation
        updateState { it.copy(joining = true, failure = null, validation = null) }
        viewModelScope.launch {
            when (val result = joinRoomUseCase(draft.code, name, accountGeneration)) {
                is RoomResult.Success -> {
                    updateState { it.copy(joining = false, destination = result.value.roomId) }
                }
                is RoomResult.Failed -> {
                    updateState {
                        val canEditName = draft.profileName != null &&
                            result.failure.reason == RoomFailureReason.NAME_TAKEN &&
                            it.account?.generation == accountGeneration
                        it.copy(
                            joining = false,
                            failure = result.failure,
                            editProfileName = it.editProfileName || canEditName,
                        )
                    }
                    result.failure.retryAfterSeconds?.let(::waitBeforeJoin)
                }
            }
        }
    }

    private fun waitBeforeJoin(seconds: Long) {
        rateLimit?.cancel()
        rateLimit = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                updateState { it.copy(retryAfter = remaining) }
                delay(MILLIS_PER_SECOND.milliseconds)
                remaining--
            }
            updateState { it.copy(retryAfter = 0) }
        }
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
    }
}
