package com.quizzgame.core.flags

import com.quizzgame.core.network.api.ApiUnavailableReason
import com.quizzgame.core.network.result.NetworkFailure

sealed interface FeatureFlagsLoadResult {
    data object Applied : FeatureFlagsLoadResult
    data class Failed(val reason: FeatureFlagsLoadFailure) : FeatureFlagsLoadResult
}

sealed interface FeatureFlagsLoadFailure {
    data class Network(val failure: NetworkFailure) : FeatureFlagsLoadFailure
    data class Runtime(val reason: ApiUnavailableReason) : FeatureFlagsLoadFailure
    data object InvalidRemote : FeatureFlagsLoadFailure
    data object InvalidLocal : FeatureFlagsLoadFailure
    data object LocalRead : FeatureFlagsLoadFailure
    data object Closed : FeatureFlagsLoadFailure
}

internal class FeatureFlagsSourceException(val failure: FeatureFlagsLoadFailure) : Exception("Flags source failed")
