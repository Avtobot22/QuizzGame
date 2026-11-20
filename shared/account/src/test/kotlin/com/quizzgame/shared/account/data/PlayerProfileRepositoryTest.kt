package com.quizzgame.shared.account.data

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.shared.account.domain.entity.PlayerNickname
import com.quizzgame.shared.account.domain.entity.PlayerProfile
import com.quizzgame.shared.account.domain.entity.SavePlayerNicknameResult
import com.quizzgame.shared.account.domain.usecase.GeneratePlayerNicknameUseCase
import com.quizzgame.shared.account.domain.usecase.SavePlayerNicknameUseCase
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.util.UUID

class PlayerProfileRepositoryTest {
    @Test
    fun sendsTheNormalizedNameAndReadsTheProfileFromTheServerOnEachLoad() = runTest {
        val fixture = Fixture()
        val name = "Éclair"
        val saved = fixture.save(fixture.user.id, GENERATION, "  E\u0301clair  ")

        assertEquals(SavePlayerNicknameResult.Saved(fixture.user.copy(displayName = name)), saved)
        assertEquals(name to GENERATION, fixture.request)
        assertEquals(name, (fixture.repository.load() as PlayerProfile.SignedIn).user?.displayName)
        fixture.serverUser = fixture.user.copy(displayName = "Новое имя")
        assertEquals("Новое имя", (fixture.repository.load() as PlayerProfile.SignedIn).user?.displayName)
        assertEquals(2, fixture.reads)
    }

    @Test
    fun rejectsAResponseFromAnotherAccount() = runTest {
        val fixture = Fixture()
        fixture.response = { fixture.user.copy(id = UUID(0, 2), displayName = it) }

        assertEquals(
            SavePlayerNicknameResult.Failed(AppFailure.InvalidServerData),
            fixture.save(fixture.user.id, GENERATION, "QuizFox-201"),
        )
    }

    @Test
    fun rejectsAStaleEditorBeforeSendingTheRequest() = runTest {
        val fixture = Fixture()
        fixture.session = SessionState.Authenticated(GENERATION + 1)

        assertEquals(
            SavePlayerNicknameResult.AccountChanged,
            fixture.save(fixture.user.id, GENERATION, "QuizFox-201"),
        )
        assertNull(fixture.request)
    }

    @Test
    fun discardsTheResponseAfterAccountSwitch() = runTest {
        val fixture = Fixture()
        fixture.response = {
            fixture.session = SessionState.Authenticated(GENERATION + 1)
            fixture.user.copy(displayName = it)
        }

        assertEquals(
            SavePlayerNicknameResult.AccountChanged,
            fixture.save(fixture.user.id, GENERATION, "QuizFox-201"),
        )
    }

    @Test
    fun invalidNameSkipsNetworkAndGeneratedNamesCanBeSaved() = runTest {
        val fixture = Fixture()
        assertEquals(SavePlayerNicknameResult.InvalidName, fixture.save(fixture.user.id, GENERATION, " "))
        assertNull(fixture.request)
        val suggestions = GeneratePlayerNicknameUseCase()
        repeat(40) {
            val name = suggestions()
            assertEquals(name, PlayerNickname.normalize(name))
        }
    }

    private class Fixture {
        val user = CurrentUser(UUID(0, 1), Instant.EPOCH, null, null, null, null, emptyList())
        var serverUser = user
        var session: SessionState = SessionState.Authenticated(GENERATION)
        var request: Pair<String, Long>? = null
        var reads = 0
        var response: (String) -> CurrentUser = { user.copy(displayName = it) }
        private val remote = object : AccountRemoteDataSource {
            override suspend fun loadProfile(generation: Long): ApiCallResult<CurrentUser> {
                assertEquals(GENERATION, generation)
                reads++
                return ApiCallResult.Executed(NetworkResult.Success(serverUser))
            }

            override suspend fun updateNickname(name: String, generation: Long): ApiCallResult<CurrentUser> {
                request = name to generation
                serverUser = response(name)
                return ApiCallResult.Executed(NetworkResult.Success(serverUser))
            }
        }
        val repository = PlayerProfileRepositoryImpl(remote, SessionStateReader { session })
        val save = SavePlayerNicknameUseCase(repository)
    }

    private companion object {
        const val GENERATION = 6L
    }
}
