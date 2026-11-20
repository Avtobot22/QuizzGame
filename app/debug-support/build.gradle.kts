plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
}

android { namespace = "com.quizzgame.app.debugsupport" }

dependencies {
    debugApi(projects.app.foundation)
    debugApi(libs.bundles.android.mock)
    debugImplementation(projects.shared.account)

    testDebugImplementation(libs.bundles.unit.test)
}
