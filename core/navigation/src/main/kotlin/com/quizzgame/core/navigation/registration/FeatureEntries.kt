package com.quizzgame.core.navigation.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider

fun interface FeatureEntries<in C> {
    fun register(scope: EntryProviderScope<NavKey>, context: State<C>)
}

internal fun <C> featureEntryProvider(
    graphs: Collection<FeatureEntries<C>>,
    context: State<C>,
): (NavKey) -> NavEntry<NavKey> = entryProvider { graphs.forEach { it.register(this, context) } }

@Composable
fun <C> rememberFeatureEntryProvider(graphs: Collection<FeatureEntries<C>>, context: C): (NavKey) -> NavEntry<NavKey> {
    val current = rememberUpdatedState(context)
    return remember(graphs) { featureEntryProvider(graphs, current) }
}
