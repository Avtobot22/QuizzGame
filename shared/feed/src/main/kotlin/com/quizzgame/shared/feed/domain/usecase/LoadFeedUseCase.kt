package com.quizzgame.shared.feed.domain.usecase

import androidx.paging.PagingData
import com.quizzgame.shared.feed.domain.entity.section.FeedSection
import com.quizzgame.shared.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadFeedUseCase @Inject constructor(private val repository: FeedRepository) {
    operator fun invoke(): Flow<PagingData<FeedSection>> = repository.getFeed()
}
