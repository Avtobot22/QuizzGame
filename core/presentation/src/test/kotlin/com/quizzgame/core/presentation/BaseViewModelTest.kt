package com.quizzgame.core.presentation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private lateinit var viewModel: FixtureViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        viewModel = FixtureViewModel()
        store.put("fixture", viewModel)
    }

    @After
    fun cleanup() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsCreatedLazilyOnceAndIntentUpdatesStateSynchronously() {
        assertEquals(0, viewModel.initialStateCreations)
        assertEquals(0, viewModel.state.value)
        assertEquals(1, viewModel.initialStateCreations)
        assertEquals(0, viewModel.processedIntents)
        viewModel.onIntent(FixtureIntent.Add(3))
        assertEquals(3, viewModel.state.value)
        assertEquals(1, viewModel.processedIntents)
        assertEquals(1, viewModel.initialStateCreations)
    }

    @Test
    fun concurrentStateUpdatesDoNotLoseIncrements() = runTest(dispatcher) {
        List(8) {
            async(Dispatchers.Default) {
                repeat(1_000) { viewModel.increment() }
            }
        }.awaitAll()
        assertEquals(8_000, viewModel.state.value)
    }

    @Test
    fun eventsWaitForAConsumerAndConsumedEventsAreNotReplayed() = runTest(dispatcher) {
        viewModel.onIntent(FixtureIntent.Notify("first"))
        runCurrent()
        assertEquals("first", viewModel.events.first())
        viewModel.onIntent(FixtureIntent.Notify("second"))
        runCurrent()
        assertEquals("second", viewModel.events.first())
    }

    @Test
    fun fullEventBufferSuspendsProducerUntilAConsumerReceivesAnEvent() = runTest(dispatcher) {
        repeat(65) { viewModel.onIntent(FixtureIntent.Notify(it.toString())) }
        runCurrent()
        assertEquals(64, viewModel.completedEmissions)
        assertEquals("0", viewModel.events.first())
        runCurrent()
        assertEquals(65, viewModel.completedEmissions)
    }

    @Test
    fun clearingViewModelCancelsItsWorkAndDiscardsQueuedEvents() = runTest(dispatcher) {
        viewModel.onIntent(FixtureIntent.Notify("queued"))
        viewModel.onIntent(FixtureIntent.Wait)
        runCurrent()
        store.clear()
        runCurrent()
        assertTrue(viewModel.operationCancelled)
        assertTrue(runCatching { viewModel.events.first() }.exceptionOrNull() is CancellationException)
    }
}

private sealed interface FixtureIntent {
    data class Add(val amount: Int) : FixtureIntent
    data class Notify(val message: String) : FixtureIntent
    data object Wait : FixtureIntent
}

private class FixtureViewModel : BaseViewModel<Int, FixtureIntent, String>() {
    private val initialValues = listOf(0)
    var initialStateCreations = 0
        private set
    var processedIntents = 0
        private set
    var completedEmissions = 0
        private set
    var operationCancelled = false
        private set

    override fun createInitialState(): Int {
        initialStateCreations++
        return initialValues.single()
    }

    override fun handleIntent(intent: FixtureIntent) {
        processedIntents++
        when (intent) {
            is FixtureIntent.Add    -> updateState { it + intent.amount }
            is FixtureIntent.Notify -> {
                viewModelScope.launch {
                    emitEvent(intent.message)
                    completedEmissions++
                }
            }

            FixtureIntent.Wait      -> {
                viewModelScope.launch {
                    try {
                        awaitCancellation()
                    } finally {
                        operationCancelled = true
                    }
                }
            }
        }
    }

    fun increment() {
        updateState { it + 1 }
    }
}
