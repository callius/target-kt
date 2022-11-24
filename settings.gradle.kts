enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "target"

include("domain")
project(":domain").projectDir = File("target-libs/domain")

include("annotation")
project(":annotation").projectDir = File("target-libs/annotation")

include("annotation-processor")
project(":annotation-processor").projectDir = File("target-libs/annotation-processor")

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.7.21")
            version("arrow", "1.1.3")
            version("junit", "5.8.1")
            version("kotlinpoet", "1.12.0")
            version("ksp", "1.7.21-1.0.8")

            library("arrow-core", "io.arrow-kt", "arrow-core").versionRef("arrow")
            library("junitJupiterApi", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
            library("junitJupiterEngine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
            library("kotlinpoet", "com.squareup", "kotlinpoet").versionRef("kotlinpoet")
            library("kotlinpoet-ksp", "com.squareup", "kotlinpoet-ksp").versionRef("kotlinpoet")
            library("ksp", "com.google.devtools.ksp", "symbol-processing-api").versionRef("ksp")

            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
        }
    }
}
