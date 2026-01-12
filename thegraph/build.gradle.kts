import com.android.build.gradle.ProguardFiles.getDefaultProguardFile

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.apollo)
    alias(libs.plugins.metro)
}

android {
    namespace = "dev.octogene.pooly.thegraph"
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
    implementation(project(":shared"))
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
