import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("application")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.jib)
}

application {
    mainClass = "dev.octogene.pooly.worker.MainKt"
}

jib {
    to {
        image = "pooly-worker"
    }
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
    implementation(project(":rpc"))
    implementation(project(":thegraph"))
    implementation(libs.h2)
    implementation(platform(libs.arrow.stack))
    implementation(libs.arrow.suspendapp)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.logback.classic)
}
