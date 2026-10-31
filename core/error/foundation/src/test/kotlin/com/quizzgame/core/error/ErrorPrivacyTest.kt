package com.quizzgame.core.error

import com.quizzgame.core.network.BackendErrorDetails
import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.network.RefreshFailureReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ErrorPrivacyTest {

    private val classifier: ErrorClassifier = DefaultErrorClassifier()

    @Test
    fun `server debug and friendly text do not survive direct or refresh classification`() {
        val secrets = listOf(
            "Bearer SECRET_ACCESS_TOKEN",
            "SECRET_REFRESH_TOKEN",
            "SECRET_PENDING_TOKEN",
            "user@example.test",
            "otp=123456",
            "https://private.example.test/path?key=SECRET_QUERY",
        )
        val backend = BackendErrorDetails(9001, secrets.joinToString(" "), secrets.reversed().joinToString(" "))
        val cases = listOf(
            NetworkFailure.Http(503, backend) to AppFailure.Service(ServiceKind.UNAVAILABLE, 503, 9001),
            NetworkFailure.Http(401, backend) to AppFailure.Authentication(AuthenticationKind.REQUIRED, 401, 9001),
            NetworkFailure.RefreshFailed(RefreshFailureReason.Http(500, backend)) to
                AppFailure.Service(ServiceKind.UNAVAILABLE, 500, 9001),
            NetworkFailure.RefreshFailed(RefreshFailureReason.Http(401, backend)) to
                AppFailure.Service(ServiceKind.CLIENT_REJECTED, 401, 9001),
        )
        cases.forEach { (technical, expected) ->
            val semantic = classifier.fromNetwork(technical)
            assertEquals(expected, semantic)
            secrets.forEach { secret ->
                assertFalse(semantic.toString().contains(secret))
            }
        }
    }

    @Test
    fun `server copy does not affect semantic failure identity`() {
        val first = BackendErrorDetails(71, "private server trace", "user@example.test")
        val second = BackendErrorDetails(71, "different trace", "different text")
        listOf(401, 403, 429, 503).forEach { status ->
            assertEquals(
                classifier.fromNetwork(NetworkFailure.Http(status, first)),
                classifier.fromNetwork(NetworkFailure.Http(status, second)),
            )
            assertEquals(
                classifier.fromNetwork(NetworkFailure.RefreshFailed(RefreshFailureReason.Http(status, first))),
                classifier.fromNetwork(NetworkFailure.RefreshFailed(RefreshFailureReason.Http(status, second))),
            )
        }
    }

    @Test
    fun `server secrets never enter presentation for direct or refresh failures`() {
        val secret = "Bearer SECRET_TOKEN user@example.test otp=123456 https://private.test/?key=SECRET"
        val backend = BackendErrorDetails(9001, secret, secret)
        val policy: ErrorDecisionPolicy = DefaultErrorDecisionPolicy()
        val failures = listOf(
            NetworkFailure.Http(503, backend),
            NetworkFailure.Http(401, backend),
            NetworkFailure.RefreshFailed(RefreshFailureReason.Http(401, backend)),
        )
        failures.forEach { failure ->
            OperationKind.entries.forEach { operation ->
                val context = ErrorOperationContext(operation, true, RetrySafety.REQUIRES_CONFIRMATION)
                val spec = policy.decide(classifier.fromNetwork(failure), context)
                assertFalse(spec.toString().contains(secret))
                assertFalse(spec.toString().contains("SECRET"))
                assertFalse(spec.toString().contains("user@example.test"))
            }
        }
    }
}
