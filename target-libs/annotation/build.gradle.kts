@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
}

version = "0.1.0"

dependencies {
    implementation(project(":domain"))
}
