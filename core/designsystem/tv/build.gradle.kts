plugins {
    id("quizz.android.tv.library")
}

android {
    namespace = "com.quizzgame.core.designsystem.tv"
}

dependencies {
    api(projects.core.ui)
}
