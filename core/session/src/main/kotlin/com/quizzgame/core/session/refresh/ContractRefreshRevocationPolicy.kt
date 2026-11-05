package com.quizzgame.core.session.refresh

import com.quizzgame.core.session.refresh.api.RefreshFailure

// Published wire codes are kept together as a contract registry.
@Suppress("MagicNumber")
enum class BackendAuthErrorCode(val reasonCode: Int) {
    REFRESH_INVALID(21001),
    REFRESH_EXPIRED(21002),
    REFRESH_REVOKED(21003),
    REFRESH_REUSE_DETECTED(21004),
}

class ContractRefreshRevocationPolicy : RefreshRevocationPolicy {
    override fun isRevoked(failure: RefreshFailure): Boolean =
        failure is RefreshFailure.Http && failure.status == INVALID_GRANT_HTTP_STATUS &&
            BackendAuthErrorCode.entries.any { it.reasonCode == failure.backend?.reasonCode }
}

private const val INVALID_GRANT_HTTP_STATUS = 400
