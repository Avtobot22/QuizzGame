package com.quizzgame.core.flags

import android.content.res.AssetManager
import io.github.avtobot22.featureflags.ConfigOrigin
import io.github.avtobot22.featureflags.FallbackPolicy
import io.github.avtobot22.featureflags.FeatureFlagsReader
import io.github.avtobot22.featureflags.FeatureFlagsSource
import io.github.avtobot22.featureflags.FeatureFlagsSystem
import io.github.avtobot22.featureflags.FlagDefinition
import io.github.avtobot22.featureflags.RefreshError
import io.github.avtobot22.featureflags.RefreshResult
import io.github.avtobot22.featureflags.android.AssetsFeatureFlagsSource
import io.github.avtobot22.featureflags.json.JsonConfigurationCodec

class FeatureFlagsSdkAdapter(
    remote: FeatureFlagsSource,
    local: FeatureFlagsSource,
    private val guestDefinition: FlagDefinition<Boolean>,
) : AppFeatureFlags, FeatureFlagsController {
    private val system = FeatureFlagsSystem(remote, JsonConfigurationCodec, FallbackPolicy.OnRemoteEmpty, local)
    val reader: FeatureFlagsReader get() = system.reader

    override fun guestAllowed(): Boolean = reader.get(guestDefinition)

    override suspend fun refresh(): FeatureFlagsLoadResult = when (val result = system.manager.refresh()) {
        is RefreshResult.Applied -> FeatureFlagsLoadResult.Applied
        is RefreshResult.Failure -> FeatureFlagsLoadResult.Failed(result.error.sanitized())
    }

    companion object {
        fun bundled(assets: AssetManager): FeatureFlagsSource = AssetsFeatureFlagsSource(assets)
    }
}

private fun RefreshError.sanitized(): FeatureFlagsLoadFailure = when (this) {
    is RefreshError.Transport -> (cause as? FeatureFlagsSourceException)?.failure
        ?: FeatureFlagsLoadFailure.Runtime(com.quizzgame.core.network.api.ApiUnavailableReason.TRANSPORT)
    is RefreshError.LocalSource -> FeatureFlagsLoadFailure.LocalRead
    is RefreshError.InvalidConfiguration -> if (origin == ConfigOrigin.REMOTE) {
        FeatureFlagsLoadFailure.InvalidRemote
    } else {
        FeatureFlagsLoadFailure.InvalidLocal
    }
    RefreshError.Invalidated -> FeatureFlagsLoadFailure.Closed
}
