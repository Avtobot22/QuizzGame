package com.quizzgame.feature.authprovider.tv.presentation

internal sealed interface TvDeviceAuthIntent {
    data object Start : TvDeviceAuthIntent
    data class Foreground(val active: Boolean) : TvDeviceAuthIntent
    data object Back : TvDeviceAuthIntent
}
