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
