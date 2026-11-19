package com.quizzgame.shared.feed.domain.entity

import com.quizzgame.core.error.failure.AppFailure

class FeedPagingException(val failure: AppFailure) : Exception("Catalog page unavailable")
