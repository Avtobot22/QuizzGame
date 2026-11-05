package com.quizzgame.core.session.refresh

import com.quizzgame.core.network.result.BackendErrorDetails
import com.quizzgame.core.session.refresh.api.RefreshFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ContractRefreshRevocationPolicyTest {
    private val policy = ContractRefreshRevocationPolicy()

    @Test
    fun onlyBadRequestWithAKnownTerminalCodeConfirmsRevocation() {
        val statuses = listOf(200, 400, 401, 403, 409, 429, 500, 503)
        val codes = listOf(21001, 21002, 21003, 21004, 21005, 21008, 0)
        statuses.forEach { status ->
            codes.forEach { code ->
                val failure = RefreshFailure.Http(status, BackendErrorDetails(code, "Fixture rejection", null))
                assertEquals(status == 400 && code in 21001..21004, policy.isRevoked(failure))
            }
        }
    }

    @Test
    fun missingBackendDetailsAndTransportFailuresNeverConfirmRevocation() {
        listOf(
            RefreshFailure.Http(400),
            RefreshFailure.Timeout,
            RefreshFailure.Io,
            RefreshFailure.InvalidResponse,
        ).forEach { assertFalse(policy.isRevoked(it)) }
    }
}
