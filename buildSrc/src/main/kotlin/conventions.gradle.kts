plugins {
    kotlin("jvm")

    id("com.palantir.git-version")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

val gitVersion: groovy.lang.Closure<*> by extra

group = "io.koalaql"
version = gitVersion()

check("$version".isNotBlank() && version != "unspecified")
    { "invalid version $version" }