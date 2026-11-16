package com.quizzgame.feature.feed.tv.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.feature.feed.tv.data.dto.FeedDto
import com.quizzgame.feature.feed.tv.data.dto.section.RailItemsDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

internal interface TvFeedApi {
    @GET("api/tv/feed")
    suspend fun load(
        @Tag policy: RequestPolicy,
        @Query("limit") limit: Int = SECTION_LIMIT,
        @Query("railLimit") railLimit: Int = PREVIEW_LIMIT,
        @Query("cursor") cursor: String? = null,
    ): FeedDto

    @GET("api/tv/feed/sections/{sectionId}/items")
    suspend fun items(
        @Path("sectionId") sectionId: String,
        @Query("cursor") cursor: String,
        @Tag policy: RequestPolicy,
        @Query("limit") limit: Int = ITEM_LIMIT,
    ): RailItemsDto

    companion object {
        const val SECTION_LIMIT = 6
        const val PREVIEW_LIMIT = 8
        const val ITEM_LIMIT = 12
    }
}
