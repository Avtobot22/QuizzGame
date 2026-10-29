plugins {
    alias(libs.plugins.quizz.android.tv.application)
}

android {
    namespace = "com.quizzgame.tv"
    defaultConfig { applicationId = "com.quizzgame.tv" }
}

dependencies {
    implementation(projects.core.session)
    implementation(projects.core.designsystem.tv)
    debugImplementation(projects.core.network)
    debugImplementation(libs.androidmock.core)
    debugImplementation(libs.androidmock.okhttp.server)
    testDebugImplementation(libs.junit)
}
