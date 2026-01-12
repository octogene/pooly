
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.impldep.org.apache.commons.compress.harmony.pack200.PackingUtils.config
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
        plugins.apply("io.gitlab.arturbosch.detekt")

        extensions.configure<DetektExtension> {
            parallel = true
            buildUponDefaultConfig = true
            config.from(file("$rootDir/config/detekt.yml"))
            baseline = file("${rootProject.rootDir}/config/baseline.xml")
        }

        tasks.register("detektAll") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            dependsOn(tasks.withType<Detekt>().filter { !it.name.contains("Format") })
        }

        tasks.register<Detekt>("detektFormat") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            config.from(file("$rootDir/config/detekt.yml"))
            buildUponDefaultConfig = true
            autoCorrect = true
        }

        tasks.register("detektFormatAll") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            dependsOn(tasks.withType<Detekt>().filter { it.name.contains("Format") })
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
                xml.required.set(false)
                sarif.required.set(true)
                md.required.set(false)
            }
        }

        tasks.withType<Detekt>().configureEach {
            jvmTarget = "24"
        }
        tasks.withType<DetektCreateBaselineTask>().configureEach {
            jvmTarget = "24"
        }

        dependencies {
            catalog?.findLibrary("detekt.formatting")?.ifPresent {
                "detektPlugins"(it)
            }
        }
    }
}

private fun Project.checkIsRootProject() {
    check(this == rootProject) { "Plugin can only be applied to root project" }
}

private fun Task.getFamily(): Family? =
    Family.entries.firstOrNull { family ->
        name.contains(other = family.name, ignoreCase = true)
    }
