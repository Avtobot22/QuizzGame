plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

configureAndroidApplication()
addCatalogDependency("implementation", "androidx-tv-material")
