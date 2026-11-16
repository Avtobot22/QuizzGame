package com.quizzgame.feature.feed.tv.domain

import com.quizzgame.core.error.failure.AppFailure

internal class FeedPagingException(val failure: AppFailure) : Exception("Catalog page unavailable")
