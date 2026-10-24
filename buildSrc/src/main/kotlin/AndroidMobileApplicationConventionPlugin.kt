import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class AndroidMobileApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(AndroidApplicationConventionPlugin::class.java)
            val implementation = configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
            dependencies.add(implementation.name, libs.androidx.compose.material3)
        }
    }
}
