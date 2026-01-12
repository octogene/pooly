plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.metro)
    alias(libs.plugins.sqldelight)
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
                    "/META-INF/DEPENDENCIES"
                )
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

    kotlin {
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_24
            targetCompatibility = JavaVersion.VERSION_24
        }
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
    implementation(project(":shared"))
    implementation(project(":rpc"))
    implementation(project(":thegraph"))
    implementation(project(":settings"))
    implementation(libs.sqldelight.coroutines)
    implementation(libs.androidx.workmanager)
}
