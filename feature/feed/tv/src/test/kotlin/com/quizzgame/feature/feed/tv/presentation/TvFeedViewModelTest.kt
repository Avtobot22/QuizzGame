package com.quizzgame.feature.feed.tv.presentation

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.flags.AppFeatureFlags
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.feature.feed.tv.data.dto.FeedDto
import com.quizzgame.feature.feed.tv.data.mapper.FeedMapper
import com.quizzgame.feature.feed.tv.domain.FeedRepository
import com.quizzgame.feature.feed.tv.domain.LoadFeedUseCase
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TvFeedViewModelTest {
    @Test
    fun concurrentRetryDoesNotDuplicateAndFailedRefreshRetainsContent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var calls = 0
            val reply = CompletableDeferred<FeedResult>()
            val repository = object : FeedRepository {
                override suspend fun load(): FeedResult {
                    calls++
                    return if (calls == 1) reply.await() else FeedResult.Failed(AppFailure.InvalidServerData)
                }
            }
            val model = TvFeedViewModel(LoadFeedUseCase(repository), flags)
            runCurrent()
            repeat(5) { model.onIntent(TvFeedIntent.Retry) }
            runCurrent()
            assertEquals(1, calls)
            reply.complete(FeedResult.Loaded(fixture()))
            runCurrent()
            val content = (model.state.value as TvFeedState.Content).value
            model.onIntent(TvFeedIntent.Refresh)
            runCurrent()
            val failedRefresh = model.state.value as TvFeedState.Content
            assertEquals(content, failedRefresh.value)
            assertNotNull(failedRefresh.error)
            assertEquals(ErrorSurface.BANNER, failedRefresh.error?.surface)
            val entryId = content.rails.first().cards.first().entryId
            model.onIntent(TvFeedIntent.Open(entryId))
            model.onIntent(TvFeedIntent.Open("invalid"))
            assertEquals(entryId, (model.state.value as TvFeedState.Content).destination)
            model.onIntent(TvFeedIntent.NavigationHandled)
            assertEquals(null, (model.state.value as TvFeedState.Content).destination)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun fixture(): com.quizzgame.feature.feed.tv.domain.entity.Feed {
        val json = createNetworkJson()
        val dto = json.decodeFromString<FeedDto>(checkNotNull(javaClass.getResource("/feed.json")).readText())
        return FeedMapper().map(dto)
    }

    private val flags = object : AppFeatureFlags { override fun guestAllowed(): Boolean = true }
}
