package com.quizzgame.core.designsystem.tv

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.matcher.ViewMatchers.isRoot

internal object TvErrorRemote {
    fun press(keyCode: Int) {
        onView(isRoot()).perform(pressKey(keyCode))
    }
}
