plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    /* higher than recommended since jdbc tests block */
    maxParallelForks = 8
}