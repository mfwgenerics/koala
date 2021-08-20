plugins {
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))

    testImplementation(kotlin("test"))
    testImplementation("com.h2database:h2:1.4.200")
}
