import buildlogic.DownloadArtifactTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.docker.compose)
    alias(libs.plugins.benchmark)
    alias(libs.plugins.all.open)
    alias(libs.plugins.redacted)
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

application {
    mainClass = "dev.octogene.pooly.server.ApplicationKt"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")

    applicationDefaultJvmArgs = listOf(
        "-Xms256m",
        "-Xmx512m",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseShenandoahGC",
        "-XX:ShenandoahGCMode=generational",
        "-XX:+UseCompactObjectHeaders",
        "-XX:+PrintFlagsFinal",
        "-XX:StartFlightRecording:" +
            "jdk.SafepointLatency#enabled=true, + " +
            "jdk.CPUTimeSample#enabled=true," +
            "jdk.CPUTimeSample#throttle=500/s," +
            "filename=pooly.jfr",
    )
}

ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = true
        onlyCommented = false
    }

    docker {
        customBaseImage.set("amazoncorretto:25-headless")
        localImageName.set("pooly")
        jreVersion.set(JavaVersion.VERSION_25)
        imageTag.set("${project.version}")

        jib {
            to {
                image = "pooly"
            }
            from {
                image = "amazoncorretto:25-headless"
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
        environmentVariable(
            "OTEL_RESOURCE_ATTRIBUTES",
            "service.namespace=pooly,service.name=pooly",
        )
        environmentVariable("PYROSCOPE_APPLICATION_NAME", "dev.octogene.pooly")
        environmentVariable("PYROSCOPE_SERVER_ADDRESS", "http://$pyroscope:4040")
        environmentVariable("PYROSCOPE_FORMAT", "jfr")
        environmentVariable("PYROSCOPE_PROFILING_INTERVAL", "10ms")
        environmentVariable("PYROSCOPE_PROFILER_EVENT", "itimer")
        environmentVariable("PYROSCOPE_PROFILER_LOCK", "10ms")
        environmentVariable("PYROSCOPE_PROFILER_ALLOC", "512k")
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

dependencies {
    implementation(project(":common:core"))
    implementation(project(":common:db"))
    implementation(project(":common:cache"))
    implementation(project(":common:backend"))
    implementation(libs.argon2)
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.server.routing.openapi)
    implementation(libs.ktor.server.openapi)
    implementation(libs.arrow.raise.ktor.server)
    implementation(libs.cohort.ktor)
    implementation(platform(libs.arrow.stack))
    implementation(libs.arrow.core)
    implementation(libs.arrow.core.serialization)
    implementation(libs.arrow.coroutines)
    implementation(libs.arrow.suspendapp.ktor)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.bundles.exposed)
    implementation(libs.bundles.server.logs)
    implementation(libs.ktor.server.auth.api.key)
    implementation(libs.kotlinx.coroutines.reactive)
    implementation(libs.kotlinx.coroutines.core)
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
    val agentVersion = "v2.24.0"
    artifactUrl =
        "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/$agentVersion/opentelemetry-javaagent.jar"
    val outputDir = layout.buildDirectory.dir("tmp/javaagent")
    outputFile = outputDir.get().file("opentelemetry-javaagent.jar")
}

tasks.register<DownloadArtifactTask>("downloadPyroscopeAgent") {
    description = "Downloads the Pyroscope Java agent artifact"
    group = "custom"
    val agentVersion = "v2.1.2"
    artifactUrl =
        "https://github.com/grafana/pyroscope-java/releases/download/$agentVersion/pyroscope.jar"
    val outputDir = layout.buildDirectory.dir("tmp/javaagent")
    outputFile = outputDir.get().file("pyroscope.jar")
}

dockerCompose.isRequiredBy(tasks["runDocker"])

dockerCompose {
    useComposeFiles.addAll("../docker-compose.yml")
    startedServices.addAll(
        "pooly-worker",
        "otel-collector",
        "grafana",
        "loki",
        "pyroscope",
        "prometheus",
        "tempo",
    )
}

// workaround for Jib https://github.com/GoogleContainerTools/jib/issues/3132 and runDocker
tasks.filter { it.name in setOf("jibDockerBuild", "jibBuildTar", "jib", "runDocker") }.onEach {
    it.notCompatibleWithConfigurationCache("Jib & runDocker are not compatible with configuration cache")
}
