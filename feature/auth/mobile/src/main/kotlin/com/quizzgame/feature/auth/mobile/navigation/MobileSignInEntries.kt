package com.quizzgame.feature.auth.mobile.navigation

import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.routes.common.SignInRoute
import com.quizzgame.feature.auth.mobile.ui.MobileSignInScreen

val MobileSignInEntries = FeatureEntries<MobileSignInCallbacks> { scope, context ->
    scope.entry<SignInRoute> { MobileSignInScreen(context.value.onCancel, context.value.recovery) }
}
