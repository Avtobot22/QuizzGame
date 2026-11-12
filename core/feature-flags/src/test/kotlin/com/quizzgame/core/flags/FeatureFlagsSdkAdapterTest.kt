package com.quizzgame.core.flags

import com.quizzgame.core.network.result.NetworkFailure
import io.github.avtobot22.featureflags.FeatureFlagsSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FeatureFlagsSdkAdapterTest {
    @Test
    fun defaultsDenyGuestBeforeAnyPublication() {
        assertFalse(adapter().guestAllowed())
    }

    @Test
    fun nonemptyRemoteReplacesSnapshotWithoutReadingBundledFlags() = runTest {
        var payload = ENABLED
        var localReads = 0
        val adapter = adapter(remote = { payload }, local = {
            localReads++
            ENABLED
        })
        assertEquals(FeatureFlagsLoadResult.Applied, adapter.refresh())
        assertTrue(adapter.guestAllowed())
        payload = """{"featureToggles":[{"feature":"unrelated","enabled":true}]}"""
        assertEquals(FeatureFlagsLoadResult.Applied, adapter.refresh())
        assertFalse(adapter.guestAllowed())
        assertEquals(0, localReads)
    }

    @Test
    fun emptyRemoteUsesBundledConfigurationAndEmptyBundleRemainsValid() = runTest {
        var localReads = 0
        val adapter = adapter(local = {
            localReads++
            EMPTY
        })
        assertEquals(FeatureFlagsLoadResult.Applied, adapter.refresh())
        assertFalse(adapter.guestAllowed())
        assertEquals(1, localReads)
    }

    @Test
    fun emptyRemoteCanApplyExplicitBundledGuestRule() = runTest {
        val adapter = adapter(local = { ENABLED })
        assertEquals(FeatureFlagsLoadResult.Applied, adapter.refresh())
        assertTrue(adapter.guestAllowed())
    }

    @Test
    fun remoteFailurePreservesSnapshotButDoesNotReportAppliedOrReadBundle() = runTest {
        var failed = false
        var localReads = 0
        val adapter = adapter(
            remote = {
                if (failed) throw FeatureFlagsSourceException(FeatureFlagsLoadFailure.Network(NetworkFailure.Http(503)))
                ENABLED
            },
            local = {
                localReads++
                EMPTY
            },
        )
        adapter.refresh()
        failed = true
        assertEquals(
            FeatureFlagsLoadResult.Failed(FeatureFlagsLoadFailure.Network(NetworkFailure.Http(503))),
            adapter.refresh(),
        )
        assertTrue(adapter.guestAllowed())
        assertEquals(0, localReads)
    }

    @Test
    fun duplicateAndWrongBooleanRejectTheWholeRemoteConfiguration() = runTest {
        val invalid = listOf(
            """{"featureToggles":[{"feature":"dashboard.guestMode","enabled":true},""" +
                """{"feature":"dashboard.guestMode","enabled":false}]}""",
            """{"featureToggles":[{"feature":"dashboard.guestMode","enabled":"true"}]}""",
        )
        for (payload in invalid) {
            var localReads = 0
            val adapter = adapter(remote = { payload }, local = {
                localReads++
                ENABLED
            })
            assertEquals(FeatureFlagsLoadResult.Failed(FeatureFlagsLoadFailure.InvalidRemote), adapter.refresh())
            assertFalse(adapter.guestAllowed())
            assertEquals(0, localReads)
        }
    }

    @Test
    fun guestKeysArePlatformSpecificAndCaseSensitive() = runTest {
        val adapter = adapter(remote = {
            """{"featureToggles":[{"feature":"joinRoom.guestMode","enabled":true},""" +
                """{"feature":"Dashboard.guestMode","enabled":true}]}"""
        })
        assertEquals(FeatureFlagsLoadResult.Applied, adapter.refresh())
        assertFalse(adapter.guestAllowed())
        assertTrue(adapter.reader.get(AppFeatureDefinitions.mobileGuest))
    }

    @Test
    fun concurrentRefreshAttemptsAreSerializedByTheSdk() = runTest {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var calls = 0
        var running = 0
        var maxRunning = 0
        val adapter = adapter(remote = {
            calls++
            running++
            maxRunning = maxOf(maxRunning, running)
            entered.complete(Unit)
            release.await()
            running--
            ENABLED
        })
        val first = async { adapter.refresh() }
        entered.await()
        val second = async { adapter.refresh() }
        runCurrent()
        assertEquals(1, calls)
        release.complete(Unit)
        assertEquals(FeatureFlagsLoadResult.Applied, first.await())
        assertEquals(FeatureFlagsLoadResult.Applied, second.await())
        assertEquals(1, maxRunning)
        assertEquals(2, calls)
    }

    @Test
    fun cancellationPropagatesWithoutPublishingFallback() = runTest {
        var localReads = 0
        val adapter = adapter(remote = { throw CancellationException("cancel") }, local = {
            localReads++
            ENABLED
        })
        var cancelled = false
        try {
            adapter.refresh()
        } catch (_: CancellationException) {
            cancelled = true
        }
        assertTrue(cancelled)
        assertEquals(0, localReads)
        assertFalse(adapter.guestAllowed())
    }

    @Test
    fun invalidAndUnreadableBundlesHaveDistinctSanitizedFailures() = runTest {
        val invalid = adapter(local = { "invalid" })
        assertEquals(FeatureFlagsLoadResult.Failed(FeatureFlagsLoadFailure.InvalidLocal), invalid.refresh())
        val unreadable = adapter(local = { throw java.io.IOException("private path") })
        assertEquals(FeatureFlagsLoadResult.Failed(FeatureFlagsLoadFailure.LocalRead), unreadable.refresh())
    }

    private fun adapter(
        remote: suspend () -> String = { EMPTY },
        local: suspend () -> String = { EMPTY },
    ) = FeatureFlagsSdkAdapter(
        FeatureFlagsSource { remote() },
        FeatureFlagsSource { local() },
        AppFeatureDefinitions.tvGuest,
    )

    private companion object {
        const val EMPTY = """{"featureToggles":[]}"""
        const val ENABLED = """{"featureToggles":[{"feature":"dashboard.guestMode","enabled":true}]}"""
    }
}
