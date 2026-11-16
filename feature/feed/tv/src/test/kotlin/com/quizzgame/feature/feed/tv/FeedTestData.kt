package com.quizzgame.feature.feed.tv

import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.feed.tv.data.FeedRemoteDataSource
import com.quizzgame.feature.feed.tv.data.dto.FeedDto
import com.quizzgame.feature.feed.tv.data.dto.RailItemsDto
import com.quizzgame.feature.feed.tv.data.mapper.FeedMapper
import com.quizzgame.feature.feed.tv.domain.entity.Feed
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import com.quizzgame.feature.feed.tv.domain.entity.RailItems

internal object FeedTestData {
    private val json = createNetworkJson()
    private val mapper = FeedMapper()
    fun text(name: String): String = checkNotNull(javaClass.getResource("/$name")).readText()
    fun feed(name: String = "feed-page-1.json"): Feed = mapper.map(json.decodeFromString<FeedDto>(text(name)))
    fun items(): RailItems {
        val dto = json.decodeFromString<RailItemsDto>(text("feed-rail-a-page-2.json"))
        return mapper.mapItems(dto, feed().rails.first().id)
    }
}

internal class FakeFeedRemoteDataSource : FeedRemoteDataSource {
    val feedCalls = mutableListOf<String?>()
    val itemCalls = mutableListOf<Pair<String, String>>()
    var feedReply: suspend (String?) -> FeedResult<Feed> = { cursor ->
        FeedResult.Loaded(FeedTestData.feed(if (cursor == null) "feed-page-1.json" else "feed-page-2.json"))
    }
    var itemsReply: suspend (String) -> FeedResult<RailItems> = { FeedResult.Loaded(FeedTestData.items()) }

    override suspend fun load(cursor: String?, owner: SessionState?): FeedResult<Feed> {
        feedCalls += cursor
        return feedReply(cursor)
    }

    override suspend fun loadItems(sectionId: String, cursor: String, owner: SessionState): FeedResult<RailItems> {
        itemCalls += sectionId to cursor
        return itemsReply(cursor)
    }
}
