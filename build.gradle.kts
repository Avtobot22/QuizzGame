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
    exclude("docs/**", "external/android-mock-server/**", "**/build/**", "**/.*/**")
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
        ":core:network:lint",
        ":core:network:testDebugUnitTest",
        ":core:network:testReleaseUnitTest",
        ":core:network:assembleDebug",
        ":core:network:assembleRelease",
        ":core:session:lint",
        ":core:session:testDebugUnitTest",
        ":core:session:testReleaseUnitTest",
        ":core:session:assembleDebug",
        ":core:session:assembleRelease",
        ":core:session:assembleDebugAndroidTest",
        ":core:error:foundation:lint",
        ":core:error:foundation:testDebugUnitTest",
        ":core:error:foundation:testReleaseUnitTest",
        ":core:error:foundation:assembleDebug",
        ":core:error:foundation:assembleRelease",
        ":core:error:ui-common:lint",
        ":core:error:ui-common:testDebugUnitTest",
        ":core:error:ui-common:testReleaseUnitTest",
        ":core:error:ui-common:assembleDebug",
        ":core:error:ui-common:assembleRelease",
        ":core:error:ui-mobile:lint",
        ":core:error:ui-mobile:testDebugUnitTest",
        ":core:error:ui-mobile:testReleaseUnitTest",
        ":core:error:ui-mobile:assembleDebug",
        ":core:error:ui-mobile:assembleRelease",
        ":core:error:ui-mobile:assembleDebugAndroidTest",
        ":core:error:ui-tv:lint",
        ":core:error:ui-tv:testDebugUnitTest",
        ":core:error:ui-tv:testReleaseUnitTest",
        ":core:error:ui-tv:assembleDebug",
        ":core:error:ui-tv:assembleRelease",
        ":core:error:ui-tv:assembleDebugAndroidTest",
        ":core:designsystem:mobile:lint",
        ":core:designsystem:mobile:testDebugUnitTest",
        ":core:designsystem:mobile:assembleDebugAndroidTest",
        ":core:designsystem:tv:lint",
        ":core:designsystem:tv:testDebugUnitTest",
        ":core:designsystem:tv:assembleDebugAndroidTest",
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
