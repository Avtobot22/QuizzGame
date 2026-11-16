package com.quizzgame.feature.feed.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.paging.compose.LazyPagingItems
import com.quizzgame.core.designsystem.tv.media.LocalTvMediaLoader
import com.quizzgame.core.flags.CatalogPrefetch
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun TvRailImagePrefetch(cards: LazyPagingItems<QuizCard>, settings: CatalogPrefetch) {
    if (!settings.enabled) return
    val images = LocalTvMediaLoader.current
    LaunchedEffect(cards, images, settings) {
        if (settings.enabled) {
            snapshotFlow {
                cards.itemSnapshotList.items.take(settings.depth).mapNotNull { (it.image as? MediaSpec.Remote)?.url }
            }.distinctUntilChanged().collectLatest { images.prefetch(it, settings.maxConcurrent) }
        }
    }
}
