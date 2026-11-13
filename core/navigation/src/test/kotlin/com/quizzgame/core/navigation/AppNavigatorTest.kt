package com.quizzgame.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {
    @Test
    fun navigateIsSingleInstanceAndReusesAnExistingDestination() {
        val navigator = navigator()
        navigator.navigate(Key.DETAIL)
        navigator.navigate(Key.DETAIL)
        navigator.navigate(Key.OTHER)
        navigator.navigate(Key.DETAIL)
        assertEquals(listOf(Key.HOME, Key.DETAIL), navigator.state.snapshot.activeEntries)
    }

    @Test
    fun replaceChangesOnlyTheTopAndCollapsesAnExistingTarget() {
        val navigator = navigator()
        navigator.navigate(Key.DETAIL)
        navigator.replace(Key.OTHER)
        assertEquals(listOf(Key.HOME, Key.OTHER), navigator.state.snapshot.activeEntries)
        navigator.replace(Key.HOME)
        assertEquals(listOf(Key.HOME), navigator.state.snapshot.activeEntries)
    }

    @Test(expected = IllegalArgumentException::class)
    fun replaceCannotRemoveASectionRoot() { navigator().replace(Key.OTHER) }

    @Test
    fun popUpToIsSafeForMissingAndInclusiveRootTargets() {
        val navigator = navigator()
        navigator.navigate(Key.DETAIL)
        navigator.navigate(Key.OTHER)
        navigator.popUpTo(Key.AUTH)
        assertEquals(3, navigator.state.snapshot.activeEntries.size)
        navigator.popUpTo(Key.DETAIL)
        assertEquals(listOf(Key.HOME, Key.DETAIL), navigator.state.snapshot.activeEntries)
        navigator.popUpTo(Key.DETAIL, inclusive = true)
        assertEquals(listOf(Key.HOME), navigator.state.snapshot.activeEntries)
        navigator.popUpTo(Key.HOME, inclusive = true)
        assertFalse(navigator.back())
    }

    @Test
    fun switchingSectionsPreservesBothNestedHistoriesAndBackAffectsOnlyTheActiveSection() {
        val navigator = navigator()
        navigator.navigate(Key.DETAIL)
        navigator.switchTopLevel(Key.LIBRARY)
        navigator.navigate(Key.OTHER)
        navigator.switchTopLevel(Key.HOME)
        assertEquals(listOf(Key.HOME, Key.DETAIL), navigator.state.snapshot.activeEntries)
        assertTrue(navigator.back())
        navigator.switchTopLevel(Key.LIBRARY)
        assertEquals(listOf(Key.LIBRARY, Key.OTHER), navigator.state.snapshot.activeEntries)
        assertTrue(navigator.back())
        assertEquals(Key.LIBRARY, navigator.state.snapshot.active)
        assertTrue(navigator.back())
        assertEquals(Key.HOME, navigator.state.snapshot.active)
        assertFalse(navigator.back())
    }

    @Test
    fun backFromSecondaryRootRestoresThePrimaryNestedHistory() {
        val navigator = navigator()
        navigator.navigate(Key.DETAIL)
        navigator.switchTopLevel(Key.LIBRARY)
        navigator.back()
        assertEquals(listOf(Key.HOME, Key.DETAIL), navigator.state.snapshot.activeEntries)
    }

    @Test
    fun reselectionPopsToRootBeforeInvokingTheRootCallback() {
        val navigator = navigator()
        var reselections = 0
        navigator.navigate(Key.DETAIL)
        navigator.switchTopLevel(Key.HOME) { reselections++ }
        assertEquals(0, reselections)
        assertEquals(listOf(Key.HOME), navigator.state.snapshot.activeEntries)
        navigator.switchTopLevel(Key.HOME) { reselections++ }
        assertEquals(1, reselections)
    }

    @Test
    fun applicationFlowPreservesSectionsAndCannotPopItsLastRoot() {
        val navigator = navigator()
        navigator.navigate(Key.DETAIL)
        navigator.showFlow(Key.AUTH)
        assertFalse(navigator.back())
        navigator.navigate(Key.OTHER)
        assertTrue(navigator.back())
        navigator.completeFlow()
        assertEquals(listOf(Key.HOME, Key.DETAIL), navigator.state.snapshot.visibleEntries)
    }

    @Test
    fun replaceRootClearsInactiveHistoryAndChangesTheDecoratorGeneration() {
        val navigator = navigator()
        navigator.navigate(Key.DETAIL)
        navigator.switchTopLevel(Key.LIBRARY)
        navigator.navigate(Key.OTHER)
        val generation = navigator.state.snapshot.generation
        navigator.replaceRoot(Key.AUTH)
        assertEquals(listOf(Key.AUTH), navigator.state.snapshot.flow)
        assertEquals(generation + 1, navigator.state.snapshot.generation)
        assertTrue(navigator.state.snapshot.branches.all { it.entries == listOf(it.root) })
        navigator.replaceRoot(Key.LIBRARY)
        assertEquals(Key.LIBRARY, navigator.state.snapshot.active)
        assertTrue(navigator.state.snapshot.flow.isEmpty())
    }

    @Test
    fun snapshotSerializationRestoresSelectionAllHistoriesAndApplicationFlow() {
        val navigator = navigator()
        navigator.navigate(Key.DETAIL)
        navigator.switchTopLevel(Key.LIBRARY)
        navigator.navigate(Key.OTHER)
        navigator.showFlow(Key.AUTH)
        val snapshot = navigator.state.snapshot
        val restored = Json.decodeFromString<NavigationSnapshot>(Json.encodeToString(snapshot))
        assertEquals(snapshot, restored)
        val next = AppNavigator(NavigationState(restored))
        next.completeFlow()
        assertEquals(listOf(Key.LIBRARY, Key.OTHER), next.state.snapshot.visibleEntries)
        next.switchTopLevel(Key.HOME)
        assertEquals(listOf(Key.HOME, Key.DETAIL), next.state.snapshot.visibleEntries)
    }

    @Test
    fun sharedDestinationsHaveAnIndependentEntryInEachSection() {
        val navigator = navigator()
        navigator.navigate(Key.DETAIL)
        navigator.switchTopLevel(Key.LIBRARY)
        navigator.navigate(Key.DETAIL)
        assertTrue(navigator.state.snapshot.branches.all { it.entries.last() == Key.DETAIL })
        navigator.back()
        navigator.switchTopLevel(Key.HOME)
        assertEquals(listOf(Key.HOME, Key.DETAIL), navigator.state.snapshot.activeEntries)
    }

    @Test
    fun changingAvailableSectionsRemovesUnavailableHistoryAndPreservesRemainingHistory() {
        val navigator = navigator()
        navigator.navigate(Key.DETAIL)
        navigator.switchTopLevel(Key.LIBRARY)
        navigator.navigate(Key.OTHER)
        navigator.state.configure(Key.HOME, listOf(Key.HOME, Key.AUTH))
        assertEquals(Key.HOME, navigator.state.snapshot.active)
        assertEquals(listOf(Key.HOME, Key.DETAIL), navigator.state.snapshot.activeEntries)
        assertEquals(listOf(Key.HOME, Key.AUTH), navigator.state.snapshot.branches.map { it.root })
    }

    private fun navigator(): AppNavigator = AppNavigator(
        NavigationState(
            NavigationSnapshot(
                Key.HOME,
                branches = listOf(
                    NavigationBranch(Key.HOME),
                    NavigationBranch(Key.LIBRARY),
                ),
            ),
        ),
    )

    @Serializable
    enum class Key : NavKey { HOME, LIBRARY, DETAIL, OTHER, AUTH }
}
