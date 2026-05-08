plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ethers.abigen)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.stability.analyzer)
    alias(libs.plugins.metro)
    alias(libs.plugins.pooly.kotlin)
    alias(libs.plugins.gradle.buildconfig)
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
                    "/META-INF/DEPENDENCIES",
                ),
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
                "proguard-rules.pro",
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

    sourceSets.named("main") {
        kotlin.directories += "build/generated/source/ethers/main/kotlin"
    }

    composeCompiler {
        stabilityConfigurationFiles.add(
            rootProject.layout.projectDirectory.file("stability_config.conf"),
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
    implementation(project(":common:mobile"))
    implementation(project(":pooltogether"))
    implementation(project(":settings"))
    implementation(project(":login"))
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

    // Paging3
    implementation(libs.androidx.paging3.runtime)
    implementation(libs.androidx.paging3.compose)

    // Navigation
    implementation(libs.androidx.nav3.ui)

    // Adaptive
    implementation(libs.androidx.material3.adaptive)
    implementation(libs.androidx.material3.adaptive.nav3)

    // Metro
    implementation(libs.metrox)
    implementation(libs.metrox.viewmodel.compose)

    // OTEL
    implementation(libs.otel.android.agent)

    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}

buildConfig {
    packageName("dev.octogene.pooly.app")
    useKotlinOutput { internalVisibility = true }
    buildConfigField(
        "OTEL_BASE_URL",
        System.getenv("OTEL_BASE_URL") ?: "http://10.0.2.2:4318",
    )
}

composeStabilityAnalyzer {

    stabilityValidation {
        enabled.set(true) // Enable or disable stability validation
        outputDir.set(layout.projectDirectory.dir("stability")) // set the output directory
        includeTests.set(false) // Exclude test code from stability reports (default)

        // Exclude specific sub-projects/modules (useful for multi-module projects)
        ignoredProjects.set(listOf("benchmarks", "examples", "samples"))

        // Control build failure behavior on stability changes (default: true)
        failOnStabilityChange.set(true)

        // Do not report any stable changes from the baseline (default: false)
        ignoreNonRegressiveChanges.set(false)

        // Allow the check to run, even if the baseline does not exist (default: false)
        allowMissingBaseline.set(false)

        // Add stability configuration file
        // Matches compose's identical property
        // (see https://developer.android.com/develop/ui/compose/performance/stability/fix#configuration-file)
        stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
    }
}
