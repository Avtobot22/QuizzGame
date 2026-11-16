package com.quizzgame.feature.feed.tv.domain

import androidx.paging.PagingData
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

internal interface FeedRepository {
    fun load(scope: CoroutineScope): Flow<PagingData<QuizRail>>
    fun loadItems(rail: QuizRail): Flow<PagingData<QuizCard>>
}
