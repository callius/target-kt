@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    signing
}

version = libs.versions.target.get()

dependencies {
    implementation(project(":domain"))
}
