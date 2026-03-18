import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.internal.builtins.StandardNames.FqNames.target

class KotlinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            val kmpExtension = target.extensions.getByType<KotlinMultiplatformExtension>()
        }
        target.plugins.withId("org.jetbrains.kotlin.android") {
            val androidExtension = target.extensions.getByType<KotlinAndroidProjectExtension>()
            target.configureKotlinAndroidCompilerArgs()
        }
        target.plugins.withId("org.jetbrains.kotlin.jvm") {
            val jvmExtension = target.extensions.getByType<KotlinJvmProjectExtension>()
            target.configureKotlinJvmCompilerArgs()
        }
    }

    private fun Project.configureKotlinJvmCompilerArgs() {
        configureKotlin<KotlinJvmProjectExtension>()
    }

    private fun Project.configureKotlinAndroidCompilerArgs() {
        configureKotlin<KotlinAndroidProjectExtension>()
        configure<KotlinAndroidProjectExtension> {
            jvmToolchain(24)
            compilerOptions {
                freeCompilerArgs.add("-Xexplicit-backing-fields")
            }
        }
    }

    internal inline fun <reified T : KotlinBaseExtension> Project.configureKotlin() = configure<T> {
        when (this) {
            is KotlinAndroidProjectExtension -> compilerOptions
            is KotlinJvmProjectExtension -> compilerOptions
            else -> throw Exception("Unsupported Kotlin project type")
        }.apply {
            jvmTarget.set(JvmTarget.JVM_24)

            freeCompilerArgs.addAll(
                "-Xcontext-parameters",
                "-Xreturn-value-checker=check",
                "-Xname-based-destructuring=complete"
            )
        }
    }
}
