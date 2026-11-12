package com.quizzgame.app.navigation

import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.request.ExternalNavigationParser
import com.quizzgame.core.navigation.request.NavigationRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExternalNavigationRouterTest {
    @Test
    fun unknownAndAmbiguousLinksAreRejected() {
        val request = NavigationRequest("request", TestKey, TestKey)
        assertNull(ExternalNavigationRouter(emptySet()).parse("unknown"))
        val first = ExternalNavigationParser { request }
        val second = ExternalNavigationParser { request.copy(id = "other") }
        assertNull(ExternalNavigationRouter(setOf(first, second)).parse("ambiguous"))
        assertEquals(request, ExternalNavigationRouter(setOf(first)).parse("known"))
    }

    private data object TestKey : NavKey
}
