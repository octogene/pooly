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
    from {
        image = "amazoncorretto:24-headless"
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

// workaround https://github.com/GoogleContainerTools/jib/issues/3132
tasks.filter { it.name in setOf("jibDockerBuild", "jibBuildTar", "jib") }.onEach {
    it.notCompatibleWithConfigurationCache("Jib is not compatible with configuration cache")
}

dependencies {
    implementation(project(":common:core"))
    implementation(project(":common:db"))
    implementation(project(":rpc"))
    implementation(project(":thegraph"))
    implementation(libs.h2)
    implementation(libs.postgresql)
    implementation(platform(libs.arrow.stack))
    implementation(libs.arrow.suspendapp)
    implementation(libs.bundles.exposed)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.logback.classic)
}
