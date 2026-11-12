package com.quizzgame.core.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationReducerTest {
    @Test
    fun duplicatePushIsIgnoredAndBackNeverRemovesTheRoot() {
        val root = listOf(First)
        assertEquals(root, reduceNavigation(root, NavAction.Pop))
        val pushed = reduceNavigation(root, NavAction.Push(Second))
        assertEquals(pushed, reduceNavigation(pushed, NavAction.Push(Second)))
        assertEquals(root, reduceNavigation(pushed, NavAction.Pop))
    }

    @Test
    fun rootResetDropsAllPreviousHistory() {
        assertEquals(
            listOf(Second),
            reduceNavigation(listOf(First, Second, First), NavAction.ResetTo(Second)),
        )
    }

    private data object First : NavKey
    private data object Second : NavKey
}
