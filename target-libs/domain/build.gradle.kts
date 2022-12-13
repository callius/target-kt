@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    id("config.publish")
}

version = libs.versions.target.get()

dependencies {
    implementation(libs.arrow.core)
    testImplementation(libs.junitJupiterApi)
    testRuntimeOnly(libs.junitJupiterEngine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
