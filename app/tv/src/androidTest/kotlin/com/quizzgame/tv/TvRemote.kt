package com.quizzgame.tv

import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.Matcher

internal object TvRemote {
    fun press(keyCode: Int) {
        onView(isRoot()).perform(pressKey(keyCode))
    }

    fun pressBeforeNextFrame(vararg keyCodes: Int) {
        onView(isRoot()).perform(KeyBurst(keyCodes))
    }

    private class KeyBurst(private val keyCodes: IntArray) : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()

        override fun getDescription(): String = "dispatch remote key burst before the next frame"

        override fun perform(uiController: UiController, view: View) {
            val target = checkNotNull(view.findFocus()) { "The TV component must have focus before key input" }
            keyCodes.forEach { keyCode ->
                val eventTime = SystemClock.uptimeMillis()
                target.dispatchKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0))
                target.dispatchKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0))
            }
        }
    }
}
