@file:Suppress("SpellCheckingInspection")

plugins {
    `maven-publish`
    signing
}

fun propertyString(propertyName: String) = property(propertyName).toString()

publishing {
    repositories {
        maven {
            name = "sonatype"

            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(snapshotsRepoUrl)
//            url = uri(
//                if (version.toString().endsWith("-SNAPSHOT", ignoreCase = true)) snapshotsRepoUrl
//                else releasesRepoUrl
//            )

            credentials {
                username = propertyString("ossrhUsername")
                password = propertyString("ossrhPassword")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

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
    }
}

signing {
    sign(publishing.publications)
}
