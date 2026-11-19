package com.quizzgame.feature.auth.tv.navigation

import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.routes.common.SignInRoute
import com.quizzgame.feature.auth.tv.ui.TvSignInScreen

val TvSignInEntries = FeatureEntries<TvSignInCallbacks> { scope, context ->
    scope.entry<SignInRoute> { TvSignInScreen(context.value.onCancel, context.value.recovery) }
}
