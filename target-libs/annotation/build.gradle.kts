@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.target-kt.target-gradle-config-publish")
}

version = libs.versions.target.get()

java {
    withSourcesJar()
}

dependencies {
    implementation(project(":target-core"))
}
