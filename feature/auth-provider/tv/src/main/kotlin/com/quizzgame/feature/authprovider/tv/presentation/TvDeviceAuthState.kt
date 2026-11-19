package com.quizzgame.feature.authprovider.tv.presentation

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthorization

internal sealed interface TvDeviceAuthState {
    data object Idle : TvDeviceAuthState
    data object Starting : TvDeviceAuthState
    data class Waiting(val authorization: DeviceAuthorization) : TvDeviceAuthState
    data class Confirming(val authorization: DeviceAuthorization) : TvDeviceAuthState
    data class Failed(val failure: AuthFailure) : TvDeviceAuthState
}
