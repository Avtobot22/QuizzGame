package com.quizzgame.shared.quizentry.data

import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.classification.classifyApiUnavailable
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.shared.catalog.data.mapper.catalogId
import com.quizzgame.shared.catalog.data.mapper.requireCatalog
import com.quizzgame.shared.catalog.data.mapper.toDomain
import com.quizzgame.shared.quizentry.domain.QuizEntryRepository
import com.quizzgame.shared.quizentry.domain.entity.QuizEntryResult
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import javax.inject.Inject

internal class QuizEntryRepositoryImpl @Inject constructor(
    private val remote: QuizEntryRemoteDataSource,
    private val session: SessionStateReader,
    private val dispatchers: DispatcherProvider,
) : QuizEntryRepository {
    override suspend fun load(entryId: String): QuizEntryResult {
        val generation = session.current()
        val result = try {
            catalogId(entryId)
            when (val response = remote.load(entryId)) {
                is ApiCallResult.Executed -> when (val value = response.result) {
                    is NetworkResult.Success -> withContext(dispatchers.default) {
                        requireCatalog(value.value.entryId == entryId)
                        QuizEntryResult.Loaded(value.value.toDomain())
                    }
                    is NetworkResult.Failure ->
                        QuizEntryResult.Failed(DefaultErrorClassifier().fromNetwork(value.reason))
                }
                is ApiCallResult.Unavailable -> QuizEntryResult.Failed(classifyApiUnavailable(response.reason))
                ApiCallResult.Closed -> QuizEntryResult.Failed(AppFailure.OperationInterrupted)
            }
        } catch (_: SerializationException) {
            QuizEntryResult.Failed(AppFailure.InvalidServerData)
        }
        return if (session.current() == generation) result else QuizEntryResult.Failed(AppFailure.OperationInterrupted)
    }
}
