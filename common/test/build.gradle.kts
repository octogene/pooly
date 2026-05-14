import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    `java-test-fixtures`
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
    kotlin("test")
    testFixturesApi(libs.kotest.core)
    testFixturesApi(libs.kotest.property)
    testFixturesImplementation(project(":common:core"))
}
