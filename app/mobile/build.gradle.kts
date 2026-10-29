plugins {
    alias(libs.plugins.quizz.android.mobile.application)
}

android {
    namespace = "com.quizzgame.mobile"
    defaultConfig { applicationId = "com.quizzgame.mobile" }
}

dependencies {
    implementation(projects.core.session)
    implementation(projects.core.designsystem.mobile)
    debugImplementation(projects.core.network)
    debugImplementation(libs.androidmock.core)
    debugImplementation(libs.androidmock.okhttp.server)
    testDebugImplementation(libs.junit)
}
