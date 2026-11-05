package com.quizzgame.core.navigation.registration

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FeatureEntriesTest {
    @Test
    fun contributionsResolveTheirKeysAndReceiveTheCurrentContainerContext() {
        val contexts = mutableListOf<String>()
        val first = FeatureEntries<String> { scope, context ->
            contexts += context.value
            scope.entry<First> {}
        }
        val second = FeatureEntries<String> { scope, context ->
            contexts += context.value
            scope.entry<Second> {}
        }
        val provider = featureEntryProvider(setOf(first, second), mutableStateOf("current-container"))
        assertNotEquals(provider(First).contentKey, provider(Second).contentKey)
        assertEquals(listOf("current-container", "current-container"), contexts)
    }

    @Test
    fun duplicateKeysAreRejectedRatherThanDependingOnSetIterationOrder() {
        val graphs = listOf(
            FeatureEntries<Unit> { scope, _ -> scope.entry<First> {} },
            FeatureEntries<Unit> { scope, _ -> scope.entry<First> {} },
        )
        assertThrows(IllegalArgumentException::class.java) { featureEntryProvider(graphs, mutableStateOf(Unit)) }
    }

    @Serializable
    private data object First : NavKey

    @Serializable
    private data object Second : NavKey
}
