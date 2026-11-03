package com.quizzgame.buildlogic.android.mobile

import com.quizzgame.buildlogic.android.AndroidSharedLibraryConventionPlugin
import com.quizzgame.buildlogic.android.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class AndroidMobileLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(AndroidSharedLibraryConventionPlugin::class.java)
            val api = configurations.getByName(JavaPlugin.API_CONFIGURATION_NAME)
            dependencies.add(api.name, libs.androidx.compose.material3)
        }
    }
}
