import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.metro)
    alias(libs.plugins.gradle.buildconfig)
}

android {
    namespace = "dev.octogene.pooly.shared"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

dependencies {
    implementation(libs.androidx.workmanager)
    api(libs.kotlinx.datetime)
    api(libs.kermit)
}

buildConfig {
    packageName("dev.octogene.pooly.shared")
    useKotlinOutput { internalVisibility = false }
    buildConfigField(
        "ALCHEMY_KEY",
        gradleLocalProperties(rootProject.rootDir, providers).getProperty("alchemy.key")
    )
}
