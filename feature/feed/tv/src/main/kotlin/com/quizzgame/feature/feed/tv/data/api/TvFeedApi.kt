package com.quizzgame.feature.feed.tv.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.feature.feed.tv.data.dto.FeedDto
import retrofit2.http.GET
import retrofit2.http.Tag

internal interface TvFeedApi {
    @GET("api/tv/feed")
    suspend fun load(@Tag policy: RequestPolicy): FeedDto
}
