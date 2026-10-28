import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.detekt)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt/detekt.yml"))
    source.setFrom(files(rootDir))
    basePath = rootDir.absolutePath
    parallel = true
    ignoreFailures = false
    autoCorrect = false
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

tasks.withType<Detekt>().configureEach {
    include("**/*.kt", "**/*.kts")
    exclude("docs/**", "**/build/**", "**/.*/**")
    jvmTarget = "17"
    reports {
        html.required.set(true)
        sarif.required.set(true)
        xml.required.set(false)
        md.required.set(false)
    }
}

tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs static analysis, unit tests, and builds both Android applications."
    dependsOn(
        "detekt",
        ":core:network:check",
        ":core:designsystem:mobile:lint",
        ":core:designsystem:mobile:testDebugUnitTest",
        ":core:designsystem:tv:lint",
        ":core:designsystem:tv:testDebugUnitTest",
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
