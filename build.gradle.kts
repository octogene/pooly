import org.jetbrains.changelog.date
import se.bjurr.violations.gradle.plugin.ViolationsTask
import se.bjurr.violations.lib.reports.Parser

buildscript {
    dependencies {
        constraints {
            classpath("org.apache.commons:commons-compress:1.26.0") {
                because("Fixes Jib plugin conflict with older versions pulled by other plugins")
            }
        }
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.apollo) apply false
    alias(libs.plugins.ethers.abigen) apply false
    alias(libs.plugins.stability.analyzer) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.pooly.detekt)
    alias(libs.plugins.moduleGraph)
    alias(libs.plugins.jetbrains.changelog)
    alias(libs.plugins.violations)
    alias(libs.plugins.gradle.buildconfig)
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.netty" && requested.version == "4.2.5.Final") {
            useVersion("4.2.9.Final")
            because("Aligning Ktor & Lettuce netty versions")
        }
    }
}

moduleGraphConfig {
    readmePath.set("README.md")
    heading.set("### Module Graph")
}

changelog {
    version.set(project.version.toString())
    path.set(file("${projectDir.path}/CHANGELOG.md").canonicalPath)
    header.set(provider { "[${project.version}] - ${date()}" })
    headerParserRegex.set("""(\d+\.\d+)""".toRegex())
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
    lineSeparator.set("\n")
    combinePreReleases.set(true)
}

tasks.register<ViolationsTask>("violations") {
    codeClimateFile.set(file("code-climate.json"))
    with(violationConfig()) {
        folder = projectDir.path
        pattern = ".*/detekt/.*\\.sarif$"
        parser = Parser.SARIF
        reporter = "SARIF"
    }
}

tasks["detekt"].finalizedBy("violations")
