package com.quizzgame.buildlogic.android.tv

import com.quizzgame.buildlogic.android.AndroidApplicationConventionPlugin
import com.quizzgame.buildlogic.android.libs
import com.quizzgame.buildlogic.app.configureAppFoundation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class AndroidTvApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(AndroidApplicationConventionPlugin::class.java)
            configureAppFoundation("tv")
            val implementation = configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
            dependencies.add(implementation.name, libs.androidx.tv.material)
        }
    }
}
