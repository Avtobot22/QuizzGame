import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    // AGP's built-in Kotlin uses this version without applying the Android Kotlin plugin.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt/detekt.yml"))
    source.setFrom(files("app", "core", "build.gradle.kts", "settings.gradle.kts"))
    parallel = true
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

tasks.withType<Detekt>().configureEach {
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**")
    reports {
        html.required.set(true)
        sarif.required.set(true)
    }
}

tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs static analysis, unit tests, and builds both Android applications."
    dependsOn(
        "detekt",
        ":core:ui:lint",
        ":core:ui:testDebugUnitTest",
        ":app:mobile:lint",
        ":app:mobile:testDebugUnitTest",
        ":app:mobile:assembleDebug",
        ":app:mobile:assembleRelease",
        ":app:mobile:assembleDebugAndroidTest",
        ":app:tv:lint",
        ":app:tv:testDebugUnitTest",
        ":app:tv:assembleDebug",
        ":app:tv:assembleRelease",
        ":app:tv:assembleDebugAndroidTest",
    )
}
