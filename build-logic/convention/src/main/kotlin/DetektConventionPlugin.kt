
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.konan.target.Family

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.setupDetekt()
    }
}

private fun Project.setupDetekt() {
    checkIsRootProject()

    val catalog = versionCatalog

    allprojects {
        plugins.apply("dev.detekt")

        extensions.configure<DetektExtension> {
            parallel.set(true)
            buildUponDefaultConfig.set(true)
            config.from(file("$rootDir/config/detekt.yml"))
            baseline.set(file("${rootProject.rootDir}/config/baseline.xml"))
        }

        tasks.register("detektAll") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            dependsOn(tasks.withType<Detekt>().filter { !it.name.contains("Format") })
        }

        tasks.register<Detekt>("detektFormat") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            config.from(file("$rootDir/config/detekt.yml"))
            buildUponDefaultConfig.set(true)
            autoCorrect.set(true)
        }

        tasks.register("detektFormatAll") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            dependsOn(tasks.withType<Detekt>().filter { it.name == "detektFormat" })
        }

        tasks.configureEach {
            if (name == "build") {
                dependsOn("detektAll")
            } else if (name.startsWith("detekt")) {
                enabled = getFamily()?.isCompilationAllowed() != false
                logger.info("Detekt $this, enabled: $enabled")
            }
        }

        tasks.withType<Detekt>().configureEach {
            setSource(files(projectDir))
            include("**/*.kt", "**/*.kts")
            exclude("**/resources/**", "**/build/**", ".gradle")

            reports {
                html.required.set(false)
                sarif.required.set(true)
                markdown.required.set(false)
            }
        }

        tasks.withType<Detekt>().configureEach {
            jvmTarget.set("25")
        }
        tasks.withType<DetektCreateBaselineTask>().configureEach {
            jvmTarget.set("25")
        }

        dependencies {
            catalog?.findLibrary("detekt-rules-ktlint-wrapper")?.ifPresent {
                "detektPlugins"(it)
            }
        }
    }
}

private fun Project.checkIsRootProject() {
    check(this == rootProject) { "Plugin can only be applied to root project" }
}

private fun Task.getFamily(): Family? = Family.entries.firstOrNull { family ->
    name.contains(other = family.name, ignoreCase = true)
}
