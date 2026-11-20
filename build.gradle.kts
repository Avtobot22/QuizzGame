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
    exclude("docs/**", "external/android-mock-server/**", "external/feature-flags/**", "**/build/**", "**/.*/**")
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
        ":shared:auth:lint",
        ":shared:room:lint",
        ":feature:auth:mobile:lint",
        ":feature:auth:tv:lint",
        ":feature:auth-provider:mobile:lint",
        ":feature:auth-provider:tv:lint",
        ":feature:dashboard:tv:lint",
        ":feature:dashboard:mobile:lint",
        ":feature:play:mobile:lint",
        ":feature:feed:mobile:lint",
        ":feature:quiz-entry:mobile:lint",
        ":shared:feed:lint",
        ":shared:quiz-entry:lint",
        ":core:media:android:lint",
        ":feature:feed:tv:lint",
        ":feature:quiz-entry:tv:lint",
        ":app:foundation:lint",
        ":app:debug-support:lintDebug",
        ":core:navigation:lint",
        ":core:network:lint",
        ":core:presentation:lint",
        ":core:feature-flags:lint",
        ":shared:splash:lint",
        ":shared:account:lint",
        ":feature:splash:tv:lint",
        ":feature:splash:mobile:lint",
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
        ":shared:auth:testDebugUnitTest",
        ":shared:room:testDebugUnitTest",
        ":feature:auth-provider:mobile:testDebugUnitTest",
        ":feature:dashboard:tv:testDebugUnitTest",
        ":feature:dashboard:mobile:testDebugUnitTest",
        ":feature:play:mobile:testDebugUnitTest",
        ":feature:feed:mobile:testDebugUnitTest",
        ":feature:quiz-entry:mobile:testDebugUnitTest",
        ":shared:feed:testDebugUnitTest",
        ":shared:quiz-entry:testDebugUnitTest",
        ":core:media:android:testDebugUnitTest",
        ":feature:feed:tv:testDebugUnitTest",
        ":feature:quiz-entry:tv:testDebugUnitTest",
        ":core:media:test",
        ":shared:catalog:test",
        ":app:foundation:testDebugUnitTest",
        ":app:debug-support:testDebugUnitTest",
        ":core:navigation:testDebugUnitTest",
        ":core:network:testDebugUnitTest",
        ":core:presentation:testDebugUnitTest",
        ":core:feature-flags:testDebugUnitTest",
        ":shared:splash:testDebugUnitTest",
        ":shared:account:testDebugUnitTest",
        ":feature:splash:tv:testDebugUnitTest",
        ":feature:splash:mobile:testDebugUnitTest",
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
        ":shared:auth:testReleaseUnitTest",
        ":shared:room:testReleaseUnitTest",
        ":feature:auth-provider:mobile:testReleaseUnitTest",
        ":feature:dashboard:tv:testReleaseUnitTest",
        ":feature:dashboard:mobile:testReleaseUnitTest",
        ":feature:play:mobile:testReleaseUnitTest",
        ":feature:feed:mobile:testReleaseUnitTest",
        ":feature:quiz-entry:mobile:testReleaseUnitTest",
        ":shared:feed:testReleaseUnitTest",
        ":shared:quiz-entry:testReleaseUnitTest",
        ":core:media:android:testReleaseUnitTest",
        ":feature:feed:tv:testReleaseUnitTest",
        ":feature:quiz-entry:tv:testReleaseUnitTest",
        ":core:media:test",
        ":shared:catalog:test",
        ":app:foundation:testReleaseUnitTest",
        ":core:navigation:testReleaseUnitTest",
        ":core:network:testReleaseUnitTest",
        ":core:presentation:testReleaseUnitTest",
        ":core:feature-flags:testReleaseUnitTest",
        ":shared:splash:testReleaseUnitTest",
        ":shared:account:testReleaseUnitTest",
        ":feature:splash:tv:testReleaseUnitTest",
        ":feature:splash:mobile:testReleaseUnitTest",
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
        ":feature:dashboard:tv:assembleDebug",
        ":feature:dashboard:mobile:assembleDebug",
        ":feature:play:mobile:assembleDebug",
        ":feature:feed:mobile:assembleDebug",
        ":feature:quiz-entry:mobile:assembleDebug",
        ":shared:feed:assembleDebug",
        ":shared:quiz-entry:assembleDebug",
        ":core:media:android:assembleDebug",
        ":feature:feed:tv:assembleDebug",
        ":feature:quiz-entry:tv:assembleDebug",
        ":app:foundation:assembleDebug",
        ":app:debug-support:assembleDebug",
        ":core:navigation:assembleDebug",
        ":core:network:assembleDebug",
        ":core:presentation:assembleDebug",
        ":core:feature-flags:assembleDebug",
        ":shared:splash:assembleDebug",
        ":shared:account:assembleDebug",
        ":feature:splash:tv:assembleDebug",
        ":feature:splash:mobile:assembleDebug",
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
        ":feature:dashboard:tv:assembleRelease",
        ":feature:dashboard:mobile:assembleRelease",
        ":feature:play:mobile:assembleRelease",
        ":feature:feed:mobile:assembleRelease",
        ":feature:quiz-entry:mobile:assembleRelease",
        ":shared:feed:assembleRelease",
        ":shared:quiz-entry:assembleRelease",
        ":core:media:android:assembleRelease",
        ":feature:feed:tv:assembleRelease",
        ":feature:quiz-entry:tv:assembleRelease",
        ":app:foundation:assembleRelease",
        ":core:navigation:assembleRelease",
        ":core:network:assembleRelease",
        ":core:presentation:assembleRelease",
        ":core:feature-flags:assembleRelease",
        ":shared:splash:assembleRelease",
        ":shared:account:assembleRelease",
        ":feature:splash:tv:assembleRelease",
        ":feature:splash:mobile:assembleRelease",
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
        ":core:media:android:assembleDebugAndroidTest",
        ":feature:feed:tv:assembleDebugAndroidTest",
        ":feature:play:mobile:assembleDebugAndroidTest",
        ":feature:feed:mobile:assembleDebugAndroidTest",
        ":feature:quiz-entry:mobile:assembleDebugAndroidTest",
        ":feature:quiz-entry:tv:assembleDebugAndroidTest",
        ":app:debug-support:assembleDebugAndroidTest",
        ":core:navigation:assembleDebugAndroidTest",
        ":core:session:assembleDebugAndroidTest",
        ":core:error:ui-mobile:assembleDebugAndroidTest",
        ":core:error:ui-tv:assembleDebugAndroidTest",
        ":core:ui:assembleDebugAndroidTest",
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
