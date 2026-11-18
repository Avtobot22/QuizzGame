package com.quizzgame.shared.quizentry.data

import com.quizzgame.core.coroutines.DefaultDispatcherProvider
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.shared.catalog.data.dto.entry.QuizEntryDto
import com.quizzgame.shared.quizentry.domain.entity.QuizEntryResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizEntryRepositoryTest {
    private fun dto(): QuizEntryDto = createNetworkJson().decodeFromString(
        checkNotNull(javaClass.getResource("/mobile-quiz-entry.json")).readText(),
    )

    @Test
    fun sharedTvContractKeepsStatsAndRejectsMismatchedIdentifier() = runTest {
        val dto = dto()
        val repository = QuizEntryRepositoryImpl(
            { ApiCallResult.Executed(NetworkResult.Success(dto)) },
            { SessionState.Guest },
            DefaultDispatcherProvider(),
        )
        val result = repository.load(dto.entryId) as QuizEntryResult.Loaded
        assertEquals(1200, result.content.durationSeconds)
        assertEquals(30, result.content.questionCount)
        assertEquals(
            QuizEntryResult.Failed(AppFailure.InvalidServerData),
            repository.load("ebc9675e-2cd9-4ed7-a4b4-1d279d2ed003"),
        )
    }

    @Test
    fun lateAccountResponseIsDiscardedAndCancellationPropagates() = runTest {
        val gate = CompletableDeferred<Unit>()
        var owner: SessionState = SessionState.Authenticated(1)
        val dto = dto()
        val repository = QuizEntryRepositoryImpl(
            {
                gate.await()
                ApiCallResult.Executed(NetworkResult.Success(dto))
            },
            SessionStateReader { owner },
            DefaultDispatcherProvider(),
        )
        val late = async { repository.load(dto.entryId) }
        runCurrent()
        owner = SessionState.Authenticated(2)
        gate.complete(Unit)
        assertEquals(QuizEntryResult.Failed(AppFailure.OperationInterrupted), late.await())
        val cancelled = QuizEntryRepositoryImpl({ awaitCancellation() }, { owner }, DefaultDispatcherProvider())
        val job = async { cancelled.load(dto.entryId) }
        runCurrent()
        job.cancel()
        assertTrue(job.isCancelled)
    }
}
