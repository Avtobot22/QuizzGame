plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.quizzgame.core.session"
}

// Host tests need JVM file operations: Android stubs report SDK_INT = 0 and select legacy renameTo.
configurations.matching { it.name.endsWith("UnitTestRuntimeClasspath") }.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("androidx.datastore:datastore-core-android"))
            .using(module("androidx.datastore:datastore-core-jvm:${libs.versions.datastore.get()}"))
    }
}

dependencies {
    api(projects.core.network)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.tink)
    implementation(libs.tink.android)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
