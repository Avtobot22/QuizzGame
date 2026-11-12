package com.quizzgame.buildlogic.android

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("com.google.dagger.hilt.android")
            dependencies.add("implementation", libs.hilt.android)
            dependencies.add("implementation", libs.androidx.annotation.experimental)
            dependencies.add("ksp", libs.hilt.compiler)
        }
    }
}
