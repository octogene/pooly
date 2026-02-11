import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.apollo)
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
    implementation(project(":common:core"))
    implementation(libs.logback.classic)
    implementation(platform(libs.arrow.stack))
    implementation(libs.arrow.core)
    implementation(libs.arrow.coroutines)
    implementation(libs.arrow.resilience)
    implementation(libs.apollo.runtime)
    testImplementation(libs.junit)
}

apollo {
    service("service") {
        packageName.set("dev.octogene.pooly.thegraph")
        mapScalarToKotlinString("BigInt")
        mapScalarToKotlinString("Bytes")

        introspection {
            endpointUrl.set("https://api.studio.thegraph.com/query/41211/pt-v5-base/version/latest")
            schemaFile.set(file("src/main/graphql/schema.base.graphqls"))
        }
    }
}
