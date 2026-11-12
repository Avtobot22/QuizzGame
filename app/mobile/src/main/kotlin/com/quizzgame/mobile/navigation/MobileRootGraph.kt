package com.quizzgame.mobile.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.quizzgame.core.navigation.NavAction
import com.quizzgame.core.navigation.dispatch
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.registration.rememberFeatureEntryProvider

@Composable
internal fun MobileRootGraph(
    graphs: Set<FeatureEntries<MobileNavigationContext>>,
    initialDestination: NavKey,
    isCurrentGraph: () -> Boolean,
    roomCode: String,
    onRoomCodeChange: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val stack = rememberNavBackStack(initialDestination)
    val navigate: (NavAction) -> Unit = { action ->
        if (isCurrentGraph()) stack.dispatch(action)
    }
    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = stack,
        onBack = { stack.dispatch(NavAction.Pop) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = rememberFeatureEntryProvider(
            graphs,
            MobileNavigationContext(
                roomCode,
                onRoomCodeChange,
                navigate = navigate,
                onLogout = onLogout,
            ),
        ),
    )
}
