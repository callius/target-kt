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
                implementation(libs.kotlinx.datetime)
                implementation(project(":target-core"))
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
