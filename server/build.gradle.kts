import io.ktor.plugin.OpenApiPreview
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.docker.compose)
}

application {
    mainClass = "dev.octogene.pooly.server.ApplicationKt"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")

    applicationDefaultJvmArgs = listOf(
        "-Xms256m",
        "-Xmx512m",
        "-XX:+PrintFlagsFinal",
        "-XX:StartFlightRecording:" +
            "filename=pooly.jfr"
    )
}

ktor {
    @OptIn(OpenApiPreview::class)
    // Two issues:
    // - Generation should work with 2.3.0 once KTOR-9130 resolved
    // - The preview runs on a isolated compilation which ignores serialization which
    //  causes issues for the resolution of the config.
    // Should be usable with 3.4.0
    openApi {
        title = "Pooly"
        contact = "me@octogene.dev"
        summary = "Service for the Pooly mobile application"
        version = "${project.version}"
        license = "MIT"
    }

    docker {
        customBaseImage.set("amazoncorretto:24-headless")
        localImageName.set("pooly")
        jreVersion.set(JavaVersion.VERSION_24)
        imageTag.set("${project.version}")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

kotlin {
    jvmToolchain(24)

    compilerOptions {
        jvmTarget = JvmTarget.JVM_24
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

// Once buildOpenApi works
//tasks.processResources {
//    dependsOn("buildOpenApi")
//}

dependencies {
    implementation(project(":common:core"))
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.serialization.kotlinx.protobuf)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.arrow.raise.ktor.server)
    implementation(libs.logback.classic)
    implementation(libs.cohort.ktor)
    implementation(platform(libs.arrow.stack))
    implementation(libs.arrow.core)
    implementation(libs.arrow.coroutines)
    implementation(libs.arrow.continuations)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
}
