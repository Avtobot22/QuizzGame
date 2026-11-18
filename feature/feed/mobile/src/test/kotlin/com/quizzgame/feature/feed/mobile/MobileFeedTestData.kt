package com.quizzgame.feature.feed.mobile

internal object MobileFeedTestData {
    fun text(name: String): String = checkNotNull(javaClass.getResource("/$name")).readText()
}
