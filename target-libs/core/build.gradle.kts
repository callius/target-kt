@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("io.target-kt.target-gradle-config-kotlin")
    id("io.target-kt.target-gradle-config-publish")
}

version = libs.versions.target.get()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlibCommon)
                implementation(libs.arrow.core)
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.kotlin.stdlibJdk8)
            }
        }
        jsMain {
            dependencies {
                implementation(libs.kotlin.stdlibJs)
            }
        }
    }
}
