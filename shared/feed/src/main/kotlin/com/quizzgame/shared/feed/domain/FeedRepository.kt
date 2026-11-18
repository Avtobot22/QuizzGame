package com.quizzgame.shared.feed.domain

import androidx.paging.PagingData
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.feed.domain.entity.FeedSection
import com.quizzgame.shared.feed.domain.entity.QuizRail
import kotlinx.coroutines.flow.Flow

interface FeedRepository {

    fun getFeed(): Flow<PagingData<FeedSection>>
    fun loadItems(rail: QuizRail): Flow<PagingData<QuizCard>>
}
