package com.quizzgame.core.error

sealed interface AppFailure {
    data class Connectivity(val kind: ConnectivityKind) : AppFailure
    data class Service(
        val kind: ServiceKind,
        val httpStatus: Int,
        val backendReasonCode: Int? = null,
    ) : AppFailure

    data class Authentication(
        val kind: AuthenticationKind,
        val httpStatus: Int? = null,
        val backendReasonCode: Int? = null,
    ) : AppFailure

    data object InvalidServerData : AppFailure
    data class Storage(val kind: StorageKind) : AppFailure
}
