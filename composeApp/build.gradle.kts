plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ethers.abigen)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.stability.analyzer)
    alias(libs.plugins.metro)
}

android {
    namespace = "dev.octogene.pooly"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.octogene.pooly"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
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
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    kotlin {
        jvmToolchain(24)
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_24
            targetCompatibility = JavaVersion.VERSION_24
        }
//        compilerOptions {
//            freeCompilerArgs.add("-Xexplicit-backing-fields")
//        }
    }

    sourceSets {
        val main = getByName("main")
        main.kotlin.srcDirs(file("build/generated/source/ethers/main/kotlin"))
    }

    composeCompiler {
        stabilityConfigurationFiles.add(
            rootProject.layout.projectDirectory.file("compose_stability.conf")
        )
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("dev.octogene.pooly.db")
            dependency(project(":settings"))
            dependency(project(":pooltogether"))
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":pooltogether"))
    implementation(project(":settings"))
    // Glance
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Activity
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.workmanager)

    // Ethers
    implementation(project.dependencies.platform(libs.ethers.bom))
    implementation(libs.ethers.core)
    implementation(libs.ethers.providers)
    implementation(libs.ethers.abi)
    implementation(libs.ethers.signers)

    // Datastore
    implementation(libs.sqldelight.android)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.viewmodel.nav3)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    // Navigation
    implementation(libs.androidx.nav3.ui)

    // Adaptive
    implementation(libs.androidx.material3.adaptive)
    implementation(libs.androidx.material3.adaptive.nav3)

    // Metro
    implementation(libs.metrox)
    implementation(libs.metrox.viewmodel.compose)

    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}
