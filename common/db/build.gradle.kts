import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
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
    implementation(project(":common:backend"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.bundles.exposed)
    implementation(libs.exposed.migration.core)
    implementation(libs.exposed.migration.jdbc)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.flyway)
    implementation(libs.flyway.postgresql)
    implementation(platform(libs.arrow.stack))
    implementation(libs.arrow.core)
}

// TODO: Build a migration plugin
//  or wait for EXPOSED-755 (Gradle migration plugin)
tasks.register<JavaExec>("generateMigrationScript") {
    group = "application"
    description = "Generate a migration script in the path src/main/resources/migrations"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "dev.octogene.pooly.common.db.migration.GenerateMigrationScriptKt"

    systemProperty("db.url", project.findProperty("db.url"))
    systemProperty("db.user", project.findProperty("db.user"))
    systemProperty("db.password", project.findProperty("db.password") ?: "")
    systemProperty("migration.name", project.findProperty("migration.name") ?: "migration")
    systemProperty("migration.tables", project.findProperty("migration.tables"))
}
