package com.quizzgame.tv.dashboard.fixture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

internal class FixtureEntryViewModel : ViewModel() {
    var counter by mutableIntStateOf(0)
        private set
    fun increment() { counter++ }
}
