package com.quizzgame.tv.dashboard.demo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class DemoEntryViewModel @Inject constructor() : ViewModel() {
    var counter by mutableIntStateOf(0)
        private set
    fun increment() { counter++ }
}
