package com.quizzgame.shared.auth.domain

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.session.lifecycle.SessionChangeResult
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.storage.SessionStorageFailure
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.entity.OtpChallenge
import com.quizzgame.shared.auth.domain.entity.OtpIntent
import com.quizzgame.shared.auth.domain.entity.OtpState
import com.quizzgame.shared.auth.domain.entity.OtpStep
import com.quizzgame.shared.auth.domain.entity.YandexAttempt
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.Instant

class OtpFlowTest {
    @Test
    fun explicitRetryKeepsKeyAndCodeThenCommitsOnce() = runTest {
        val fixture = Fixture()
        fixture.enterCode()
        fixture.repository.result = AuthResult.Failed(AuthFailure.General(AppFailure.OperationInterrupted))
        fixture.send(OtpIntent.Verify)
        fixture.repository.result = AuthResult.Success(TokenPair("access", "refresh"))
        fixture.send(OtpIntent.Verify)
        assertEquals(OtpStep.Finished, fixture.state.step)
        assertEquals(listOf("012345", "012345"), fixture.repository.codes)
        assertEquals(1, fixture.repository.keys.distinct().size)
        assertEquals(1, fixture.commits)
    }

    @Test
    fun retryAfterBlocksRequestsAndReplacementUsesNewKey() = runTest {
        val fixture = Fixture()
        fixture.enterCode()
        fixture.repository.result = AuthResult.Failed(AuthFailure.RateLimited(5))
        fixture.send(OtpIntent.Verify)
        fixture.send(OtpIntent.Verify)
        fixture.send(OtpIntent.Resend)
        assertEquals(1, fixture.repository.codes.size)
        fixture.elapsed = 5000
        fixture.send(OtpIntent.Resend)
        assertEquals("replacement", (fixture.state.step as OtpStep.Code).challenge.id)
        fixture.send(OtpIntent.Edit("012345"))
        fixture.send(OtpIntent.Verify)
        assertNotEquals(fixture.repository.keys.first(), fixture.repository.keys.last())
    }

    @Test
    fun storageFailureDoesNotReportSuccess() = runTest {
        val fixture = Fixture()
        fixture.commitResult = SessionChangeResult.NeedsStorageRecovery(SessionStorageFailure.WRITE)
        fixture.enterCode()
        fixture.send(OtpIntent.Verify)
        assertEquals(AuthFailure.StorageRecovery, fixture.state.failure)
        assertEquals(1, fixture.commits)
    }

    @Test
    fun tvFiltersOauthAndValidatesPhoneWithCountryCode() = runTest {
        val fixture = Fixture()
        fixture.send(OtpIntent.Load)
        assertEquals(listOf(AuthMethod.EMAIL, AuthMethod.SMS), (fixture.state.step as OtpStep.Methods).choices)
        fixture.send(OtpIntent.Choose(AuthMethod.SMS))
        fixture.send(OtpIntent.Edit("89123456789"))
        fixture.send(OtpIntent.Send)
        assertEquals(AuthFailure.InvalidIdentifier, fixture.state.failure)
        fixture.send(OtpIntent.Edit("+7 (912) 345-67-89"))
        fixture.send(OtpIntent.Send)
        assertEquals("+79123456789", fixture.repository.identifier)
    }

    private class Fixture {
        val repository = FakeRepository()
        var elapsed = 0L
        var commits = 0
        var commitResult: SessionChangeResult = SessionChangeResult.Committed
        private val auth = AuthUseCase(repository) {
            object : AuthSessionAttempt {
                override val deviceId = "test-device"
                override suspend fun signIn(tokens: TokenPair): SessionChangeResult {
                    commits++
                    return commitResult
                }
            }
        }
        private val flow = OtpFlow(auth, AuthSurface.TV, { elapsed }, { Instant.EPOCH })
        var state = OtpState()

        suspend fun send(intent: OtpIntent) = flow.handle(intent, state) { state = it }

        suspend fun enterCode() {
            send(OtpIntent.Load)
            send(OtpIntent.Choose(AuthMethod.EMAIL))
            send(OtpIntent.Edit("guest@example.test"))
            send(OtpIntent.Send)
            send(OtpIntent.Edit("012345"))
        }
    }

    private class FakeRepository : AuthRepository {
        var result: AuthResult<TokenPair> = AuthResult.Success(TokenPair("access", "refresh"))
        val keys = mutableListOf<String>()
        val codes = mutableListOf<String>()
        var identifier: String? = null

        override suspend fun methods(surface: AuthSurface) =
            AuthResult.Success(listOf(AuthMethod.EMAIL, AuthMethod.SMS, AuthMethod.GOOGLE))

        override suspend fun request(method: AuthMethod, identifier: String): AuthResult<OtpChallenge> {
            this.identifier = identifier
            return AuthResult.Success(challenge("original"))
        }

        override suspend fun resend(challenge: OtpChallenge) = AuthResult.Success(challenge("replacement"))

        override suspend fun verify(id: String, code: String, device: String, key: String): AuthResult<TokenPair> {
            codes += code
            keys += key
            return result
        }

        override suspend fun googleChallenge() = error("Unexpected provider")
        override suspend fun verifyGoogle(id: String, token: String, device: String) = error("Unexpected provider")
        override suspend fun yandexAttempt(device: String) = error("Unexpected provider")
        override suspend fun redeemYandex(attempt: YandexAttempt, device: String) = error("Unexpected provider")

        private fun challenge(id: String) = OtpChallenge(id, "g***@example.test", 6, Instant.ofEpochSecond(30), 2, 4)
    }
}
