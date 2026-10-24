plugins {
    id("quizz.android.mobile.application")
}

android {
    namespace = "com.quizzgame.mobile"
    defaultConfig { applicationId = "com.quizzgame.mobile" }
}

dependencies {
    implementation(projects.core.designsystem.mobile)
}
