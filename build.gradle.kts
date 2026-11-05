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

tasks.register("ciLint") {
    group = "verification"
    description = "Runs Android lint for all application and library modules."
    dependsOn(
        ":app:foundation:lint",
        ":app:debug-support:lintDebug",
        ":core:navigation:lint",
        ":core:network:lint",
        ":core:session:lint",
        ":core:error:foundation:lint",
        ":core:error:ui-common:lint",
        ":core:error:ui-mobile:lint",
        ":core:error:ui-tv:lint",
        ":core:designsystem:mobile:lint",
        ":core:designsystem:tv:lint",
        ":core:ui:lint",
        ":app:mobile:lint",
        ":app:tv:lint",
    )
}

tasks.register("ciUnitTestDebug") {
    group = "verification"
    description = "Runs debug JVM unit tests."
    dependsOn(
        ":app:foundation:testDebugUnitTest",
        ":app:debug-support:testDebugUnitTest",
        ":core:navigation:testDebugUnitTest",
        ":core:network:testDebugUnitTest",
        ":core:session:testDebugUnitTest",
        ":core:error:foundation:testDebugUnitTest",
        ":core:error:ui-common:testDebugUnitTest",
        ":core:error:ui-mobile:testDebugUnitTest",
        ":core:error:ui-tv:testDebugUnitTest",
        ":core:designsystem:mobile:testDebugUnitTest",
        ":core:designsystem:tv:testDebugUnitTest",
        ":core:ui:testDebugUnitTest",
        ":app:mobile:testDebugUnitTest",
        ":app:tv:testDebugUnitTest",
    )
}

tasks.register("ciUnitTestRelease") {
    group = "verification"
    description = "Runs release JVM unit tests."
    dependsOn(
        ":app:foundation:testReleaseUnitTest",
        ":core:navigation:testReleaseUnitTest",
        ":core:network:testReleaseUnitTest",
        ":core:session:testReleaseUnitTest",
        ":core:error:foundation:testReleaseUnitTest",
        ":core:error:ui-common:testReleaseUnitTest",
        ":core:error:ui-mobile:testReleaseUnitTest",
        ":core:error:ui-tv:testReleaseUnitTest",
    )
}

tasks.register("ciAssembleDebug") {
    group = "verification"
    description = "Builds debug applications and libraries."
    dependsOn(
        ":app:foundation:assembleDebug",
        ":app:debug-support:assembleDebug",
        ":core:navigation:assembleDebug",
        ":core:network:assembleDebug",
        ":core:session:assembleDebug",
        ":core:error:foundation:assembleDebug",
        ":core:error:ui-common:assembleDebug",
        ":core:error:ui-mobile:assembleDebug",
        ":core:error:ui-tv:assembleDebug",
        ":app:mobile:assembleDebug",
        ":app:tv:assembleDebug",
    )
}

tasks.register("ciAssembleRelease") {
    group = "verification"
    description = "Builds release applications and libraries."
    dependsOn(
        ":app:foundation:assembleRelease",
        ":core:navigation:assembleRelease",
        ":core:network:assembleRelease",
        ":core:session:assembleRelease",
        ":core:error:foundation:assembleRelease",
        ":core:error:ui-common:assembleRelease",
        ":core:error:ui-mobile:assembleRelease",
        ":core:error:ui-tv:assembleRelease",
        ":app:mobile:assembleRelease",
        ":app:tv:assembleRelease",
    )
}

tasks.register("ciAssembleAndroidTest") {
    group = "verification"
    description = "Builds debug instrumentation test APKs without running them."
    dependsOn(
        ":app:debug-support:assembleDebugAndroidTest",
        ":core:navigation:assembleDebugAndroidTest",
        ":core:session:assembleDebugAndroidTest",
        ":core:error:ui-mobile:assembleDebugAndroidTest",
        ":core:error:ui-tv:assembleDebugAndroidTest",
        ":core:designsystem:mobile:assembleDebugAndroidTest",
        ":core:designsystem:tv:assembleDebugAndroidTest",
        ":app:mobile:assembleDebugAndroidTest",
        ":app:tv:assembleDebugAndroidTest",
    )
}

tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs static analysis, unit tests, and builds both Android applications."
    dependsOn(
        "detekt",
        "ciLint",
        "ciUnitTestDebug",
        "ciUnitTestRelease",
        "ciAssembleDebug",
        "ciAssembleRelease",
        "ciAssembleAndroidTest",
    )
}
