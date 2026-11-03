import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(files(LibrariesForLibs::class.java.protectionDomain.codeSource.location))
    implementation(libs.bundles.build.plugins)
}

gradlePlugin {
    plugins {
        register("androidHilt") {
            id = libs.plugins.quizz.android.hilt.get().pluginId
            implementationClass = "com.quizzgame.buildlogic.android.AndroidHiltConventionPlugin"
        }
        register("androidInfrastructureLibrary") {
            id = libs.plugins.quizz.android.infrastructure.library.get().pluginId
            implementationClass = "com.quizzgame.buildlogic.android.AndroidInfrastructureLibraryConventionPlugin"
        }
        register("kotlinJvmLibrary") {
            id = libs.plugins.quizz.kotlin.jvm.library.get().pluginId
            implementationClass = "com.quizzgame.buildlogic.jvm.KotlinJvmLibraryConventionPlugin"
        }
        register("androidMobileApplication") {
            id = libs.plugins.quizz.android.mobile.application.get().pluginId
            implementationClass = "com.quizzgame.buildlogic.android.mobile.AndroidMobileApplicationConventionPlugin"
        }
        register("androidTvApplication") {
            id = libs.plugins.quizz.android.tv.application.get().pluginId
            implementationClass = "com.quizzgame.buildlogic.android.tv.AndroidTvApplicationConventionPlugin"
        }
        register("androidSharedLibrary") {
            id = libs.plugins.quizz.android.shared.library.get().pluginId
            implementationClass = "com.quizzgame.buildlogic.android.AndroidSharedLibraryConventionPlugin"
        }
        register("androidMobileLibrary") {
            id = libs.plugins.quizz.android.mobile.library.get().pluginId
            implementationClass = "com.quizzgame.buildlogic.android.mobile.AndroidMobileLibraryConventionPlugin"
        }
        register("androidTvLibrary") {
            id = libs.plugins.quizz.android.tv.library.get().pluginId
            implementationClass = "com.quizzgame.buildlogic.android.tv.AndroidTvLibraryConventionPlugin"
        }
    }
}
