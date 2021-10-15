plugins {
    id("conventions")

    `java-library`
    `maven-publish`
    signing

    id("com.palantir.git-version")
}

val gitVersion: groovy.lang.Closure<*> by extra

group = "io.koalaql"
version = gitVersion()

check("$version".isNotBlank() && version != "unspecified")
    { "invalid version $version" }

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("koala") {
            artifactId = "koala-${project.name}"
            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            pom {
                name.set("KoalaQL")
                description.set("SQL DSL")
                url.set("https://koalaql.io")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        name.set("Damien O'Hara")
                        url.set("https://github.com/mfwgenerics")
                    }
                }

                scm {
                    connection.set("scm:git@github.com:mfwgenerics/koala.git")
                    developerConnection.set("scm:git@github.com:mfwgenerics/koala.git")
                    url.set("https://github.com/mfwgenerics/koala")
                }
            }
        }
    }

    signing {
        useInMemoryPgpKeys(
            System.getenv("GPG_PRIVATE_KEY"),
            System.getenv("GPG_PRIVATE_PASSWORD")
        )

        sign(publishing.publications["koala"])
    }

    repositories {
        maven {
            val repoId = System.getenv("REPOSITORY_ID")

            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/$repoId/")

            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}