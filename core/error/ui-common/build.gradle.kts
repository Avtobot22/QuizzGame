plugins {
    alias(libs.plugins.quizz.android.shared.library)
}

android {
    namespace = "com.quizzgame.core.error.ui.common"
}

dependencies {
    api(projects.core.error.foundation)
    api(projects.core.ui)
}
