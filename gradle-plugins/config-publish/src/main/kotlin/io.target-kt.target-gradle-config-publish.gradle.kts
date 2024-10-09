@file:Suppress("SpellCheckingInspection")

plugins {
    `maven-publish`
    signing
}

fun propertyString(propertyName: String) = property(propertyName).toString()

val javadocJar by tasks.registering(Jar::class) {
    val dokkaHtmlProvider = tasks.named("dokkaGfm")
    dependsOn(dokkaHtmlProvider)
    archiveClassifier.set("javadoc")
    from(dokkaHtmlProvider.get().property("outputDirectory"))
}

// region Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://github.com/gradle/gradle/issues/26091#issuecomment-1722947958
tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}
// endregion

publishing {
    repositories {
        maven {
            name = "sonatype"

            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(
                if (propertyString("target.version").contains("SNAPSHOT", ignoreCase = true)) snapshotsRepoUrl
                else releasesRepoUrl
            )

            credentials {
                username = propertyString("ossrhUsername")
                password = propertyString("ossrhPassword")
            }
        }
    }
    publications {
        if (isKotlinMultiplatform) {
            withType<MavenPublication> {
                artifact(javadocJar)
                configurePom()
            }
        } else {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifact(javadocJar)
                configurePom()
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

internal val Project.isKotlinMultiplatform: Boolean
    get() = pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")

fun MavenPublication.configurePom() {
    pom {
        name.set(propertyString("pom.name"))
        description.set(propertyString("pom.description"))
        url.set(propertyString("pom.url"))

        licenses {
            license {
                name.set(propertyString("pom.license.name"))
                url.set(propertyString("pom.license.url"))
            }
        }

        developers {
            developer {
                id.set(propertyString("pom.developer.id"))
                name.set(propertyString("pom.developer.name"))
                email.set(propertyString("pom.developer.email"))
            }
        }

        scm {
            connection.set(propertyString("pom.scm.connection"))
            developerConnection.set(propertyString("pom.scm.developerConnection"))
            url.set(propertyString("pom.scm.url"))
        }
    }
}
