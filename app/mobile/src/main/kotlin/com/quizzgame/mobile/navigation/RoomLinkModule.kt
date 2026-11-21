package com.quizzgame.mobile.navigation

import com.quizzgame.core.navigation.request.ExternalNavigationParser
import com.quizzgame.core.navigation.request.NavigationRequest
import com.quizzgame.feature.play.mobile.navigation.MobileJoinRoute
import com.quizzgame.feature.play.mobile.navigation.PlayRoute
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinLink
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import java.util.UUID

@Module
@InstallIn(SingletonComponent::class)
internal object RoomLinkModule {
    @Provides
    @IntoSet
    fun parser(links: RoomJoinLink): ExternalNavigationParser = ExternalNavigationParser { uri ->
        links.codeFrom(uri)?.let { code ->
            NavigationRequest(UUID.randomUUID().toString(), MobileJoinRoute(code), PlayRoute)
        }
    }
}
