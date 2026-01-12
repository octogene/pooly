import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("application")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
}

application {
    mainClass = "dev.octogene.pooly.worker.MainKt"
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_24
    }
}
