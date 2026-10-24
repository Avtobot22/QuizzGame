import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin

class AndroidSharedLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(LibraryPlugin::class.java)
            pluginManager.apply(ComposeCompilerGradleSubplugin::class.java)
            configureAndroidLibrary()
        }
    }
}
