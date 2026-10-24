import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(AppPlugin::class.java)
            pluginManager.apply(ComposeCompilerGradleSubplugin::class.java)
            configureAndroidApplication()
        }
    }
}
