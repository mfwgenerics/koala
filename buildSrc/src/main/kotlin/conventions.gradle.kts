import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    test {
        /* higher than recommended since jdbc tests block */
        maxParallelForks = 8
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}