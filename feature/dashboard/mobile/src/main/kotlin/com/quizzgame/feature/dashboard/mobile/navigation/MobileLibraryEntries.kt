package com.quizzgame.feature.dashboard.mobile.navigation

import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.dashboard.mobile.ui.MobileLibraryScreen

val MobileLibraryEntries = FeatureEntries<Unit> { scope, _ ->
    scope.entry<LibraryRoute> { MobileLibraryScreen() }
}
