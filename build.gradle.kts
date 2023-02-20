// TODO: Remove when the issue is closed: https://github.com/gradle/gradle/issues/22797
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

val dokkaPluginId = libs.plugins.dokka.get().pluginId
allprojects {
    group = property("projects.group").toString()

    repositories {
        mavenCentral()
    }

    apply(plugin = dokkaPluginId)
}
