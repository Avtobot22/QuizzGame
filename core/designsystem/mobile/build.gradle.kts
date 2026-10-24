plugins {
    id("quizz.android.mobile.library")
}

android {
    namespace = "com.quizzgame.core.designsystem.mobile"
}

dependencies {
    api(projects.core.ui)
}
