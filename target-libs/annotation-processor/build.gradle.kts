@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.target-kt.target-gradle-config-kotlin")
    id("io.target-kt.target-gradle-config-publish")
}

version = libs.versions.target.get()

java {
    withSourcesJar()
}

dependencies {
    implementation(project(":target-annotation"))
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.ksp)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}
