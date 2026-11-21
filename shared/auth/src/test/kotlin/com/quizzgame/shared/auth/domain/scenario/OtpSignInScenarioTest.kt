package com.quizzgame.shared.auth.domain.scenario

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStorageException
import com.quizzgame.core.session.storage.SessionStorageFailure
import com.quizzgame.core.session.storage.SessionStore
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.entity.otp.OtpChallenge
import com.quizzgame.shared.auth.domain.entity.otp.OtpSignInAttempt
import com.quizzgame.shared.auth.domain.entity.provider.YandexAttempt
import com.quizzgame.shared.auth.domain.repository.AuthRepository
import com.quizzgame.shared.auth.domain.usecase.BeginSignInUseCase
import com.quizzgame.shared.auth.domain.usecase.LoadAuthMethodsUseCase
import com.quizzgame.shared.auth.domain.usecase.RequestOtpUseCase
import com.quizzgame.shared.auth.domain.usecase.ResendOtpUseCase
import com.quizzgame.shared.auth.domain.usecase.VerifyOtpUseCase
import com.quizzgame.shared.auth.presentation.OtpCodeSession
import com.quizzgame.shared.auth.presentation.OtpRequestPolicy
import com.quizzgame.shared.auth.presentation.OtpSignInEnvironment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.Instant

class OtpSignInScenarioTest {
    @Test
    fun explicitRetryKeepsKeyAndCodeThenCommitsOnce() = runTest {
        val fixture = fixture()
        val attempt = fixture.requestCode()
        fixture.repository.result = AuthResult.Failed(AuthFailure.General(AppFailure.OperationInterrupted))
        fixture.scenario.verify(attempt, "012345")
        fixture.repository.result = AuthResult.Success(TokenPair("access", "refresh"))
        assertEquals(Unit, (fixture.scenario.verify(attempt, "012345") as AuthResult.Success).value)
        assertEquals(listOf("012345", "012345"), fixture.repository.codes)
        assertEquals(1, fixture.repository.keys.distinct().size)
        assertEquals(1, fixture.store.commits)
    }

    @Test
    fun replacementChallengeUsesNewKey() = runTest {
        val fixture = fixture()
        val attempt = fixture.requestCode()
        fixture.repository.result = AuthResult.Failed(AuthFailure.RateLimited(5))
        fixture.scenario.verify(attempt, "012345")
        val resent = (fixture.scenario.resend(attempt) as AuthResult.Success).value
        assertEquals("replacement", resent.challenge.id)
        fixture.scenario.verify(resent, "012345")
        assertNotEquals(fixture.repository.keys.first(), fixture.repository.keys.last())
    }

    @Test
    fun storageFailureDoesNotReportSuccess() = runTest {
        val fixture = fixture()
        fixture.store.failCommit = true
        assertEquals(
            AuthResult.Failed(AuthFailure.StorageRecovery),
            fixture.scenario.verify(fixture.requestCode(), "012345"),
        )
        assertEquals(1, fixture.store.commits)
    }

    @Test
    fun tvKeepsGoogleAndValidatesPhoneWithCountryCode() = runTest {
        val fixture = fixture()
        assertEquals(
            listOf(AuthMethod.EMAIL, AuthMethod.SMS, AuthMethod.GOOGLE),
            (fixture.scenario.methods(AuthSurface.TV) as AuthResult.Success).value,
        )
        assertEquals(
            AuthResult.Failed(AuthFailure.InvalidIdentifier),
            fixture.scenario.request(AuthMethod.SMS, "89123456789"),
        )
        fixture.scenario.request(AuthMethod.SMS, "+7 (912) 345-67-89")
        assertEquals("+79123456789", fixture.repository.identifier)
    }

    @Test
    fun oldCodeCannotStartSessionAfterSignOut() = runTest {
        val fixture = fixture()
        val attempt = fixture.requestCode()
        fixture.session.signOut()
        assertEquals(AuthResult.Failed(AuthFailure.SessionChanged), fixture.scenario.verify(attempt, "012345"))
        assertEquals(0, fixture.store.commits)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun retryAfterKeepsItsDeadlineWhenTheCodeSessionIsReset() = runTest {
        val fixture = fixture()
        val environment = OtpSignInEnvironment({ testScheduler.currentTime }, { Instant.EPOCH })
        val otp = OtpCodeSession(fixture.scenario, environment)
        val initial = otp.request(AuthMethod.EMAIL, "guest@example.test") as AuthResult.Success
        val code = initial.value.withCode("0 12-3456")
        assertEquals("012345", code.code)
        fixture.repository.result = AuthResult.Failed(AuthFailure.RateLimited(5))
        val failed = otp.verify(code.code) as AuthResult.Failed
        val blockedUntil = environment.blockedUntil(0, failed.failure)
        assertEquals(5_000L, code.resendWaitMillis(blockedUntil, environment.elapsed()))

        otp.reset()
        assertEquals(AuthResult.Failed(AuthFailure.Expired), otp.verify(code.code))
        assertEquals(false, OtpRequestPolicy.canRequest(false, null, blockedUntil, environment.elapsed()))

        advanceTimeBy(5_000)
        assertEquals(true, OtpRequestPolicy.canRequest(false, null, blockedUntil, environment.elapsed()))
    }

    private suspend fun TestScope.fixture(): Fixture {
        val store = TestStore()
        return Fixture(store, SessionManager.open(store, StandardTestDispatcher(testScheduler)))
    }

    private class Fixture(val store: TestStore, val session: SessionManager) {
        val repository = FakeRepository()
        val scenario = OtpSignInScenario(
            LoadAuthMethodsUseCase(repository),
            BeginSignInUseCase(session),
            RequestOtpUseCase(repository),
            ResendOtpUseCase(repository),
            VerifyOtpUseCase(repository),
        )

        suspend fun requestCode(): OtpSignInAttempt =
            (scenario.request(AuthMethod.EMAIL, "guest@example.test") as AuthResult.Success).value
    }

    private class TestStore : SessionStore {
        private var record = SessionRecord("a5b6f452-355d-4001-a98a-652bf45bd785", null)
        var commits = 0
            private set
        var failCommit = false

        override fun read() = record

        override fun write(tokens: TokenPair?): SessionRecord = error("Use transactional write")

        override fun writeRecord(record: SessionRecord): SessionRecord {
            if (record.tokens != null) {
                commits++
                if (failCommit) throw SessionStorageException(SessionStorageFailure.WRITE)
            }
            this.record = record
            return record
        }
    }

    private class FakeRepository : AuthRepository {
        var result: AuthResult<TokenPair> = AuthResult.Success(TokenPair("access", "refresh"))
        val keys = mutableListOf<String>()
        val codes = mutableListOf<String>()
        var identifier: String? = null

        override suspend fun methods(surface: AuthSurface) =
            AuthResult.Success(listOf(AuthMethod.EMAIL, AuthMethod.SMS, AuthMethod.GOOGLE, AuthMethod.YANDEX))

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
