import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks {
    withType<Test>().configureEach {
        maxParallelForks = Runtime.getRuntime().availableProcessors()
        useJUnitPlatform()
        testLogging {
            setExceptionFormat("full")
            setEvents(listOf("passed", "skipped", "failed", "standardOut", "standardError"))
        }
    }

    withType<JavaCompile>().configureEach {
        targetCompatibility = "${JavaVersion.toVersion(8)}"
        sourceCompatibility = "${JavaVersion.toVersion(8)}"
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

//configure<KotlinProjectExtension> { explicitApi() }

if (isKotlinMultiplatform) {
    configure<KotlinMultiplatformExtension> {
        jvm {
            // Fix JVM target ignores Java sources and compiles only Kotlin source files.
            withJava()
        }
        js(IR) {
            browser()
            nodejs()
        }

        linuxX64()

        mingwX64()

        iosArm64()
        iosSimulatorArm64()
        iosX64()
        macosArm64()
        macosX64()
        tvosArm64()
        tvosSimulatorArm64()
        tvosX64()
        watchosArm32()
        watchosArm64()
        watchosSimulatorArm64()
        watchosX64()

        sourceSets {
            val commonMain by getting
            val mingwX64Main by getting
            val linuxX64Main by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting
            val iosX64Main by getting
            val macosArm64Main by getting
            val macosX64Main by getting
            val tvosArm64Main by getting
            val tvosSimulatorArm64Main by getting
            val tvosX64Main by getting
            val watchosArm32Main by getting
            val watchosArm64Main by getting
            val watchosSimulatorArm64Main by getting
            val watchosX64Main by getting

            create("nativeMain") {
                dependsOn(commonMain)
                mingwX64Main.dependsOn(this)
                linuxX64Main.dependsOn(this)
                iosArm64Main.dependsOn(this)
                iosSimulatorArm64Main.dependsOn(this)
                iosX64Main.dependsOn(this)
                macosArm64Main.dependsOn(this)
                macosX64Main.dependsOn(this)
                tvosArm64Main.dependsOn(this)
                tvosSimulatorArm64Main.dependsOn(this)
                tvosX64Main.dependsOn(this)
                watchosArm32Main.dependsOn(this)
                watchosArm64Main.dependsOn(this)
                watchosSimulatorArm64Main.dependsOn(this)
                watchosX64Main.dependsOn(this)
            }
        }
    }
}

internal val Project.isKotlinMultiplatform: Boolean
    get() = pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")
