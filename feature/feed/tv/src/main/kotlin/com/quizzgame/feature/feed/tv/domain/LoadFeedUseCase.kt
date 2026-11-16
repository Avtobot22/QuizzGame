package com.quizzgame.feature.feed.tv.domain

import androidx.paging.PagingData
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class LoadFeedUseCase @Inject constructor(private val repository: FeedRepository) {
    operator fun invoke(): Flow<PagingData<QuizRail>> = repository.getFeed()
}
