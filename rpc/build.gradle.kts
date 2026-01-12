plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.metro)
    alias(libs.plugins.ethers.abigen)
}

android {
    namespace = "dev.octogene.pooly.rpc"
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
    sourceSets {
        getByName("main") {
            // As ethers-kt does not add its generated code to the proper sourceSets
            kotlin.srcDir("build/generated/source/ethers/main/kotlin")
        }
    }
}

dependencies {
    implementation(project(":shared"))
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
