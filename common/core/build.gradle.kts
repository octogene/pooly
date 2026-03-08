import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.gradle.buildconfig)
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_24
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.arrow.stack))
    implementation(libs.arrow.core)
    api(libs.kotlinx.datetime)
}

fun getSecret(key: String, envVar: String): String =
    gradleLocalProperties(rootProject.rootDir, providers).getProperty(key)
        ?: System.getenv(envVar)
        ?: error("Missing $key")

buildConfig {
    packageName("dev.octogene.pooly.common.core")
    useKotlinOutput { internalVisibility = false }
    buildConfigField("ALCHEMY_KEY", getSecret("alchemy.key", "ALCHEMY_KEY"))
    buildConfigField("POOLY_USER", getSecret("pooly.user", "POOLY_USER"))
    buildConfigField("POOLY_PASSWORD", getSecret("pooly.password", "POOLY_PASSWORD"))
}
