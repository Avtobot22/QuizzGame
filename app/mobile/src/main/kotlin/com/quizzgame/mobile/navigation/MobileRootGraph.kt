package com.quizzgame.mobile.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.quizzgame.core.navigation.NavAction
import com.quizzgame.core.navigation.SignInRoute
import com.quizzgame.core.navigation.SplashRoute
import com.quizzgame.core.navigation.dispatch
import com.quizzgame.core.session.SessionState

@Composable
internal fun MobileRootGraph(sessionState: SessionState) {
    val stack = rememberNavBackStack(SplashRoute)
    if (sessionState == SessionState.Expired && stack.toList() != listOf(SignInRoute)) {
        LaunchedEffect(sessionState) { stack.dispatch(NavAction.ResetTo(SignInRoute)) }
        return
    }
    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = stack,
        onBack = { stack.dispatch(NavAction.Pop) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<SplashRoute> {
                Box(Modifier.fillMaxSize().testTag("splash")) {
                    MobileSplashDestination(
                        onSignIn = { stack.dispatch(NavAction.ResetTo(SignInRoute)) },
                        onContinue = { stack.dispatch(NavAction.ResetTo(JoinRoomRoute)) },
                    )
                }
            }
            entry<SignInRoute> {
                Box(Modifier.fillMaxSize().testTag("signin")) {
                    MobileSignInDestination { stack.dispatch(NavAction.ResetTo(JoinRoomRoute)) }
                }
            }
            entry<JoinRoomRoute> {
                Box(Modifier.fillMaxSize().testTag("join-room")) {
                    MobileJoinRoomDestination { stack.dispatch(NavAction.ResetTo(SignInRoute)) }
                }
            }
        },
    )
}
