package com.quizzgame.shared.feed.domain

import androidx.paging.PagingData
import com.quizzgame.shared.feed.domain.entity.FeedSection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadFeedUseCase @Inject constructor(private val repository: FeedRepository) {
    operator fun invoke(): Flow<PagingData<FeedSection>> = repository.getFeed()
}
