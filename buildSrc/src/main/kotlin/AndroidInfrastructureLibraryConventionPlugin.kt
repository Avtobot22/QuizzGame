import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidInfrastructureLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(LibraryPlugin::class.java)
        target.configureAndroidInfrastructureLibrary()
    }
}
