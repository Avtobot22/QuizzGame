package com.quizzgame.mobile

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.internal.GeneratedComponentManager
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HiltStartupTest {
    @Test
    fun activityRecreationKeepsTheApplicationComponent() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            var initial: Any? = null
            scenario.onActivity { activity ->
                initial = component(activity.application)
                assertNotNull(initial)
                assertNotNull(component(activity))
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                assertSame(initial, component(activity.application))
                assertNotNull(component(activity))
            }
        }
    }

    private fun component(owner: Any): Any = (owner as GeneratedComponentManager<*>).generatedComponent()
}
