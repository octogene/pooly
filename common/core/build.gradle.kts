import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
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
    implementation(libs.kotlinx.datetime)
}
