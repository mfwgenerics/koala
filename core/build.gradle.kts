plugins {
    id("conventions")
    `java-library`
    `maven-publish`
    signing
}

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("koala") {
            artifactId = "koala-core"
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
        sign(publishing.publications["koala"])
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}