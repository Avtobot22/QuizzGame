import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(files(LibrariesForLibs::class.java.protectionDomain.codeSource.location))
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.compose.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kotlinJvmLibrary") {
            id = libs.plugins.quizz.kotlin.jvm.library.get().pluginId
            implementationClass = "KotlinJvmLibraryConventionPlugin"
        }
        register("androidMobileApplication") {
            id = libs.plugins.quizz.android.mobile.application.get().pluginId
            implementationClass = "AndroidMobileApplicationConventionPlugin"
        }
        register("androidTvApplication") {
            id = libs.plugins.quizz.android.tv.application.get().pluginId
            implementationClass = "AndroidTvApplicationConventionPlugin"
        }
        register("androidSharedLibrary") {
            id = libs.plugins.quizz.android.shared.library.get().pluginId
            implementationClass = "AndroidSharedLibraryConventionPlugin"
        }
        register("androidMobileLibrary") {
            id = libs.plugins.quizz.android.mobile.library.get().pluginId
            implementationClass = "AndroidMobileLibraryConventionPlugin"
        }
        register("androidTvLibrary") {
            id = libs.plugins.quizz.android.tv.library.get().pluginId
            implementationClass = "AndroidTvLibraryConventionPlugin"
        }
    }
}
