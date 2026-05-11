plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.metro)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.gradle.buildconfig)
    alias(libs.plugins.pooly.kotlin)
}
android {
    namespace = "dev.octogene.pooly.pooltogether"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    packaging {
        resources {
            excludes.addAll(
                listOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "/META-INF/DEPENDENCIES",
                ),
            )
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("dev.octogene.pooly.pooltogether.db")
            dependency(project(":settings"))
        }
    }
}

dependencies {
    implementation(project(":common:mobile"))
    implementation(project(":settings"))
    implementation(platform(libs.arrow.stack))
    implementation(libs.arrow.core)
    implementation(libs.arrow.resilience.ktor.client)
    implementation(libs.androidx.paging3.runtime)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.androidx.paging3)
    implementation(libs.androidx.workmanager)
}

buildConfig {
    packageName("dev.octogene.pooly.pooltogether")
    useKotlinOutput { internalVisibility = true }
    buildConfigField(
        "POOLY_BASE_URL",
        System.getenv("POOLY_BASE_URL") ?: "http://10.0.2.2:8080/api/v1",
    )
}
