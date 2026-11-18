package com.quizzgame.feature.dashboard.mobile.presentation

import androidx.lifecycle.ViewModelStore
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.feature.dashboard.mobile.domain.DashboardRepository
import com.quizzgame.feature.dashboard.mobile.domain.LoadDashboardUseCase
import com.quizzgame.feature.dashboard.mobile.domain.entity.DashboardResult
import com.quizzgame.feature.dashboard.mobile.domain.entity.MobileTabId
import com.quizzgame.feature.dashboard.mobile.domain.entity.localDashboard
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MobileDashboardViewModelTest {
    @Test
    fun menuIsUsableBeforeHttpAndAfterFailureThenRetryLoadsRemote() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val first = CompletableDeferred<DashboardResult>()
            var calls = 0
            val repository = object : DashboardRepository {
                override suspend fun load(): DashboardResult {
                    calls++
                    return if (calls == 1) first.await() else DashboardResult.Loaded(localDashboard())
                }
            }
            val model = MobileDashboardViewModel(LoadDashboardUseCase(repository))
            store.put("dashboard", model)
            assertEquals(MobileTabId.entries, model.state.value.dashboard.tabs.map { it.id })
            runCurrent()
            model.onIntent(MobileDashboardIntent.Retry)
            assertEquals(1, calls)
            first.complete(DashboardResult.Failed(AppFailure.InvalidServerData))
            runCurrent()
            assertTrue(model.state.value is MobileDashboardState.FailureWithFallback)
            assertEquals(MobileTabId.PLAY, model.state.value.dashboard.tabs.first().id)
            model.onIntent(MobileDashboardIntent.Retry)
            runCurrent()
            assertTrue(model.state.value is MobileDashboardState.Remote)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
