package com.quizzgame.mobile.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.quizzgame.core.navigation.GuestAccess
import com.quizzgame.core.navigation.NavAction
import com.quizzgame.core.navigation.RootNavigationDecision
import com.quizzgame.core.navigation.decideRootNavigation
import com.quizzgame.core.navigation.dispatch
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.registration.rememberFeatureEntryProvider
import com.quizzgame.core.navigation.routes.JoinRoomRoute
import com.quizzgame.core.navigation.routes.SignInRoute
import com.quizzgame.core.navigation.routes.SplashRoute
import com.quizzgame.core.session.lifecycle.SessionState

@Composable
internal fun MobileRootGraph(
    graphs: Set<FeatureEntries<MobileNavigationContext>>,
    sessionState: SessionState,
    roomCode: String,
    onRoomCodeChange: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var guestAccess by rememberSaveable { mutableStateOf(GuestAccess.UNKNOWN) }
    val stack = rememberNavBackStack(SplashRoute)
    val decision = decideRootNavigation(sessionState, guestAccess)
    if (decision in setOf(RootNavigationDecision.BOOTSTRAP, RootNavigationDecision.BLOCKED)) return
    val signInRequired = decision == RootNavigationDecision.SIGN_IN && stack.toList() != listOf(SignInRoute)
    val contentForbidden = JoinRoomRoute in stack && decision != RootNavigationDecision.CONTENT
    if (signInRequired || contentForbidden) {
        LaunchedEffect(decision) {
            val root = if (decision == RootNavigationDecision.SIGN_IN) SignInRoute else SplashRoute
            stack.dispatch(NavAction.ResetTo(root))
        }
        return
    }
    val continueToRoom: (GuestAccess) -> Unit = { permission ->
        guestAccess = permission
        val root = when (decideRootNavigation(sessionState, permission)) {
            RootNavigationDecision.CONTENT -> JoinRoomRoute
            RootNavigationDecision.SIGN_IN -> SignInRoute
            else -> SplashRoute
        }
        stack.dispatch(NavAction.ResetTo(root))
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
                navigate = stack::dispatch,
                onContinue = continueToRoom,
                onLogout = onLogout,
            ),
        ),
    )
}
