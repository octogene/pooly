import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("application")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

application {
    mainClass = "dev.octogene.pooly.infrastructure.Infrastructure"
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

dependencies {
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
    implementation(libs.pulumi)
    implementation(libs.pulumi.hcloud)
    implementation(libs.pulumi.command)
}
