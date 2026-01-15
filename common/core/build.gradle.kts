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

buildConfig {
    packageName("dev.octogene.pooly.common.core")
    useKotlinOutput { internalVisibility = false }
    buildConfigField(
        "ALCHEMY_KEY",
        gradleLocalProperties(rootProject.rootDir, providers).getProperty("alchemy.key")
    )
    buildConfigField(
        "POOLY_USER",
        gradleLocalProperties(rootProject.rootDir, providers).getProperty("pooly.user")
    )
    buildConfigField(
        "POOLY_PASSWORD",
        gradleLocalProperties(rootProject.rootDir, providers).getProperty("pooly.password")
    )
}
