@file:Suppress("SpellCheckingInspection")

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
    versionCatalogs {
        create("libs") {
            version("target", extra["target.version"].toString())
        }
    }
}
