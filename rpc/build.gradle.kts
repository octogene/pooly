import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.ethers.abigen)
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
    implementation(project(":common:core"))
    implementation(project.dependencies.platform(libs.arrow.stack))
    implementation(libs.arrow.core)
    implementation(project.dependencies.platform(libs.ethers.bom))
    implementation(libs.ethers.core)
    implementation(libs.ethers.providers)
    implementation(libs.ethers.abi)
    implementation(libs.ethers.signers)
}

ethersAbigen {
    directorySource("src/main/abi")
    outputDir = "generated/source/ethers/main/kotlin"
}
