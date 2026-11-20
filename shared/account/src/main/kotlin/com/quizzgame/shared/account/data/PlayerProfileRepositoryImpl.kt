package com.quizzgame.shared.account.data

import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.classification.classifyApiUnavailable
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.result.NetworkFailure
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.shared.account.domain.entity.PlayerProfile
import com.quizzgame.shared.account.domain.entity.SavePlayerNicknameResult
import com.quizzgame.shared.account.domain.repository.PlayerProfileRepository
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PlayerProfileRepositoryImpl @Inject constructor(
    private val remote: AccountRemoteDataSource,
    private val session: SessionStateReader,
) : PlayerProfileRepository {
    private val mutations = Mutex()

    override suspend fun load(): PlayerProfile = when (val owner = session.current()) {
        SessionState.Guest -> PlayerProfile.Guest
        is SessionState.Authenticated -> {
            val response = remote.loadProfile(owner.sessionGeneration)
            val user = ((response as? ApiCallResult.Executed)?.result as? NetworkResult.Success)?.value
            if (session.current() == owner) {
                PlayerProfile.SignedIn(owner.sessionGeneration, user)
            } else {
                PlayerProfile.Unavailable
            }
        }
        else -> PlayerProfile.Unavailable
    }

    override suspend fun save(
        userId: UUID,
        generation: Long,
        nickname: String,
    ): SavePlayerNicknameResult = mutations.withLock {
        val owner = SessionState.Authenticated(generation)
        if (session.current() != owner) return@withLock SavePlayerNicknameResult.AccountChanged
        val result = remote.updateNickname(nickname, generation)
        if (session.current() != owner) return@withLock SavePlayerNicknameResult.AccountChanged
        when (result) {
            is ApiCallResult.Executed -> when (val response = result.result) {
                is NetworkResult.Success -> confirmed(response.value, userId, nickname)
                is NetworkResult.Failure -> response.reason.toNicknameFailure()
            }
            is ApiCallResult.Unavailable -> SavePlayerNicknameResult.Failed(classifyApiUnavailable(result.reason))
            ApiCallResult.Closed -> SavePlayerNicknameResult.Failed(AppFailure.OperationInterrupted)
        }
    }

    private fun confirmed(
        user: CurrentUser,
        userId: UUID,
        nickname: String,
    ): SavePlayerNicknameResult = when {
        user.id != userId || user.displayName != nickname ->
            SavePlayerNicknameResult.Failed(AppFailure.InvalidServerData)
        else -> SavePlayerNicknameResult.Saved(user)
    }
}

private fun NetworkFailure.toNicknameFailure(): SavePlayerNicknameResult =
    if (this is NetworkFailure.Http && status == HTTP_UNPROCESSABLE && backend?.reasonCode == INVALID_NICKNAME) {
        SavePlayerNicknameResult.InvalidName
    } else {
        SavePlayerNicknameResult.Failed(DefaultErrorClassifier().fromNetwork(this))
    }

private const val HTTP_UNPROCESSABLE = 422
private const val INVALID_NICKNAME = 33001
