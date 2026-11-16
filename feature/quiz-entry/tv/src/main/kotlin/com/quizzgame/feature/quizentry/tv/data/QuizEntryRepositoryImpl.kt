package com.quizzgame.feature.quizentry.tv.data

import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.classification.classifyApiUnavailable
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.feature.quizentry.tv.data.api.TvQuizEntryApi
import com.quizzgame.feature.quizentry.tv.data.mapper.toDomain
import com.quizzgame.feature.quizentry.tv.domain.QuizEntryRepository
import com.quizzgame.feature.quizentry.tv.domain.entity.QuizEntryResult
import com.quizzgame.shared.catalog.data.mapper.requireCatalog
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class QuizEntryRepositoryImpl @Inject constructor(
    private val executor: ApiCallExecutor,
    private val session: SessionStateReader,
    private val dispatchers: DispatcherProvider,
) : QuizEntryRepository {
    override suspend fun load(entryId: String): QuizEntryResult {
        val generation = session.current()
        val response = executor.call(TvQuizEntryApi::class.java) {
            val dto = load(entryId, RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE))
            requireCatalog(dto.entryId == entryId)
            withContext(dispatchers.default) { dto.toDomain() }
        }
        if (session.current() != generation) return QuizEntryResult.Failed(AppFailure.OperationInterrupted)
        return when (response) {
            is ApiCallResult.Executed -> when (val result = response.result) {
                is NetworkResult.Success -> QuizEntryResult.Loaded(result.value)
                is NetworkResult.Failure -> QuizEntryResult.Failed(DefaultErrorClassifier().fromNetwork(result.reason))
            }
            is ApiCallResult.Unavailable -> QuizEntryResult.Failed(classifyApiUnavailable(response.reason))
            ApiCallResult.Closed -> QuizEntryResult.Failed(AppFailure.OperationInterrupted)
        }
    }
}
