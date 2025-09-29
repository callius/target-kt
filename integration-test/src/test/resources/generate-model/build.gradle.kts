plugins {
    kotlin("jvm") version "2.1.21"
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val targetVersion = "0.8.0-SNAPSHOT"

dependencies {
    implementation("io.arrow-kt:arrow-core:2.0.1")
    implementation("io.target-kt:target-annotation:$targetVersion")
    implementation("io.target-kt:target-core:$targetVersion")
    ksp("io.target-kt:target-annotation-processor:$targetVersion")
}
