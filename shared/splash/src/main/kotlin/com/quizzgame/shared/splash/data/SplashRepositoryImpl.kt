package com.quizzgame.shared.splash.data

import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.classification.classifyApiUnavailable
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.flags.AppFeatureFlags
import com.quizzgame.core.flags.FeatureFlagsController
import com.quizzgame.core.flags.FeatureFlagsLoadFailure
import com.quizzgame.core.flags.FeatureFlagsLoadResult
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.shared.splash.domain.SplashRepository
import javax.inject.Inject

internal class SplashRepositoryImpl @Inject constructor(
    private val remote: SplashRemoteDataSource,
    private val flags: AppFeatureFlags,
    private val controller: FeatureFlagsController,
    private val session: SessionStateReader,
    private val users: CurrentUserStore,
) : SplashRepository {
    override suspend fun currentSession(): SessionState = session.current()

    override suspend fun loadLaunch(): AppFailure? = remote.launch().failure()

    override suspend fun refreshFlags(): AppFailure? = when (val result = controller.refresh()) {
        FeatureFlagsLoadResult.Applied   -> null
        is FeatureFlagsLoadResult.Failed -> result.reason.appFailure()
    }

    override suspend fun loadUser(expectedGeneration: Long): AppFailure? {
        val response = remote.currentUser()
        val failure = response.failure()
        return when {
            failure != null -> failure
            session.current() != SessionState.Authenticated(expectedGeneration) -> AppFailure.OperationInterrupted
            else -> {
                val user = ((response as ApiCallResult.Executed).result as NetworkResult.Success).value
                if (users.replace(expectedGeneration, user)) null else AppFailure.OperationInterrupted
            }
        }
    }

    override fun guestAllowed(): Boolean = flags.guestAllowed()

    override fun clearUser() {
        users.clear()
    }
}

private fun ApiCallResult<*>.failure(): AppFailure? = when (this) {
    is ApiCallResult.Executed    -> (result as? NetworkResult.Failure)?.let {
        DefaultErrorClassifier().fromNetwork(it.reason)
    }

    is ApiCallResult.Unavailable -> classifyApiUnavailable(reason)
    ApiCallResult.Closed         -> AppFailure.OperationInterrupted
}

private fun FeatureFlagsLoadFailure.appFailure(): AppFailure = when (this) {
    is FeatureFlagsLoadFailure.Network    -> DefaultErrorClassifier().fromNetwork(failure)
    FeatureFlagsLoadFailure.InvalidRemote -> AppFailure.InvalidServerData
    FeatureFlagsLoadFailure.InvalidLocal  -> AppFailure.Storage(StorageKind.CORRUPT)
    FeatureFlagsLoadFailure.LocalRead     -> AppFailure.Storage(StorageKind.READ)
    is FeatureFlagsLoadFailure.Runtime    -> classifyApiUnavailable(reason)
    FeatureFlagsLoadFailure.Closed        -> AppFailure.OperationInterrupted
}
