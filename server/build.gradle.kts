import buildlogic.DownloadArtifactTask
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

        jib {
            to {
                image = "pooly"
            }
            from {
                image = "amazoncorretto:24-headless"
            }
            extraDirectories {
                paths {
                    path {
                        setFrom(layout.buildDirectory.dir("tmp/javaagent"))
                        into = "/extras"
                    }
                }
            }
        }

        val javaToolOptions =
            application.applicationDefaultJvmArgs +
                "-javaagent:/extras/opentelemetry-javaagent.jar" +
                "-javaagent:/extras/pyroscope.jar"

        val pyroscope = dockerCompose.servicesInfos["pyroscope"]?.host ?: "pyroscope"
        val loki = dockerCompose.servicesInfos["loki"]?.host ?: "loki"
        val otel = dockerCompose.servicesInfos["otel-collector"]?.host ?: "otel-collector"

        environmentVariable("JAVA_TOOL_OPTIONS", javaToolOptions.joinToString(" "))
        environmentVariable("OTEL_RESOURCE_ATTRIBUTES", "service.name=pooly")
        environmentVariable("OTEL_JAVAAGENT_DEBUG", "true")
        environmentVariable("OTEL_JAVAAGENT_LOGGING", "application")
        environmentVariable("OTEL_EXPORTER_OTLP_ENDPOINT", "http://$otel:4318")
        environmentVariable("OTEL_RESOURCE_ATTRIBUTES", "service.namespace=pooly,service.name=pooly")
        environmentVariable("PYROSCOPE_APPLICATION_NAME", "dev.octogene.pooly")
        environmentVariable("PYROSCOPE_SERVER_ADDRESS", "http://$pyroscope:4040")
        environmentVariable("PYROSCOPE_FORMAT", "jfr")
        environmentVariable("PYROSCOPE_PROFILING_INTERVAL", "10ms")
        environmentVariable("PYROSCOPE_PROFILER_EVENT", "itimer")
        environmentVariable("PYROSCOPE_PROFILER_LOCK", "10ms")
        environmentVariable("PYROSCOPE_PROFILER_ALLOC", "512k")
        environmentVariable("AWS_REGION", "us-east-1")
        environmentVariable("LISTEN_ADDRESS", "0.0.0.0:8080")
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
// tasks.processResources {
//    dependsOn("buildOpenApi")
// }

dependencies {
    implementation(project(":common:core"))
    implementation(project(":common:db"))
    implementation(libs.argon2)
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.serialization.kotlinx.protobuf)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.arrow.raise.ktor.server)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.logback.classic)
    implementation(libs.logback.loki.appender)
    implementation(libs.cohort.ktor)
    implementation(platform(libs.arrow.stack))
    implementation(libs.arrow.core)
    implementation(libs.arrow.core.serialization)
    implementation(libs.arrow.coroutines)
    implementation(libs.arrow.suspendapp.ktor)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.bundles.exposed)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotest.core)
    testImplementation(libs.kotest.extensions.ktor)

}

tasks.named("setupJibLocal") {
    dependsOn("downloadOTELAgent")
    dependsOn("downloadPyroscopeAgent")
}

tasks.register<DownloadArtifactTask>("downloadOTELAgent") {
    description = "Downloads the AWS OTEL Java agent artifact"
    group = "custom"
    val agentVersion = "v2.20.0"
    artifactUrl = "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/$agentVersion/opentelemetry-javaagent.jar"
    val outputDir = layout.buildDirectory.dir("tmp/javaagent")
    outputFile = outputDir.get().file("opentelemetry-javaagent.jar")
}

tasks.register<DownloadArtifactTask>("downloadPyroscopeAgent") {
    description = "Downloads the Pyroscope Java agent artifact"
    group = "custom"
    val agentVersion = "v2.1.2"
    artifactUrl = "https://github.com/grafana/pyroscope-java/releases/download/$agentVersion/pyroscope.jar"
    val outputDir = layout.buildDirectory.dir("tmp/javaagent")
    outputFile = outputDir.get().file("pyroscope.jar")
}
