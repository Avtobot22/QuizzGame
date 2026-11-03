package com.quizzgame.core.navigation

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationReducerTest {
    @Test
    fun duplicatePushIsIgnoredAndBackNeverRemovesTheRoot() {
        val root = listOf(SplashRoute)
        assertEquals(root, reduceNavigation(root, NavAction.Pop))
        val signIn = reduceNavigation(root, NavAction.Push(SignInRoute))
        assertEquals(signIn, reduceNavigation(signIn, NavAction.Push(SignInRoute)))
        assertEquals(root, reduceNavigation(signIn, NavAction.Pop))
    }

    @Test
    fun rootResetDropsAllPreviousHistory() {
        assertEquals(
            listOf(SignInRoute),
            reduceNavigation(listOf(SplashRoute, SignInRoute, SplashRoute), NavAction.ResetTo(SignInRoute)),
        )
    }

    @Test
    fun savedRoutesAreUntrustedAfterAProcessChange() {
        assertTrue(mayRestoreNavigation("current", "current"))
        assertFalse(mayRestoreNavigation("old", "current"))
        assertFalse(mayRestoreNavigation(null, "current"))
    }

    @Test
    fun sharedRootKeysHaveGeneratedSerializers() {
        assertEquals(SplashRoute, Json.decodeFromString<SplashRoute>(Json.encodeToString(SplashRoute)))
        assertEquals(SignInRoute, Json.decodeFromString<SignInRoute>(Json.encodeToString(SignInRoute)))
    }
}
