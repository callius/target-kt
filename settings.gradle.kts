@file:Suppress("SpellCheckingInspection")

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "target"

include("target-core")
project(":target-core").projectDir = File("target-libs/core")

include("target-annotation")
project(":target-annotation").projectDir = File("target-libs/annotation")

include("target-annotation-processor")
project(":target-annotation-processor").projectDir = File("target-libs/annotation-processor")

includeBuild("gradle-plugins/config-kotlin")
includeBuild("gradle-plugins/config-publish")

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            version("target", extra["target.version"].toString())

            version("kotlin", "1.9.0")
            version("arrow", "1.1.5")
            version("junit", "5.9.3")
            version("kotlinpoet", "1.14.2")
            version("ksp", "1.9.0-1.0.11")

            library("arrow-core", "io.arrow-kt", "arrow-core").versionRef("arrow")
            library("junitJupiterApi", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
            library("junitJupiterEngine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
            library("kotlin-stdlibCommon", "org.jetbrains.kotlin", "kotlin-stdlib-common").versionRef("kotlin")
            library("kotlin-stdlibJdk8", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")
            library("kotlin-stdlibJs", "org.jetbrains.kotlin", "kotlin-stdlib-js").versionRef("kotlin")
            library("kotlinpoet", "com.squareup", "kotlinpoet").versionRef("kotlinpoet")
            library("kotlinpoet-ksp", "com.squareup", "kotlinpoet-ksp").versionRef("kotlinpoet")
            library("kotlinx-datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.4.0")
            library("ksp", "com.google.devtools.ksp", "symbol-processing-api").versionRef("ksp")

            plugin("dokka", "org.jetbrains.dokka").version("1.8.20")
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("kotlin-multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
        }
    }
}
